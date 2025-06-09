plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.controlasistencias"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.controlasistencias"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)

    // recyclerview
    implementation(libs.recyclerview)
    // ZXing para escaneo QR
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)
    // retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}