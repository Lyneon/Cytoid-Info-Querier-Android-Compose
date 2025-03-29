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
        versionCode = 22
        versionName = "2.7.1-${getCurrentCommitHash()}"

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
    val composeBomVersion = "2025.03.01"

    implementation("io.sentry:sentry-android:8.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.tencent:mmkv:2.1.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("dev.shreyaspatil:capturable:3.0.0")
    implementation("com.patrykandpatrick.vico:compose:2.1.1")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.2")
    implementation("androidx.media3:media3-exoplayer:1.6.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.5.0")
    implementation("com.github.jeziellago:compose-markdown:0.5.4")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("dev.rikka.shizuku:api:13.1.0")
    implementation("dev.rikka.shizuku:provider:13.1.0")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
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
    } catch (e: Exception) {
        "unknownCommit"
    }
}