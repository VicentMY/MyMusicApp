plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Firebase
    id("com.google.gms.google-services")
    // Room
    id("kotlin-kapt")
}

android {
    namespace = "es.vmy.musicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "es.vmy.musicapp"
        minSdkVersion(rootProject.extra["defaultMinSdkVersion"] as Int)
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
    }

}

dependencies {

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    //

    // Room components
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    //

    // MOD
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.media:media:1.7.0")
    //

    // Animation
    val dynamicanimation_version = "1.0.0"
    implementation("androidx.dynamicanimation:dynamicanimation:$dynamicanimation_version")
    //

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}