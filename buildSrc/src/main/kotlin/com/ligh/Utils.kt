package com.ligh

import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet
import java.io.File
import java.util.zip.ZipFile

object Utils {


    fun getConfiguration(project: Project, configurationName: String = "implementation") =
        project.configurations.getByName(configurationName) {
            isCanBeResolved = true
        }


    /**
     * 打印依赖
     * 可以可以根据依赖层级处理，；例如主项目依赖A，A依赖B，此时可以依赖A，可以打印A，B
     */
    fun printDepenendcy(project: Project) {
        val compileConfig = getConfiguration(project)
        compileConfig.dependencies.forEach {
            val files = compileConfig.files(it)
            println(it)
            files.forEach { file ->
                if (file.name.endsWith(".aar")) {
                    println("AAR File: ${file.name}")
                    processAarFile(file)
                }
            }
        }
    }


    /**
     * 获取所有的aar文件
     */
    fun getAllAarDependencyFiles(project: Project) =
        getConfiguration(project).files.filter { file ->
            file.name.endsWith(".aar")
        }

    /**
     * 实例1 ：拿到aar之后zip解压遍历文件自定义处理
     */
    fun processAarFile(aarFile: File) {
        ZipFile(aarFile).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith("res/layout") && entry.name.endsWith(".xml")) {
                    val size = entry.size
                    println("Layout File: ${entry.name}, Size: $size bytes")
                }
            }
        }
    }


    /**
     * 示例2 ： 拿到aar之后zip解压遍历文件自定义处理
     */
    fun processAarFile(project: Project, aarFile: File) {

        project.zipTree(aarFile).matching(PatternSet().include("res/layout")).files.forEach {
            val size = it.length()
            println("Layout File: ${it.name}, Size: $size bytes")
        }
    }


    /**
     * 检查某个权限
     */
    fun checkPermission(project: Project, permission: String) {
        getAllAarDependencyFiles(project).forEach {aarFile->
            project.zipTree(aarFile)
                .matching(PatternSet().include("AndroidManifest.xml")).files.forEach {
                    if (it.readText().contains(permission)) {
                        println(permission)
                        println(aarFile.name)
                    }
                }
        }
    }

    /**
     * 寻找某个文件所在的aar，例如so
     */

    fun findFileInAar(project: Project,fileName :String){
        getAllAarDependencyFiles(project).forEach {aarFile->
           val files = project.zipTree(aarFile)
                .matching(PatternSet().include(fileName)).files
            files.takeIf { it.isNotEmpty() }?.let {
                println(aarFile)
            }
        }
    }

}