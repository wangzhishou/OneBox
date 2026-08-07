plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.profile"

dependencies {
    api(project(":core:base"))
    api(project(":core:model"))
    api(project(":core:network"))
    implementation(projects.core.pay)
    implementation(projects.core.ui)
    implementation(projects.core.database)
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

    implementation(projects.feature.settings)
    implementation(projects.feature.common)
    implementation(projects.feature.login)
    implementation(projects.feature.webview)
}