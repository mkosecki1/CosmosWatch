plugins {
    alias(libs.plugins.cosmoswatch.android.library)
    alias(libs.plugins.cosmoswatch.hilt)
}

android {
    namespace = "com.cosmoswatch.core.database"
}

dependencies {
    api(libs.androidx.sqlite)
    implementation(libs.androidx.core.ktx)
    implementation(libs.sqlcipher.android)
}
