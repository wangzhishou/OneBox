plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.feature)
}

android.namespace = "com.wanbaohe.blog"

dependencies {

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)
    //Di
    implementation(libs.dagger.hilt.android)
    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    implementation(projects.core.ui)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.network)

    implementation(projects.feature.common)
    implementation(projects.feature.webview)


}