package com.ligh.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
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
                val processResource = project.tasks.getByName("process${variantName}Resources")
                processResource.doLast {
                    // 获取资源打包输出的文件夹，
                    val resourcesTask = this as LinkApplicationAndroidResourcesTask
                    val resPackageOutputFolder = resourcesTask.resPackageOutputFolder
                    // 3. 找到 .ap_ 文件
                    resPackageOutputFolder.asFileTree
                        .files
                        .filter { it.name.endsWith(".ap_") }
                        .firstOrNull()?.let { _apFile ->
                            // 4. 解压 .ap_ 文件
                            val prefixIndex = _apFile.path.lastIndexOf(".")
                            val unzipPath = _apFile.path.substring(
                                0,
                                prefixIndex
                            ) + File.separator
                            ZipUtils.unZipIt(_apFile.absolutePath, unzipPath)
                            // 5. 解析 resources.arsc 文件，并进行图片去重操作
                            RemoveDupRes.optimize(unzipPath)
                            // 6. 将解压后的文件重新打包成 .ap_ zip 压缩包
                            zipFolder(unzipPath, _apFile.path)
                        }
                }
            }
        }
    }
}