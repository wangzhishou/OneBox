plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.translator"

dependencies {
    implementation(projects.feature.common)
    implementation(projects.feature.ai)
    implementation(projects.feature.onDeviceOcr)
    implementation(projects.core.tts)
    implementation(projects.core.database)
    implementation(projects.core.storage)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.airbnb.lottie)
    "arm64Api"(libs.com.tencent.mmkv)
}
