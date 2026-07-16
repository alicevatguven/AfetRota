

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.guven.acilrota"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.guven.acilrota"
        minSdk = 24
        targetSdk = 36
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
val camerax_version = "1.3.0-rc01"
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewpager2)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation("com.graphhopper:graphhopper-core:0.13.0")
    implementation("com.graphhopper:graphhopper-reader-osm:0.13.0")

    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation("org.mapsforge:mapsforge-core:0.19.0")
    implementation("org.mapsforge:mapsforge-map:0.19.0")
    implementation("org.mapsforge:mapsforge-map-android:0.19.0")
    implementation("org.mapsforge:mapsforge-map-reader:0.19.0")
    implementation("org.mapsforge:mapsforge-themes:0.19.0")
    implementation("net.sf.kxml:kxml2:2.3.0")

    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")

    implementation("org.mapsforge:mapsforge-themes:0.19.0")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.google.mlkit:object-detection:17.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}