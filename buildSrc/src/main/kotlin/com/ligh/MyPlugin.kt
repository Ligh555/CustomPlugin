package com.ligh

import android.databinding.tool.DataBindingBuilder
import android.databinding.tool.LibTypes
import android.databinding.tool.store.LayoutFileParser
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.util.RelativizableFile
import com.ligh.dataBinding.BaseLayoutBinderWriter
import android.databinding.tool.writer.BaseLayoutModel
import android.databinding.tool.writer.toJavaFile
import com.squareup.javapoet.JavaFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MyPlugin :Plugin<Project> {
    override fun apply(target: Project) {
        val task =  target.tasks.register("hello"){
            doLast {
                println("hello excution")
                test()
            }
        }
        task.configure {
            dependsOn(target.tasks.getByPath(":app:dataBindingGenBaseClassesDebug"))
            target.tasks.getByPath(":app:processDebugManifest").dependsOn(this)
        }

        doImpl(target)
    }

    private fun doTask() :  ResourceBundle.LayoutFileBundle{
        val path = "D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\data_binding_layout_info_type_package\\debug\\out\\activity_main-layout.xml"
        println("qwe")

        val input = File("D:\\code\\git\\Router\\test\\src\\main\\res\\layout\\test.xml")
        val dirFile =File("D:\\code\\git\\Router\\test")

//        val input = File("D:\\code\\git\\CustomPlugin\\app\\src\\main\\res\\layout\\activity_main.xml")
//        val dirFile = File("D:\\code\\git\\CustomPlugin")
        val outFile = File("D:\\code\\git\\CustomPlugin\\app\\build\\intermediates\\incremental\\debug\\ligh\\layout\\activity_main.xml")
        val bulder =  LayoutFileParser.parseXml(RelativizableFile.fromAbsoluteFile(input,dirFile),outFile,"com.ligh.customplugin",CustomLoomUp(),true,false)

        val testString ="<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?><Layout layout=\"test\" modulePackage=\"com.ligh.customplugin\" filePath=\"src\\main\\res\\layout\\test.xml\" directory=\"layout\" isMerge=\"false\" isBindingData=\"false\" rootNodeType=\"android.widget.LinearLayout\"><Targets><Target tag=\"layout/test_0\" view=\"LinearLayout\"><Expressions/><location startLine=\"1\" startOffset=\"0\" endLine=\"12\" endOffset=\"14\"/></Target></Targets></Layout>"
        println(bulder.toXML())
        return bulder
    }

    private fun doImpl(project: Project){
        val configurations = project.configurations
        val compileConfig = configurations.getByName("implementation")
        compileConfig.isCanBeResolved = true
        compileConfig.dependencies.forEach { println(it) }

    }
    fun genertR(str1 :String,str2 :String) :String{
        return  "com.ligh.test"
    }

    private fun test(){
        val bundle = doTask()
        val layoutModel = BaseLayoutModel(listOf(bundle),::genertR)
        val isDataBinding = false
        val javaFile :JavaFile
        if (isDataBinding) {
            val binderWriter = BaseLayoutBinderWriter(layoutModel, LibTypes(true), "com.ligh.test")
             javaFile = binderWriter.write()
        }else{
            val viewBinder = layoutModel.toViewBinder("com.ligh.test")
            javaFile = viewBinder.toJavaFile(false)
        }
        val writer =DataBindingBuilder.GradleFileWriter("D:\\code\\git\\CustomPlugin\\app\\build\\generated\\data_binding_base_class_source_out\\debug\\out")
        writer.writeToFile(javaFile)
    }
}