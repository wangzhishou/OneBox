plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.calendar"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.lunar.java)
    implementation(libs.androidx.room.runtime)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.feature.common)
    implementation(projects.feature.ai)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.kotlinx.serialization.json)
}

