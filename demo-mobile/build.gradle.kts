
// Demo application build configuration for SourceSync SDK UI
// This module demonstrates integration and usage of the SourceSync SDK UI library
plugins {
    alias(libs.plugins.android.application) // Android application plugin
    alias(libs.plugins.kotlin.android)     // Kotlin Android support
}

android {
    // Application package namespace
    namespace = "io.sourcesync.sdk.ui.demo_mobile"
    compileSdk = 35  // Target Android API level for compilation

    defaultConfig {
        // Unique application identifier for demo app
        applicationId = "io.sourcesync.sdk.ui.demo_mobile"
        minSdk = 24     // Minimum Android API level (Android 7.0+)
        targetSdk = 35  // Target Android API level
        versionCode = 1     // Internal version number
        versionName = "1.0" // User-facing version string

        // Test runner for instrumented tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Disable code obfuscation for demo app clarity
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
    // SourceSync SDK UI library (local project dependency)
    implementation(project(":sourcesync-sdk-ui"))

    // Core Android UI components
    implementation(libs.androidx.appcompat)         // AppCompat for backward compatibility
    implementation(libs.material)                   // Material Design components
    implementation(libs.androidx.activity)          // Activity framework
    implementation(libs.androidx.constraintlayout)  // ConstraintLayout for complex layouts
    implementation(libs.androidx.core.ktx)          // Kotlin extensions for Android

    // Unit testing framework
    testImplementation(libs.junit)

    // Android instrumentation testing
    androidTestImplementation(libs.androidx.junit)        // JUnit for Android tests
    androidTestImplementation(libs.androidx.espresso.core) // UI testing framework
}