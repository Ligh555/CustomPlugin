package com.ligh


import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            val task = target.tasks.register("hello") {
                doLast {
                    println("hello excution")
                    val timeStr = File("D:\\code\\git\\CustomPlugin\\app\\src\\main\\java\\com\\ligh\\customplugin\\MainActivity.kt").lastModified()
                    println(timeStr)
                }
            }
            target.tasks.findByName("dataBindingGenBaseClassesDebug")?.finalizedBy(task)

        }
    }



}