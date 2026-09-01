import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention for `feature:*` modules: Android library + Compose + Hilt + the shared testing
 * stack, plus the navigation/lifecycle dependencies every feature's presentation layer needs
 * (`@HiltViewModel`, its own `NavGraphBuilder` extension).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("cosmoswatch.android.library")
            pluginManager.apply("cosmoswatch.compose")
            pluginManager.apply("cosmoswatch.hilt")
            pluginManager.apply("cosmoswatch.testing")

            dependencies {
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
