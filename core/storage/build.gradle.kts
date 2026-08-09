plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.empty"

dependencies {

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)

    implementation(libs.datastore.preferences.android)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(projects.core.utils)

    api(libs.toolbox.logger)

    implementation(libs.org.greenrebot.eventbus)
}