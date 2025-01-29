# SourceSync SDK UI

UI components for the SourceSync platform SDK.

## Building the SDK

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Git

### Development Setup
1. Clone the repository:
```bash
git clone https://github.com/yourusername/sourcesync-sdk-ui.git
cd sourcesync-sdk-ui
```

2. Open the project in Android Studio (optional, you do not need Android Studio to build the UI SDK):
- Launch Android Studio
- Select "Open an existing project"
- Navigate to the cloned directory and click OK

3. Build the SDK (from the root of the repo):
```bash
./gradlew clean sdk-ui:build
```

### Running the Demo App
1. Ensure the SDK is built successfully
2. Select the 'demo-app' configuration in Android Studio
3. Click the 'Run' button or press Shift+F10

### Running Tests
```bash
# Run unit tests
./gradlew sdk-ui:test

# Run instrumented tests
./gradlew sdk-ui:connectedAndroidTest
```

## Publishing the SDK

### Publishing to Maven Central
1. Set up your environment:
   - Create/configure your Sonatype OSSRH account
   - Set up GPG signing keys
   
2. Add your credentials to `local.properties`:
```properties
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_PASSWORD
signing.secretKeyRingFile=C:/Users/YourUsername/.gnupg/secring.gpg
mavenCentralUsername=YOUR_SONATYPE_USERNAME
mavenCentralPassword=YOUR_SONATYPE_PASSWORD
```

3. Publish:
```bash
./gradlew sdk-ui:publishToSonatype closeAndReleaseSonatypeStagingRepository
```

### Publishing to JitPack
1. Create a release on GitHub
2. JitPack will automatically build when someone requests your release

## Using the SDK

### Maven Central
Add to your app's `build.gradle`:
```groovy
dependencies {
    implementation 'io.sourcesync:sdk-ui:1.0.0'
}
```

### JitPack
1. Add JitPack repository to your root build.gradle:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency:
```groovy
dependencies {
    implementation 'com.github.yourusername:sourcesync-sdk-ui:1.0.0'
}
```

## Basic Usage
```java
JSONObject activation = new JSONObject();
activation.put("title", "My Title");

// Preview mode
ActivationView previewView = ActivationSDK.preview(context, activation, container);

// Detail mode
ActivationView detailView = ActivationSDK.detail(context, activation, container);
```

## License
Apache License 2.0