plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.decisionwheel"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.material)


    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.theme)
    api(projects.core.database)
    api(projects.feature.common)
    api(projects.feature.settings)
    api(projects.feature.webview)
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)

    /**
     * 数据库
     */
    api(libs.androidx.paging.compose)
    api(libs.androidx.room.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)

    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)

    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)

    /**
     * 图片处理
     */
    api(libs.avif.coder)
    api(libs.androidx.lifecycle)
    api(libs.androidx.lifecycle.runtime)
}