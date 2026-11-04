plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val releaseKeystore = providers.environmentVariable("OPEN_CANVAS_KEYSTORE").orNull
val releaseStorePassword = providers.environmentVariable("OPEN_CANVAS_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("OPEN_CANVAS_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("OPEN_CANVAS_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseKeystore,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }


android {
    namespace = "com.opencanvas.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.opencanvas.app"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.gradleProperty("versionCode").orElse("1000000").get().toInt()
        versionName = providers.gradleProperty("versionName").orElse("1.0.0").get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseKeystore))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.9.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.3")
    implementation("androidx.compose.foundation:foundation:1.9.3")
    implementation("androidx.compose.runtime:runtime-saveable:1.9.3")

    debugImplementation("androidx.compose.ui:ui-tooling:1.9.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
