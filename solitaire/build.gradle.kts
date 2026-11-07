plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.salman98.games.solitaire"
    defaultConfig {
        applicationId = "com.techeliteclub.solitaire" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
