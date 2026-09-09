plugins {
    alias(libs.plugins.cosmoswatch.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cosmoswatch.feature.apod"
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
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui.compose.material3)
    implementation(libs.androidx.media3.session)
    ksp(libs.androidx.room.compiler)

    testImplementation(project(":core:testing"))
}
