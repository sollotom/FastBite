plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Firebase Google Services

    id("org.jetbrains.kotlin.plugin.compose") // версия берётся автоматически, не указывай вручную
}

android {
    namespace = "com.example.fastbite"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fastbite"
        minSdk = 24
        targetSdk = 35
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // больше не нужно указывать kotlinCompilerExtensionVersion
    }
}

dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.7.2")

    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // --- Compose UI ---
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.7.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- Tests ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.2")
}
