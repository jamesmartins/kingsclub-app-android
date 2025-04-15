plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

/**
 * KINGSCLUB APP VERSION 1.0 - Hybrid version (middle native)
 * INTRO NATIVE VIEW
 * LOGIN NATIVE VIEW
 * WEBVIEW ACTIVITY LOADING PAGES
 * Gradle 8.10.2
 * Gradle android x.x.x
 * Kotlin 1.9.24
 * URL DEFAULT: https://adm.bunkerapp.com.br/app/intro.do?key=0keurq3V0gU¢
 * FEATURES: user data saved by url handle, recognize by Biometrec methods on login authenticating
 */

android {
    namespace = "br.com.android.kingsclubapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.android.kingsclubapp"
        minSdk = 26
        targetSdk = 35
        /*1.0.0 version with js webinterface/
            version with permission post notifications and v>=13
            version with new Firebase/OneSignal integrations
            version with new Firebase/OneSignal integrations
            was the version with more native parts
            was the last version with biometria
            was the last version with OneSignal without location services
        */
        versionCode = 1
        versionName = "1.0"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle)
    implementation(libs.onesignal)
    implementation(libs.okhttp3)
    implementation(libs.androidx.biometric)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}