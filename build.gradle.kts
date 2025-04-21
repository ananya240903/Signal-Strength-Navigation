// Top-level build.gradle.kts

// Plugin management using version catalogs
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2") // ✅ Move here if needed
    }
}


