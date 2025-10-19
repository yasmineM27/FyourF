// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "yasminemassaoudi.grp3.fyourf"
    compileSdk = 33

    defaultConfig {
        applicationId = "yasminemassaoudi.grp3.fyourf"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.core:core:1.10.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.auth.api.impl)

}
