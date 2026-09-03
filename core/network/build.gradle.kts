import java.util.Properties

plugins {
    alias(libs.plugins.cosmoswatch.android.library)
    alias(libs.plugins.cosmoswatch.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cosmoswatch.core.network"

    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties().apply {
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use(::load)
        }
    }

    defaultConfig {
        buildConfigField(
            "String",
            "NASA_API_KEY",
            "\"${localProperties.getProperty("NASA_API_KEY", "DEMO_KEY")}\"",
        )
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
}
