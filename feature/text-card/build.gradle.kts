plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.textcard"

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.imageGeneration)
    implementation(projects.feature.common)
}
