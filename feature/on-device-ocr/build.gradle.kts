plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.ocr"

dependencies {
    implementation(projects.libs.opencvTools)
    implementation(projects.feature.common)
    implementation(files("libs/onnxruntime-android-1.31.1-ppocrv6.aar"))
    implementation(libs.kotlinx.serialization.json)
    "arm64Api"(libs.com.tencent.mmkv)
}
