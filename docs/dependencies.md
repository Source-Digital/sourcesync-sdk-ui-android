# Dependencies Guide

This document provides a comprehensive overview of all dependencies used in the SourceSync SDK UI Android project.

## Overview

The SDK follows a minimal dependency approach, relying primarily on:
- **DivKit** for declarative UI rendering
- **Picasso** for image loading
- **AndroidX** libraries for platform compatibility

## Gradle Configuration

### Gradle Version
- **Gradle**: 8.x (via wrapper)
- **Android Gradle Plugin**: 8.2.0+
- **Kotlin**: 1.9.0+

### Build Configuration Files
```
├── build.gradle.kts          # Root project configuration
├── settings.gradle.kts       # Module and repository settings
├── gradle.properties         # Global project properties
└── gradle/
    └── libs.versions.toml    # Version catalog (if used)
```

## Core Dependencies

### 1. DivKit (Yandex Declarative UI)

**Purpose**: JSON-based UI rendering engine

**Modules Used**:
```gradle
dependencies {
    implementation("com.yandex.div:div:30.9.0")
    implementation("com.yandex.div:div-core:30.9.0")
    implementation("com.yandex.div:div-json:30.9.0")
}
```

**Why DivKit**:
- Declarative UI from JSON templates
- Cross-platform consistency (iOS/Android)
- Rich component library (containers, text, images, actions)
- Efficient rendering and memory management

**Key Classes Used**:
- `Div2View` - Main rendering component
- `Div2Context` - Configuration and context
- `DivConfiguration` - Builder for setup
- `DivDataTag` - Data identification
- `DivData` - Parsed template data

**Documentation**: [DivKit GitHub](https://github.com/yandex/divkit)

### 2. Picasso (Image Loading)

**Purpose**: Efficient image loading, caching, and transformation

**Version**:
```gradle
implementation("com.squareup.picasso:picasso:2.8")
```

**Features Used**:
- Disk and memory caching
- Automatic request cancellation
- Image transformation
- Placeholder and error handling

**Integration Point**: `PicassoDivImageLoader.kt` implements DivKit's `DivImageLoader` interface

**Why Picasso**:
- Lightweight and stable
- Automatic memory management
- Thread-safe operations
- Good OkHttp integration

**Documentation**: [Square Picasso](https://square.github.io/picasso/)

### 3. OkHttp (HTTP Client)

**Purpose**: Network operations for image loading

**Version**:
```gradle
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

**Usage**: Transitive dependency via Picasso, provides:
- HTTP/2 support
- Connection pooling
- Automatic retries
- GZIP compression

### 4. AndroidX Libraries

**Purpose**: Android platform compatibility and utilities

```gradle
// Core Android components
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")

// RecyclerView (used by DivKit)
implementation("androidx.recyclerview:recyclerview:1.3.2")

// ConstraintLayout (optional, for demo)
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
```

**Why AndroidX**:
- Backward compatibility to API 21
- Modern Android APIs
- Consistent behavior across versions

## Development Dependencies

### Testing Libraries

```gradle
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

**Test Framework Breakdown**:
- **JUnit 4**: Unit test framework
- **Mockito**: Mocking framework for tests
- **Mockito-Kotlin**: Kotlin-friendly mocking
- **AndroidX Test**: Instrumented testing
- **Espresso**: UI testing

### Static Analysis

```gradle
// Lint (built into Android Gradle Plugin)
// No additional dependencies needed
```

## SDK Publishing Dependencies

### Maven Publishing

```gradle
plugins {
    id("com.vanniktech.maven.publish") version "0.25.3"
}
```

**Purpose**: Simplified Maven Central publishing with automatic:
- POM generation
- Source/Javadoc JAR creation
- GPG signing
- Central Portal upload

### Signing Plugin

```gradle
plugins {
    id("signing")
}
```

**Purpose**: GPG signing of artifacts for Maven Central

## Dependency Management

### Version Pinning Strategy

The project uses **explicit version pinning** to ensure:
- Reproducible builds
- Controlled updates
- Compatibility testing

### No Core SDK Dependency

**Important**: This SDK UI library is **independent** and does NOT depend on:
- SourceSync Core SDK (KMP)
- Any backend integration libraries
- Analytics or tracking SDKs

This allows the UI library to be used standalone for rendering DivKit-based content.

### Transitive Dependencies

The SDK minimizes transitive dependencies. Main transitive dependencies come from:

**DivKit** brings:
- Kotlin stdlib
- JSON parsing
- AndroidX libraries

**Picasso** brings:
- OkHttp
- Okio

**Total transitive dependency count**: ~15-20 (minimal for functionality)

## Dependency Conflicts

### Known Conflicts and Resolutions

#### Kotlin Version Alignment
```gradle
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    }
}
```

#### AndroidX Version Consistency
```gradle
// Ensure all AndroidX libraries use compatible versions
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
```

## ProGuard/R8 Configuration

### Consumer Rules (`consumer-rules.pro`)

These rules are automatically applied to apps consuming the SDK:

```proguard
# Keep public API
-keep public class io.sourcesync.sdk.ui.** { public *; }

# DivKit requirements
-keep class com.yandex.div.** { *; }
-keep class com.yandex.div2.** { *; }

# Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**
```

### Internal Rules (`proguard-rules.pro`)

Additional rules for SDK internal optimization:

```proguard
# Preserve model classes
-keep class io.sourcesync.sdk.ui.models.** { *; }

# Keep DivKit integration
-keep class io.sourcesync.sdk.ui.helpers.PicassoDivImageLoader { *; }
-keep class io.sourcesync.sdk.ui.helpers.CustomUrlHandler { *; }
```

## Platform Requirements

### Minimum API Level
```gradle
android {
    defaultConfig {
        minSdk = 21  // Android 5.0 Lollipop
    }
}
```

**Why API 21**:
- Broad device coverage (>99% of devices)
- Modern Android features available
- DivKit minimum requirement

### Target API Level
```gradle
android {
    defaultConfig {
        targetSdk = 34  // Android 14
    }
}
```

### Compile SDK
```gradle
android {
    compileSdk = 34
}
```

## Kotlin Configuration

### Kotlin JVM Target
```gradle
kotlin {
    jvmToolchain(21)
}
```

### Kotlin Compiler Options
```gradle
kotlinOptions {
    jvmTarget = "21"
    freeCompilerArgs += listOf(
        "-Xjvm-default=all",
        "-opt-in=kotlin.RequiresOptIn"
    )
}
```

## Repository Configuration

### Required Repositories

```gradle
repositories {
    google()           // AndroidX and Google libraries
    mavenCentral()     // DivKit, Picasso, standard libraries
}
```

**Note**: No custom repositories required - all dependencies from public repos

## Dependency Update Strategy

### Version Compatibility Matrix

| Component | Current Version | Min Compatible | Max Tested |
|-----------|----------------|----------------|------------|
| Gradle    | 8.5            | 8.0            | 8.6         |
| AGP       | 8.2.0          | 8.0.0          | 8.3.0      |
| Kotlin    | 1.9.0          | 1.9.0          | 2.0.0       |
| DivKit    | 30.9.0         | 30.0.0         | 30.9.0     |
| Picasso   | 2.8            | 2.7            | 2.8         |

### Update Process

1. **Check Compatibility**: Review release notes for breaking changes
2. **Update in Branch**: Never update dependencies directly in main
3. **Run Full Test Suite**: Unit + instrumentation tests
4. **Test Demo Apps**: Verify visual rendering
5. **Update Documentation**: Note any API changes
6. **Create PR**: Document changes and testing results

```

### Dependency Sources

All dependencies from trusted sources:
- ✅ Google (androidx)
- ✅ Yandex (DivKit)
- ✅ Square (Picasso, OkHttp)
- ✅ JetBrains (Kotlin)

## Migration Guides

### Upgrading DivKit

When upgrading DivKit versions:

1. **Check Breaking Changes**: Review DivKit release notes
2. **Test Parsing**: Verify existing JSON templates still parse
3. **Test Rendering**: Check visual output hasn't changed
4. **Update Helpers**: Adjust `PicassoDivImageLoader` if interfaces changed

### Upgrading Picasso

Picasso 2.x is stable; 3.x not yet released. When it arrives:

1. **Review API Changes**: Check method signatures
2. **Update Loader**: Modify `PicassoDivImageLoader` integration
3. **Test Image Loading**: Verify caching still works
4. **Check Performance**: Monitor memory usage

## Troubleshooting

### Dependency Resolution Failures

**Problem**: Gradle can't resolve dependency

**Solution**:
```bash
# Clear Gradle cache
./gradlew --refresh-dependencies

# Or delete cache manually
rm -rf ~/.gradle/caches/
```

### Duplicate Class Errors

**Problem**: `Duplicate class found in modules`

**Solution**: Add exclusion in build.gradle:
```gradle
configurations.all {
    exclude(group = "com.example", module = "conflicting-library")
}
```

### Version Conflict Warnings

**Problem**: Multiple versions of same library

**Solution**: Force specific version:
```gradle
configurations.all {
    resolutionStrategy.force("com.library:name:1.2.3")
}
```

## Related Documentation

- [Setup Guide](setup.md) - Build environment setup
- [CI/CD Guide](cicd.md) - Publishing process
- [Architecture](architecture.md) - System design

## Dependency License Summary

| Dependency | License | Commercial Use |
|-----------|---------|----------------|
| DivKit | Apache 2.0 | ✅ Yes |
| Picasso | Apache 2.0 | ✅ Yes |
| OkHttp | Apache 2.0 | ✅ Yes |
| AndroidX | Apache 2.0 | ✅ Yes |
| Kotlin | Apache 2.0 | ✅ Yes |

**SDK License**: Apache 2.0

All dependencies are compatible with commercial use.