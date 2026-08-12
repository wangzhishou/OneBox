plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.storage"

dependencies {

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
}