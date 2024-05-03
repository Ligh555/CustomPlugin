package com.ligh


import org.gradle.api.Plugin
import org.gradle.api.Project

class MyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            val task = target.tasks.register("hello") {
                doLast {
                    println("hello excution")
                }
            }
            target.tasks.findByName("dataBindingGenBaseClassesDebug")?.finalizedBy(task)
        }
    }
}