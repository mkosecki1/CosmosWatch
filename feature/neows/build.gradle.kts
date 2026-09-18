plugins {
    alias(libs.plugins.cosmoswatch.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cosmoswatch.feature.neows"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.common)
    ksp(libs.androidx.room.compiler)
}
