plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

android {
    namespace = "io.sourcesync.sdk.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    
    signAllPublications()
    
    coordinates("io.sourcesync.ui", "sourcesync-sdk-ui-android", System.getenv("VERSION") ?: "1.0.0-SNAPSHOT")

    pom {
        name.set("SourceSync SDK UI Android")
        description.set("Android UI components for SourceSync SDK")
        inceptionYear.set("2025")
        url.set("https://github.com/Source-Digital/sourcesync-sdk-ui-android")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("sourcedigital")
                name.set("Source Digital")
                email.set("dev@sourcesync.io")
                organization.set("Source Digital")
                organizationUrl.set("https://sourcedigital.com/")
            }
        }
        scm {
            url.set("https://github.com/Source-Digital/sourcesync-sdk-ui-android")
            connection.set("scm:git:git://github.com/Source-Digital/sourcesync-sdk-ui-android.git")
            developerConnection.set("scm:git:ssh://github.com/Source-Digital/sourcesync-sdk-ui-android.git")
        }
    }
}
