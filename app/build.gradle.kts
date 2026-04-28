/*
 * Copyright 2026 Reccmost
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.xiaopeng.xposed.instrument.theme"
    compileSdk {
        version = release(version = libs.versions.sdkCompile.get().toInt())
    }
    defaultConfig {
        applicationId = "com.xiaopeng.xposed.instrument.theme"
        minSdk = libs.versions.sdkMin.get().toInt()
        targetSdk = libs.versions.sdkTarget.get().toInt()
        versionCode = rootProject.extra["BuildVersionCode"] as Int
        versionName = rootProject.extra["BuildVersionName"] as String
        multiDexEnabled = false
        base.archivesName = "XPluginInstrumentTheme-$versionName-$versionCode"

        buildConfigField(type = "Boolean", name = "IS_RUNNING_TEST_PLATFORM", value = """Boolean.parseBoolean("${rootProject.extra["BuildConfigIsRunningTestPlatform"]}")""")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = false
        buildConfig = true
    }
    packaging {
        resources.excludes.add("kotlin/**")
        resources.excludes.add("kotlin-tooling-metadata.json")
        resources.excludes.add("DebugProbesKt.bin")
        resources.excludes.add("META-INF/README.txt")
        resources.excludes.add("META-INF/com/android/**")
        resources.excludes.add("META-INF/version-control-info.textproto")
    }
    androidResources {
        additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x66")
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("app.jks")
            storePassword = "123456"
            keyAlias = "debug"
            keyPassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
        create("release") {
            storeFile = file("app.jks")
            storePassword = "123456"
            keyAlias = "release"
            keyPassword = rootProject.extra["BuildReleasePassword"] as String
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro"), file("proguard-log.pro"))
            isShrinkResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.common.joor)
    testImplementation("junit:junit:4.13.2")

    compileOnly(project(":stub"))
    compileOnly(libs.androidx.core.ktx)
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.androidx.constraint)
    compileOnly(libs.androidx.recyclerview)

    compileOnly(libs.xpeng.car)
    compileOnly(libs.xpeng.xui)
    compileOnly(libs.xpeng.xui.manager)

    compileOnly(libs.common.xposed)
    compileOnly(libs.common.framework)
}
