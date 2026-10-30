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
    implementation(libs.onnxruntime.android)
}
