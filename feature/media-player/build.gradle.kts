plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.media"

dependencies {
    // 共用外壳（BaseScreen / 玻璃容器 / 底栏）
    implementation(projects.feature.common)

    // 最近访问记录（与文件浏览器共用同一张表）
    implementation(projects.core.database)

    // 播放内核：ExoPlayer + 后台播放的 MediaSessionService
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    implementation(libs.dagger.hilt.android)
    implementation(libs.coroutinesAndroid)
}
