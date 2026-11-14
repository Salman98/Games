plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.techeliteclub.game.cryptogram"
    defaultConfig {
        applicationId = "com.techeliteclub.game.cryptogram" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
