package com.cosmoswatch.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Adds Hilt DI to an Android library module: the Hilt Gradle plugin, KSP for annotation
 * processing, and the `hilt-android` runtime dependency.
 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("ksp").get().get().pluginId)
            pluginManager.apply(libs.findPlugin("hilt").get().get().pluginId)

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
