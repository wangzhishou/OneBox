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
    // 官方 maven 构件(F-Droid 合规: 源码树不得携带预编译二进制).
    // 之前用本地裁剪版 AAR(约省 8MB .so), 为 F-Droid 收录换回官方版.
    implementation(libs.onnxruntime.android)
    implementation(libs.kotlinx.serialization.json)
    "arm64Api"(libs.com.tencent.mmkv)
}
