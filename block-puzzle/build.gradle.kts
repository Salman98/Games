plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.techeliteclub.game.blockpuzzle"
    defaultConfig {
        applicationId = "com.techeliteclub.game.blockpuzzle" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
