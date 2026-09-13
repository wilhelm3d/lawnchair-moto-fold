plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "com.android.launcher.foldchair"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.android.launcher.foldchair"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android - Latest stable versions for Android 17
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.12.0")
    
    // Preferences & DataStore (Modern alternative to SharedPreferences)
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Coroutines - Latest
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    
    // Lifecycle - Latest stable
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    
    // Window Manager - For foldable device detection (Android 17)
    implementation("androidx.window:window:1.3.0")
    
    // Activity - Latest
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.activity:activity-compose:1.9.1")
    
    // Fragment - Latest
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    
    // Kotlin Standard Library - Latest
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    
    // Additional modern libraries
    implementation("androidx.annotation:annotation:1.8.0")
    implementation("androidx.collection:collection-ktx:1.4.2")
}
