plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nick.myrecoverytracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nick.myrecoverytracker"
        minSdk = 24
        targetSdk = 34

        // 🔒 Freeze pilot version
        versionCode = 10000
        versionName = "1.0.0-pilot"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Disable release lint because AGP/Kotlin lint is crashing (KaModule error)
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    // ✅ Production signing (projectRoot/keystore/… is correct from app/)
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/myrecoverytracker_keystore.jks")
            storePassword = "Bowdo1904!!"
            keyAlias = "myrecoverytracker"
            keyPassword = "Bowdo1904!!"
        }
    }

    buildTypes {
        val redcapUrl = project.findProperty("REDCAP_URL") as String? ?: ""
        val redcapToken = project.findProperty("REDCAP_TOKEN") as String? ?: ""
        val redcapPid = project.findProperty("REDCAP_PROJECT_ID") as String? ?: ""

        debug {
            buildConfigField("String", "REDCAP_URL", "\"$redcapUrl\"")
            buildConfigField("String", "REDCAP_TOKEN", "\"$redcapToken\"")
            buildConfigField("String", "REDCAP_BASE_URL", "\"$redcapUrl\"")
            buildConfigField("String", "REDCAP_API_TOKEN", "\"$redcapToken\"")
            buildConfigField("String", "REDCAP_PROJECT_ID", "\"$redcapPid\"")
        }

        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "REDCAP_URL", "\"$redcapUrl\"")
            buildConfigField("String", "REDCAP_TOKEN", "\"$redcapToken\"")
            buildConfigField("String", "REDCAP_BASE_URL", "\"$redcapUrl\"")
            buildConfigField("String", "REDCAP_API_TOKEN", "\"$redcapToken\"")
            buildConfigField("String", "REDCAP_PROJECT_ID", "\"$redcapPid\"")
        }
    }

    buildFeatures { buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

apply(from = "qa-core.gradle.kts")