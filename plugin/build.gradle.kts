plugins {
    `kotlin-dsl`
    `maven-publish`
}

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
        create("res") {
            id = "com.ligh.res"
            implementationClass = "com.ligh.plugin.MyPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:8.1.1")
    implementation("pink.madis.apk.arsc:android-chunk-utils:0.0.7")
}

publishing {
    publications {
        register<MavenPublication>("publish") {
            groupId = "com.ligh.res"
            artifactId = "res"
            version = "2.0.0-SNAPSHOT"

            from(components["java"])
        }
    }
}


