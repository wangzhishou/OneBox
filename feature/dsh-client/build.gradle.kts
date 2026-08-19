plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.dsh"

dependencies {
    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.feature.common)
    implementation(projects.core.network)
    implementation(projects.libs.richtext)
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.quickie.foss)
}
