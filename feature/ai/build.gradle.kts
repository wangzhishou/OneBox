plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.ai"

dependencies {

    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.a2ui)
    implementation(projects.core.interfaces)
    implementation(projects.core.imageGeneration)
    implementation(projects.feature.common)
    implementation(projects.feature.cloudStorage)
    implementation(projects.feature.codeEditor)
    implementation(projects.feature.login)
    implementation(projects.feature.markdownEdit)
    implementation(projects.feature.webview)
    implementation(projects.feature.base64Tools)
    implementation(projects.feature.webpTools)
    implementation(projects.feature.checksumTools)
    implementation(projects.feature.scanQrCode)
    implementation(projects.feature.fileBrowser)
    implementation(projects.feature.bookkeeping)
    implementation(projects.feature.limitsResize)
    implementation(projects.feature.weightResize)
    implementation(projects.feature.pdfTools)
    implementation(projects.feature.visualAutomation)


    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    /**
     * 发起网络请求
     */

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.com.squareup.okhttp3.okhttp)

    /**
     * 图片处理
     */
    implementation(libs.avif.coder)
    api(libs.toolbox.logger)
    implementation(libs.kotlinx.coroutines.core)




    /**
     * markdown
     */
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)
}
