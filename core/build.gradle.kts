plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android { namespace = "com.techeliteclub.game.core" }

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.compose.material3)
    api(libs.activity.compose)
    api(libs.play.services.ads)
    implementation(libs.ump)
    implementation(libs.play.review) // in-app "rate this app" popup
    implementation(libs.work.runtime) // daily reminder
}
