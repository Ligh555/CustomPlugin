package com.ligh.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.ligh.plugin.ZipUtils.unZip
import com.ligh.plugin.ZipUtils.zipFolder
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class ResPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            // 1. 找到 ProcessXXXResources 这个 task
//            val processResSet = project.tasks.filter { task ->
//                project.extensions.getByType(com.android.build.gradle.AppExtension::class.java).applicationVariants.any  { variant ->
//                    task.name == "process${variant.name.capitalize()}Resources"
//                }
//            }

//            if (processResSet.isEmpty()) {
//                return@afterEvaluate
//            }

            val isLibrary = project.plugins.hasPlugin("com.android.library")
            val variants = if (isLibrary) {
                (project.property("android") as LibraryExtension).libraryVariants
            } else {
                (project.property("android") as AppExtension).applicationVariants
            }
            variants.forEach { variant -> }

            // 2. 将自定义脚本放在 ProcessAndroidResources 这个 task 之后执行
            processResSet.forEach { processRes ->
                processRes.doLast {
                    // 获取资源打包输出的文件夹，
                    val fileList = this.project.variant.allRawAndroidResources.files
                    if (fileList != null) {
                        for (file in fileList) {
                            // 3. 找到 .ap_ 文件
                            if (file.isFile && file.path.endsWith(".ap_")) {
                                val packageOutputFile = file
                                // 4. 解压 .ap_ 文件
                                val prefixIndex = packageOutputFile.path.lastIndexOf(".")
                                val unzipPath = packageOutputFile.path.substring(
                                    0,
                                    prefixIndex
                                ) + File.separator
                                unZip(packageOutputFile, unzipPath)

                                // 5. 解析 resources.arsc 文件，并进行图片去重操作
                                RemoveDupRes.optimize(unzipPath)

                                // 6. 将解压后的文件重新打包成 .ap_ zip 压缩包
                                zipFolder(unzipPath, packageOutputFile.path)
                            }
                        }
                    }
                }
            }
        }
    }
}