plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ligh.customplugin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ligh.customplugin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val agp = "8.1.1"
    val kotlin = "1.9.0"
    val lifecycleRuntimeKtx = "2.6.1"
    val appcompat = "1.6.1"
    val material = "1.10.0"
    val activity = "1.8.0"
    val constraintlayout = "2.1.4"


    implementation("androidx.activity:activity:$activity")
    implementation("androidx.constraintlayout:constraintlayout:$constraintlayout")
    implementation("androidx.appcompat:appcompat:$appcompat")
    implementation("androidx.activity:activity-ktx:$activity")
    implementation("com.google.android.material:material:$material")

}