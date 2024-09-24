plugins {
//    //Plugin用Java语言编写时需添加
//    `java-gradle-plugin` //等同于 id("java-gradle-plugin") apply true
//
//    //Plugin用Groovy语言编写时需添加
//    `groovy` //等同于 id("groovy") apply true

    //Plugin用Kotlin语言编写时需添加
    `kotlin-dsl` //等同于 id("org.gradle.kotlin.kotlin-dsl") version "4.1.2"
    // 也可以用 id("org.jetbrains.kotlin.jvm") version "1.9.10" apply true (不建议，这个只有Kotlin的语法，而没有Kotlin DSL的语法)

}

//使用 kotlin-dsl / org.jetbrains.kotlin.jvm 的时候，需要mavenCentral仓库
repositories {
    mavenLocal()
    maven {
        setUrl("https://maven.aliyun.com/repository/google")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/public")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/jcenter")
    }
    maven {
        setUrl("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        setUrl("https://jitpack.io")
    }
    google()
    mavenCentral()

}

gradlePlugin {
    plugins {
        create("test") {
            id = "com.ligh.viewBind"
            //Java插件主类
            //implementationClass = "com.heiko.buildsrc.MyPlugin"
            //Groovy插件主类
            //implementationClass = "com.heiko.buildsrc.MyPluginGroovy"
            //Kotlin插件主类
            implementationClass = "com.ligh.MyPlugin"
        }
    }
}

dependencies{
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:8.1.1")
}

