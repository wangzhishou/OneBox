plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.devicestatus"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)

    implementation(projects.core.base)
    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.feature.common)
}
