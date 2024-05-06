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
    override fun apply(target: Project) {
        target.afterEvaluate {
            val task = target.tasks.register("insetLfayout") {
                doLast {
                    println("insetLfayout excution")
                    val bundle = creatLayoutFileBundle()
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

                    writer.writeToFile(File(xmlOutDir, bundle.fileName +"-"+ bundle.directory+".xml"), bundle.toXML())

                }
            }
            println("in configuration")
            target.tasks.findByName("processDebugMainManifest")?.finalizedBy(task)

            val task1 = target.tasks.register("customCreateViewBinding") {
                doLast {
                    println("customCreateViewBinding excution")
                    customCreatBinding()
                }
            }
            target.tasks.findByName("dataBindingGenBaseClassesDebug")?.finalizedBy(task1)

            printDependices(target)
        }
    }

    private fun creatLayoutFileBundle(): ResourceBundle.LayoutFileBundle {
        val path =
            "D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\data_binding_layout_info_type_package\\debug\\out\\activity_main-layout.xml"
        println("qwe")

        val input = File("D:\\code\\git\\Router\\test\\src\\main\\res\\layout\\test.xml")
        val dirFile = File("D:\\code\\git\\Router\\test")

//        val input = File("D:\\code\\git\\CustomPlugin\\app\\src\\main\\res\\layout\\activity_main.xml")
//        val dirFile = File("D:\\code\\git\\CustomPlugin")
        val outFile =
            File("D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\incremental\\debug\\ligh\\layout")
        val bulder = LayoutFileParser.parseXml(
            RelativizableFile.fromAbsoluteFile(input, dirFile),
            outFile,
            "com.ligh.customplugin",
            CustomLoomUp(),
            true,
            false
        )

        val testString =
            "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?><Layout layout=\"test\" modulePackage=\"com.ligh.customplugin\" filePath=\"src\\main\\res\\layout\\test.xml\" directory=\"layout\" isMerge=\"false\" isBindingData=\"false\" rootNodeType=\"android.widget.LinearLayout\"><Targets><Target tag=\"layout/test_0\" view=\"LinearLayout\"><Expressions/><location startLine=\"1\" startOffset=\"0\" endLine=\"12\" endOffset=\"14\"/></Target></Targets></Layout>"
        println(bulder.toXML())
        return bulder
    }

    private fun printDependices(project: Project) {
        val configurations = project.configurations
        val compileConfig = configurations.getByName("implementation")
        compileConfig.isCanBeResolved = true
        compileConfig.dependencies.forEach { println(it) }

    }

    fun genertR(str1: String, str2: String): String {
        return "com.ligh.test"
    }

    private fun customCreatBinding() {
        val bundle = creatLayoutFileBundle()
        val layoutModel = BaseLayoutModel(listOf(bundle), ::genertR)
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