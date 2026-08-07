plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.loancalculator"

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(projects.core.model)
    implementation(projects.core.resources)
    implementation(projects.core.theme)
    implementation(projects.feature.common)
}

