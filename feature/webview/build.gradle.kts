plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.webview"

dependencies {
    implementation(libs.androidx.webkit)
    /**
     * core module
     */
    implementation(projects.core.interfaces)
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.base)
    implementation(projects.core.theme)
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)
    implementation(projects.feature.common)

    /**
     * core module
     */
    implementation(projects.feature.login)
    implementation(projects.feature.pdfTools)

    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    // Coil SVG decoder for mermaid SVG rendering
    implementation(libs.coilSvg)
    implementation(libs.coil.compose)



}
