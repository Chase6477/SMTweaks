plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.jr.smtweaks"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.jr.smtweaks"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.3.1"

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

    implementation(libs.gson)
    implementation(libs.commons.codec)

    implementation(libs.okhttp)

    implementation(libs.appcompat)
    implementation(libs.material)
}