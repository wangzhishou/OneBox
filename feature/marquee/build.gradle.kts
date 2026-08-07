plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.marquee"

dependencies {
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)
    api(projects.core.theme)
    api(projects.core.r)
    api(projects.core.ui)
    api(projects.core.model)
    api(projects.core.storage)

    api(projects.feature.common)
}