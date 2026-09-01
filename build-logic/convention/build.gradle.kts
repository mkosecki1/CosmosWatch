plugins {
    `kotlin-dsl`
}

group = "com.cosmoswatch.buildlogic"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "cosmoswatch.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
