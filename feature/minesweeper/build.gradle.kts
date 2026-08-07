plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.minesweeper"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.feature.common)
}