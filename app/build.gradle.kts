plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.nick.myrecoverytracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nick.myrecoverytracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 10000
        versionName = "1.0.0-pilot"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/nickbowditch/AndroidStudioProjects/keystore/myrecoverytracker_keystore.jks")
            storePassword = "Bowdo1904!!"
            keyAlias = "myrecovery"
            keyPassword = "Bowdo1904!!"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        val redcapUrl = System.getProperty("REDCAP_URL") ?: project.findProperty("REDCAP_URL") as String? ?: ""
        val redcapToken = System.getProperty("REDCAP_TOKEN") ?: project.findProperty("REDCAP_TOKEN") as String? ?: ""
        val redcapPid = System.getProperty("REDCAP_PROJECT_ID") ?: project.findProperty("REDCAP_PROJECT_ID") as String? ?: ""

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

    packagingOptions {
        pickFirst("lib/arm64-v8a/libdatastore_shared_counter.so")
        pickFirst("lib/armeabi-v7a/libdatastore_shared_counter.so")
        pickFirst("lib/x86/libdatastore_shared_counter.so")
        pickFirst("lib/x86_64/libdatastore_shared_counter.so")
    }

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
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

apply(from = "qa-core.gradle.kts")