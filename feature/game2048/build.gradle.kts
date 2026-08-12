plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.game2048"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.storage)
    api(projects.feature.common)

    // MMKV (按 ABI)
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)
}

