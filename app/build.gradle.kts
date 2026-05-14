plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "kr.ac.kopo.dodaynote_2"
    compileSdk = 36

    defaultConfig {
        applicationId = "kr.ac.kopo.dodaynote"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    //implementation(libs.constraintlayout)
    //implementation(libs.material)
    implementation(libs.activity)

    // 아래 두 라이브러리를 constraintLayout 위해 추가
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-beta01")
    implementation("com.google.android.material:material:1.11.0") // FAB 사용을 위해 필요

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}