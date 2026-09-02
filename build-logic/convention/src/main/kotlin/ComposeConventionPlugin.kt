package com.cosmoswatch.buildlogic.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Adds Jetpack Compose to an Android library module: the Kotlin Compose compiler plugin,
 * `buildFeatures.compose`, and the BOM-managed core UI dependencies.
 */
class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            val bom = libs.findLibrary("androidx-compose-bom").get()
            dependencies {
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
