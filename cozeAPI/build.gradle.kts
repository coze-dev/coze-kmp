import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.0.0"
    id("co.touchlab.skie") version "0.9.5"
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

signing {
    val keyId = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId")
    val key = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
    val password = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
    if (keyId != null && key != null && password != null) {
        useInMemoryPgpKeys(keyId, key, password)
    }
    sign(publishing.publications)
}

skie {
    analytics.enabled.set(false)
}

group = "com.coze.api"
version = "1.0.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CozeAPI"
            isStatic = true
            binaryOption("bundleId", "com.coze.api")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            api(libs.kotlinx.coroutines.core)
            api(libs.ktor.client.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.coze.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    
    configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            if (name == "androidRelease") {
                artifactId = "coze-api-android"
                version = "0.2.2"
                groupId = "com.coze"
            }
        }
    }
    
    pom {
        name = "Coze API Android Library"
        description = "An android library for Coze API written in Kotlin."
        inceptionYear = "2025"
        url = "https://github.com/coze-dev/coze-kmp/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "jsongo@qq.com"
                name = "lingyibin"
                url = "https://www.coze.com/"
            }
        }
        scm {
            url = "https://github.com/coze-dev/coze-kmp/"
            connection = "scm:git:git://github.com/coze-dev/coze-kmp.git"
            developerConnection = "scm:git:ssh://git@github.com/coze-dev/coze-kmp.git"
        }
    }
}