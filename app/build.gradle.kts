plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.moneysav.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.moneysav.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // R8 men-strip ribuan ikon tak terpakai dari material-icons-extended
            // dan tree-shake miuix/compose -> APK jauh lebih kecil.
            isMinifyEnabled = true
            isShrinkResources = true
            // Ditandatangani debug key agar bisa diinstall langsung dari artifact.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // ── miuix: komponen & tema MIUI / HyperOS ──
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.4")

    // ── Liquid Glass asli (blur + lens refraction) ──
    implementation("io.github.kyant0:backdrop-android:2.0.1")

    // ── Compose / Material3 (versi sejalan dengan miuix 0.9.4) ──
    implementation("androidx.compose.material3:material3:1.5.0-alpha22")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.core:core-ktx:1.17.0")

    // ── MVVM ──
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    // ── Room (KSP) ──
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    testImplementation("junit:junit:4.13.2")
}
