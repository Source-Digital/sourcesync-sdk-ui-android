// Android TV demo application build configuration for SourceSync SDK UI
// This module demonstrates SDK integration specifically for Android TV platform
plugins {
    alias(libs.plugins.android.application) // Android application plugin
    alias(libs.plugins.kotlin.android)     // Kotlin Android support
}

android {
    // Application package namespace for TV variant
    namespace = "io.sourcesync.sdk.ui.demo_tv"
    compileSdk = 34 // Target Android API level for compilation (Android 14)

    defaultConfig {
        // Unique application identifier for TV demo app
        applicationId = "io.sourcesync.sdk.ui.demo_tv"
        minSdk = 24     // Minimum Android API level (Android 7.0+)
        targetSdk = 34  // Target Android API level (Android 14)
        versionCode = 1     // Internal version number
        versionName = "1.0" // User-facing version string

        // Note: No test runner configured - TV apps typically have limited testing
    }

    buildTypes {
        release {
            // Disable code obfuscation for demo app transparency
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java compatibility settings
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Java 11 source compatibility
        targetCompatibility = JavaVersion.VERSION_11 // Java 11 target compatibility
    }

    // Kotlin compiler settings
    kotlinOptions {
        jvmTarget = "11" // Kotlin JVM target version
    }
}

dependencies {
    // Android TV specific framework
    implementation(libs.androidx.leanback)  // Leanback library for TV UI patterns

    // Core Android components
    implementation(libs.androidx.core.ktx)  // Kotlin extensions for Android

    // Image loading library optimized for TV
    implementation("com.github.bumptech.glide:glide:4.16.0") // Glide for efficient image loading

    // implementation(project(":sourcesync-sdk-ui"))
}