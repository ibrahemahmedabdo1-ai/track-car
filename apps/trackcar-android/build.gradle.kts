plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.trackcar.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.trackcar.app"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }
    buildFeatures { compose = true }
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(System.getenv("TRACKCAR_KEYSTORE_PATH") ?: "release.keystore")
            storePassword = System.getenv("TRACKCAR_KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("TRACKCAR_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("TRACKCAR_KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    sourceSets.getByName("main").java.srcDirs(
        "src/main/java",
        "../admin-android/src/main/java",
        "../agent-android/src/main/java",
        "../installer-android/src/main/java"
    )
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
}