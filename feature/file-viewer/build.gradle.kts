plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.fileviewer"

dependencies {
    // 共用外壳（BaseScreen / 玻璃容器 / 统一顶栏）
    implementation(projects.feature.common)

    // WebViewAssetLoader + WebMessagePortCompat 所在的兼容库
    implementation(libs.androidx.webkit)

    implementation(libs.dagger.hilt.android)
    implementation(libs.coroutinesAndroid)
}
