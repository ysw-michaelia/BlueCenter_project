// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.42")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) version "2.0.10" apply false
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    alias(libs.plugins.compose.compiler) apply false
}




