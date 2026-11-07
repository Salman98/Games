plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.techeliteclub.merge2048"
    defaultConfig {
        applicationId = "com.techeliteclub.merge2048" // cannot change after the first Play upload
        versionCode = 1
        versionName = "1.0"
    }
}
