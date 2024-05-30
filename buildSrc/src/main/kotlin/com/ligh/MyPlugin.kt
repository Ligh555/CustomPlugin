package com.ligh

import android.databinding.tool.DataBindingBuilder
import android.databinding.tool.LibTypes
import android.databinding.tool.store.LayoutFileParser
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.util.RelativizableFile
import android.databinding.tool.writer.BaseLayoutModel
import android.databinding.tool.writer.JavaFileWriter
import android.databinding.tool.writer.toJavaFile
import com.ligh.dataBinding.BaseLayoutBinderWriter
import com.squareup.javapoet.JavaFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MyPlugin : Plugin<Project> {


    var aarList = mutableListOf("test")
    override fun apply(target: Project) {
        target.afterEvaluate {
            val task = target.tasks.register("insetLfayout") {
                doLast {
                    println("insetLfayout excution")
                    val bundles = createLayoutFileBundle(target)
                    val writer = object : JavaFileWriter() {
                        // These methods are not supposed to be used, they are here only because
                        // the superclass requires an implementation of it.
                        // Whichever is calling this method is probably using it incorrectly
                        // (see stacktrace).
                        override fun writeToFile(canonicalName: String, contents: String) {
                            throw UnsupportedOperationException(
                                "Not supported in this mode"
                            )
                        }

                        override fun deleteFile(canonicalName: String) {
                            throw UnsupportedOperationException(
                                "Not supported in this mode"
                            )
                        }
                    }
                    val xmlOutDir = "D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\data_binding_layout_info_type_merge\\debug\\out"
                    bundles.forEach { bundle ->
                        writer.writeToFile(File(xmlOutDir, bundle.fileName +"-"+ bundle.directory+".xml"), bundle.toXML())
                    }
                }
            }
            println("in configuration")
            Utils.getConfiguration(project)
            target.tasks.findByName("processDebugMainManifest")?.finalizedBy(task)

            val task1 = target.tasks.register("customCreateViewBinding") {
                doLast {
                    println("customCreateViewBinding excution")
                    customCreatBinding()
                }
            }
            target.tasks.findByName("dataBindingGenBaseClassesDebug")?.finalizedBy(task1)
        }
    }


    private fun collectLayoutFile(project: Project): List<File> {
        val aarFile = Utils.getConfiguration(project).files.find { file ->
            file.name.contains("test")
        }

        return aarFile?.let {
            project.zipTree(aarFile).files.toList().filter { it.absolutePath.contains("res\\layout") }
        } ?: emptyList()
    }

    private fun createLayoutFileBundle(project: Project): List<ResourceBundle.LayoutFileBundle> {
        val outFile =
            File("D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\incremental\\debug\\ligh\\layout")
        return collectLayoutFile(project).map {
            LayoutFileParser.parseXml(
                RelativizableFile.fromAbsoluteFile(it, it.parentFile),
                outFile,
                "com.ligh.customplugin",
                CustomLoomUp(),
                true,
                false
            )
        }
    }


    fun genertR(str1: String, str2: String): String {
        return "com.ligh.test"
    }

    private fun customCreatBinding() {

        val path = "D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\incremental\\debug\\ligh\\layout"
        var mBundles = mutableListOf<ResourceBundle.LayoutFileBundle>()
        val directory = File(path)

        if (directory.exists() && directory.isDirectory) {
            directory.walk().forEach {
                if (it.isFile) {
                    val bundle = ResourceBundle.LayoutFileBundle.fromXML(it.inputStream())
                    mBundles.add(bundle)
                }
            }
        }

        val layoutModel = BaseLayoutModel(mBundles, ::genertR)
        val isDataBinding = false
        val javaFile: JavaFile
        if (isDataBinding) {
            val binderWriter = BaseLayoutBinderWriter(layoutModel, LibTypes(true), "com.ligh.test")
            javaFile = binderWriter.write()
        } else {
            val viewBinder = layoutModel.toViewBinder("com.ligh.test")
            javaFile = viewBinder.toJavaFile(false)
        }
        val writer =
            DataBindingBuilder.GradleFileWriter("D:\\code\\git\\CustomPlugin\\app\\build\\generated\\data_binding_base_class_source_out\\debug\\out")
        writer.writeToFile(javaFile)
    }
}