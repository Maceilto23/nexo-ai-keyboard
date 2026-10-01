plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nexoai.keyboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nexoai.keyboard"
        minSdk = 26
        targetSdk = 36
        versionCode = 30100
        versionName = "3.1.0"
    }

    signingConfigs {
        if (System.getenv("NEXO_KEYSTORE_PATH") != null) {
            create("release") {
                storeFile = file(System.getenv("NEXO_KEYSTORE_PATH"))
                storePassword = System.getenv("NEXO_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("NEXO_KEY_ALIAS")
                keyPassword = System.getenv("NEXO_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
