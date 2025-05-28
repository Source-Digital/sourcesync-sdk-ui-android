import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.sourcesync.sdk.ui"
    compileSdk = 35

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
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.picasso)

    implementation(libs.div.core)
    implementation(libs.div.main)
    implementation(libs.div.json)

    testImplementation(libs.kotlin.test)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group.toString(), "sourcesync-sdk-ui-android", version.toString())
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
