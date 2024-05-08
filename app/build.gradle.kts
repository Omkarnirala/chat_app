plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.omkar.chatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.omkar.chatapp"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.2")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")

    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Coroutine Image Loading
    implementation ("io.coil-kt:coil:1.4.0")
    implementation ("io.coil-kt:coil-gif:1.1.1")

    //get logs of your app on top of the Android default logs using Timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    //Logger provides better readability for log messages, especially for JSON content or large amounts of data
    implementation ("com.orhanobut:logger:2.2.0")

//    implementation ("com.google.mlkit:barcode-scanning:16.0.3")
//    val camerax_version = "1.0.0-beta10"
//    implementation ("androidx.camera:camera-core:${camerax_version}")
//    implementation ("androidx.camera:camera-camera2:${camerax_version}")
//    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
//    implementation ("androidx.camera:camera-view:1.0.0-alpha10")
//    implementation ("androidx.camera:camera-extensions:1.0.0-alpha10")

//    implementation ("androidx.camera:camera-core:1.2.0-alpha01")
//    implementation ("androidx.camera:camera-camera2:1.2.0-alpha01")
//    implementation ("androidx.camera:camera-lifecycle:1.2.0-alpha01")
//    implementation ("androidx.camera:camera-view:1.2.0-alpha01")
//
//    implementation ("com.github.dhaval2404:imagepicker:2.1")
}