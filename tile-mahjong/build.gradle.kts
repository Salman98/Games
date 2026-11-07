plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.salman98.games.tilemahjong"
    defaultConfig {
        applicationId = "com.techeliteclub.tilemahjong" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
