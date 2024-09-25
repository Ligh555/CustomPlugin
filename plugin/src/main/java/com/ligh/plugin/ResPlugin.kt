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
        val isLibrary = project.plugins.hasPlugin("com.android.library")
        val variants = if (isLibrary) {
            (project.property("android") as LibraryExtension).libraryVariants
        } else {
            (project.property("android") as AppExtension).applicationVariants
        }
        project.afterEvaluate {
            variants.forEach { variant ->
                val variantName = variant.name.capitalize()
                val processResource = project.tasks.getByName("package${variantName}")
                processResource.doFirst {
                    // 获取资源打包输出的文件夹，
                    variant.allRawAndroidResources.files.forEach { file ->
                        // 3. 找到 .ap_ 文件
                        if (file.isFile && file.path.endsWith(".ap_")) {
                            // 4. 解压 .ap_ 文件
                            val prefixIndex = file.path.lastIndexOf(".")
                            val unzipPath = file.path.substring(
                                0,
                                prefixIndex
                            ) + File.separator
                            unZip(file, unzipPath)

                            // 5. 解析 resources.arsc 文件，并进行图片去重操作
                            RemoveDupRes.optimize(unzipPath)

                            // 6. 将解压后的文件重新打包成 .ap_ zip 压缩包
                            zipFolder(unzipPath, file.path)
                        }
                    }

                }
            }
        }
    }
}