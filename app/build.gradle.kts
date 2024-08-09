//plugins {
////    alias(libs.plugins.android.application)
////    alias(libs.plugins.jetbrains.kotlin.android)
////    alias(libs.plugins.jetbrains.kotlin.kapt)
//    id 'com.android.application'
//
//}
plugins {
    id("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.kapt")
    id ("com.google.gms.google-services") // Add this line
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}
android {
    namespace = "com.sunil.dhwarehouse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sunil.dhwarehouse"
        minSdk = 26
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.databinding.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.poi:poi:5.2.5")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.airbnb.android:lottie:6.4.1")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-storage")
    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}