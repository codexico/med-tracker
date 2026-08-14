import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.franciscokahil.appMeusRemedinhos"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.franciscokahil.appMeusRemedinhos"
        minSdk = 24
        targetSdk = 36
        versionCode = 15
        versionName = "4.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            val propertiesFile = project.rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                properties.load(propertiesFile.inputStream())
                val storeFilePath = properties.getProperty("RELEASE_STORE_FILE")
                if (storeFilePath != null) {
                    storeFile = rootProject.file(storeFilePath)
                }
                storePassword = properties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = properties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "37.0.0"
    ndkVersion = "27.1.12297006"
}

// Disable symbol stripping to resolve missing llvm-strip in the environment
tasks.withType<com.android.build.gradle.internal.tasks.StripDebugSymbolsTask>().configureEach {
    enabled = false
}
tasks.withType<com.android.build.gradle.internal.tasks.ExtractNativeDebugMetadataTask>().configureEach {
    enabled = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.work.runtime)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinxCoroutinesCore)
    androidTestImplementation(libs.kotlinxCoroutinesAndroid)
    androidTestImplementation(libs.kotlinxCoroutinesTest)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    val debugTree = project.layout.buildDirectory.dir("tmp/kotlin-classes/debug").map {
        fileTree(it) { exclude(fileFilter) }
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(debugTree)
    executionData.setFrom(project.layout.buildDirectory.dir("jacoco").map {
        fileTree(it) { include("testDebugUnitTest.exec") }
    })
}
