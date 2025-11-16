# Development Setup Guide

This guide will help you set up your development environment for the SourceSync SDK UI Android project.

## Prerequisites

### Required Software

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: Version 21 (Zulu distribution recommended)
- **Gradle**: 8.x (managed by wrapper, no manual installation needed)
- **Git**: Latest stable version

### Minimum System Requirements

- **OS**: Windows 10/11, macOS 11+, or Linux (Ubuntu 20.04+)
- **RAM**: 8GB minimum, 16GB recommended
- **Disk Space**: 10GB free space for Android SDK and build outputs

## Environment Setup

### 1. Install JDK 21

#### macOS (using Homebrew)
```bash
brew install --cask zulu@21
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install zulu-21
```

#### Windows
Download and install Zulu JDK 21 from [Azul Systems](https://www.azul.com/downloads/?package=jdk#zulu)

#### Verify Installation
```bash
java -version
# Should show: openjdk version "21.x.x"
```

### 2. Install Android Studio

1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install and launch Android Studio
3. Follow the setup wizard to install:
   - Android SDK Platform 34 (Android 14)
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools
   - Android Emulator (optional, for testing)

### 3. Configure Android SDK

Set the `ANDROID_HOME` environment variable:

#### macOS/Linux
Add to your `~/.bashrc`, `~/.zshrc`, or `~/.profile`:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
# OR
export ANDROID_HOME=$HOME/Android/Sdk          # Linux

export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```

Then reload:
```bash
source ~/.bashrc  # or ~/.zshrc
```

#### Windows
1. Open System Properties → Environment Variables
2. Add new system variable:
   - Variable name: `ANDROID_HOME`
   - Variable value: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk`

### 4. Clone the Repository

```bash
git clone https://github.com/Source-Digital/sourcesync-android-sdk-ui.git
cd sourcesync-android-sdk-ui
```

## Building the Project

### Command Line Build

#### Make Gradle Wrapper Executable (Linux/macOS)
```bash
chmod +x ./gradlew
```

#### Build the SDK Library
```bash
# Debug build
./gradlew :sourcesync-sdk-ui:assembleDebug

# Release build
./gradlew :sourcesync-sdk-ui:assembleRelease

# Build all modules
./gradlew build
```

#### Build Output Locations
- **SDK Library**: `sourcesync-sdk-ui/build/outputs/aar/`
- **Demo Apps**: `demo-mobile/build/outputs/apk/`

### Android Studio Build

1. Open Android Studio
2. Select **File → Open** and choose the project root directory
3. Wait for Gradle sync to complete
4. Select build variant: **Build → Select Build Variant**
5. Build: **Build → Make Project** (or `Cmd+F9` / `Ctrl+F9`)

## Running Demo Applications

### Mobile Demo App

#### From Command Line
```bash
# Install on connected device/emulator
./gradlew demo-mobile:installDebug

# Run the app
adb shell am start -n io.sourcesync.sdk.ui.demo_mobile/.MainActivity
```

#### From Android Studio
1. Select **demo-mobile** from the run configuration dropdown
2. Click **Run** (green play button) or press `Shift+F10`
3. Choose target device/emulator

### TV Demo App

#### From Command Line
```bash
./gradlew demo-tv:installDebug
adb shell am start -n io.sourcesync.sdk.ui.demo_tv/.MainActivity
```

#### From Android Studio
1. Select **demo-tv** from the run configuration dropdown
2. Choose an Android TV emulator or device
3. Click **Run**

## Running Tests

### Unit Tests

Unit tests run on the JVM without requiring an Android device.

#### Run All Unit Tests
```bash
./gradlew test
```

#### Run SDK Library Tests Only
```bash
./gradlew :sourcesync-sdk-ui:test
```

#### Run Tests with Coverage
```bash
./gradlew :sourcesync-sdk-ui:testDebugUnitTest jacocoTestReport
```

Coverage reports are generated at:
```
sourcesync-sdk-ui/build/reports/jacoco/test/html/index.html
```

#### From Android Studio
1. Right-click on test file or directory
2. Select **Run Tests** or **Run with Coverage**

### Instrumented Tests

Instrumented tests require a connected device or emulator (API 21+).

#### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

#### Run SDK Library Instrumented Tests
```bash
./gradlew :sourcesync-sdk-ui:connectedAndroidTest
```

#### Test Results Location
```
sourcesync-sdk-ui/build/reports/androidTests/connected/index.html
```

## Common Development Tasks

### Clean Build
```bash
./gradlew clean
```

### Lint Checks
```bash
./gradlew lint
```

### Generate Javadoc
```bash
./gradlew :sourcesync-sdk-ui:dokkaHtml
```

Output location: `sourcesync-sdk-ui/build/dokka/html/index.html`

### Local Maven Publishing (for testing)
```bash
./gradlew :sourcesync-sdk-ui:publishToMavenLocal
```

This publishes to your local Maven repository (`~/.m2/repository/`)

## Project Structure

```
sourcesync-android-sdk-ui/
├── sourcesync-sdk-ui/          # Main SDK library module
│   ├── src/main/               # Library source code
│   └── src/test/               # Unit tests
├── demo-mobile/                # Mobile demo app
│   └── src/main/assets/        # Demo templates
├── demo-tv/                    # TV demo app
├── docs/                       # Documentation
├── gradle/                     # Gradle wrapper and catalogs
├── build.gradle.kts            # Root build configuration
└── settings.gradle.kts         # Module settings
```

## Troubleshooting

### Gradle Sync Failures

**Problem**: Gradle sync fails with dependency resolution errors

**Solution**:
```bash
# Clear Gradle caches
./gradlew clean
rm -rf ~/.gradle/caches/

# Invalidate Android Studio caches
# File → Invalidate Caches → Invalidate and Restart
```

### Build Fails with "SDK location not found"

**Problem**: Android SDK path not configured

**Solution**: Create `local.properties` in project root:
```properties
sdk.dir=/path/to/your/Android/Sdk
```

### JDK Version Mismatch

**Problem**: Build fails with Java version errors

**Solution**: Verify JDK 21 is being used:
```bash
./gradlew --version

# Or set explicitly in gradle.properties
echo "org.gradle.java.home=/path/to/jdk-21" >> gradle.properties
```

### ProGuard/R8 Issues

**Problem**: Release build crashes with obfuscation errors

**Solution**: Check ProGuard rules in:
- `sourcesync-sdk-ui/proguard-rules.pro`
- `sourcesync-sdk-ui/consumer-rules.pro`

### DivKit Rendering Issues

**Problem**: Content not displaying correctly

**Solution**:
1. Enable visual errors in ActivationConfig:
   ```kotlin
   ActivationConfig.Builder(context)
       .setVisualErrorsEnabled(true)
       .build()
   ```
2. Check Logcat for DivKit error messages

## IDE Configuration

### Recommended Android Studio Plugins

- **Kotlin** (built-in)
- **JSON Viewer** - for template inspection
- **ADB Idea** - for quick device commands
- **Rainbow Brackets** - for better code readability

### Code Style

The project uses standard Kotlin/Android code style. Import the code style settings:

1. **File → Settings → Editor → Code Style**
2. Click gear icon → **Import Scheme**
3. Select Kotlin or Android style

### Logcat Filters

Add these filters for debugging:

```
# SDK UI logs
tag:ActivationView OR tag:ActivationConfig OR tag:CustomUrlHandler

# DivKit logs
tag:Div2View OR tag:DivKit
```