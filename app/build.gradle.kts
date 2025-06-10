import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.22"
}

val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android.buildFeatures.buildConfig = true

android {
    namespace = "com.cipherxzc.whatsnext"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cipherxzc.whatsnext"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${localProps["AZURE_OPENAI_API_KEY"]}\"")
        buildConfigField("String", "AZURE_OPENAI_RESOURCE_NAME", "\"${localProps["AZURE_OPENAI_RESOURCE_NAME"]}\"")
        buildConfigField("String", "AZURE_OPENAI_DEPLOYMENT_ID", "\"${localProps["AZURE_OPENAI_DEPLOYMENT_ID"]}\"")
        buildConfigField("String", "AZURE_OPENAI_API_VERSION", "\"${localProps["AZURE_OPENAI_API_VERSION"]}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)         // 登录与注册
    implementation(libs.firebase.firestore)    // 云数据库
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.openai.client)
    implementation(libs.ktor.ktor.client.okhttp)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}