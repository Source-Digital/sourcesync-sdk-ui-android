plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
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

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.sourcesync.ui"
            artifactId = "sourcesync-sdk-ui-android"
            version = System.getenv("VERSION") ?: "1.0.0-SNAPSHOT"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("SourceSync SDK UI Android")
                description.set("Android UI components for SourceSync SDK")
                url.set("https://github.com/Source-Digital/sourcesync-sdk-ui-android")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("sourcedigital")
                        name.set("Source Digital")
                        email.set("dev@sourcesync.io")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Source-Digital/sourcesync-sdk-ui-android.git")
                    developerConnection.set("scm:git:ssh://github.com/Source-Digital/sourcesync-sdk-ui-android.git")
                    url.set("https://github.com/Source-Digital/sourcesync-sdk-ui-android")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    val signingKey = System.getenv("GPG_KEY_CONTENTS")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["release"])
}
