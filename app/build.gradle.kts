plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
}

android {
    namespace = "com.lyneon.cytoidinfoquerier"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lyneon.cytoidinfoquerier"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 23
        versionName = "2.7.2-${getCurrentCommitHash()}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters.run {
                add("arm64-v8a")
                add("x86_64")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.sentry.android)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coil.compose)
    implementation(libs.mmkv)
    implementation(libs.navigation.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.capturable)
    implementation(libs.compose)
    implementation(libs.compose.m3)
    implementation(libs.media3.exoplayer)
    implementation(libs.material3.window.size.class1)
    implementation(libs.androidbrowserhelper)
    implementation(libs.compose.markdown)
    implementation(libs.documentfile)
    implementation(libs.material)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

fun getCurrentCommitHash(): String {
    return try {
        val process = ProcessBuilder().command("git", "rev-parse", "--short", "HEAD")
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use {
            it.readLine()?.trim()?.takeIf { str -> str.isNotEmpty() } ?: "unknownCommit"
        }
    } catch (_: Exception) {
        "unknownCommit"
    }
}