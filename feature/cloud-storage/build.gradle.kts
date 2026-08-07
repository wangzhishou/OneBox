plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.cloud.storage"

dependencies {
    implementation(projects.core.base)
    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.feature.common)

    implementation(libs.androidx.security.crypto)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.hierynomus.smbj)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)
}
