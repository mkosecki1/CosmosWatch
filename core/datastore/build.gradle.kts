plugins {
    alias(libs.plugins.cosmoswatch.android.library)
    alias(libs.plugins.cosmoswatch.hilt)
}

android {
    namespace = "com.cosmoswatch.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
}
