plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.salman98.games.colorsort"
    defaultConfig {
        applicationId = "com.techeliteclub.colorsort" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
