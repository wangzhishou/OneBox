plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.passwordvault"

dependencies {
    implementation(projects.feature.common)
    implementation(projects.core.database)
    implementation(projects.core.theme)
    implementation(projects.core.base)
}
