import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

// Settings shared by every game, so each game's build.gradle.kts only names itself.
subprojects {
    plugins.withId("com.android.application") {
        // Firebase (Google Sign-In + Firestore) turns on per game once its google-services.json is added.
        // Games without one still build; the sign-in row just stays hidden.
        if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
        extensions.configure<ApplicationExtension> {
            compileSdk = 36
            defaultConfig {
                minSdk = 24
                targetSdk = 36 // Play requires API 36 for new apps since 31 Aug 2026
            }
            buildFeatures.compose = true
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildTypes.getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            }
        }
        dependencies {
            "implementation"(project(":core")) // brings Compose, Material3 and AdMob
            "testImplementation"(libs.junit)
        }
    }
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig.minSdk = 24
            buildFeatures.compose = true
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
