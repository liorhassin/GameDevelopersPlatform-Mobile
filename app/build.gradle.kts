plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gamedevelopersplatform"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gamedevelopersplatform"
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.v1110)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.firebase.auth.v2103)
    implementation (libs.firebase.firestore)


    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.firestore.v24100)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.common.java8)

    implementation(platform(libs.firebase.bom))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage")

    implementation (libs.picasso)

    implementation(libs.circleimageview)
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

}