plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.a2ui.workbench"

dependencies {
    implementation(projects.core.a2ui)
    implementation(projects.feature.common)
    implementation(libs.kotlinx.serialization.json)
}
