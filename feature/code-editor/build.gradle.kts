plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.code.editor"

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.resources)
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.feature.common)
    implementation(projects.feature.webview)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.webkit)
}
