plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.feature)
}

android.namespace = "com.wanbaohe.visual.automation"

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.base)
    implementation(projects.core.network)
    implementation(projects.feature.common)

    ksp(libs.dagger.hilt.compiler)
}
