plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.techeliteclub.game.carescape"
    defaultConfig {
        applicationId = "com.techeliteclub.game.carescape" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
