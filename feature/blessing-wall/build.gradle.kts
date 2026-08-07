plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.blessingwall"

dependencies {
    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.database)
    api(projects.core.tts)
    api(projects.feature.common)
    implementation(libs.lunar.java)
}
