plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.network"

dependencies {
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)
    /**
     * 事件总线
     */
    api(libs.org.greenrebot.eventbus)

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
     * Charset detection
     */
    api(libs.juniversalchardet)

    /**
     * HTML parsing
     */
    api(libs.jsoup)

    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)

    /**
     * 图片处理
     */
    api(libs.coil)
    api(libs.coil.compose)
    api(libs.coilGif)
    api(libs.coilSvg)
    api(libs.datastore.preferences.android)

    api(libs.avif.coder.coil) {
        exclude(module = "com.github.awxkee:avif-coder")
    }
    api(libs.avif.coder)
    api(libs.jxl.coder.coil) {
        exclude(module = "com.github.awxkee:jxl-coder")
    }
    api(libs.jxl.coder)
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:resources"))
    implementation(project(":core:settings"))
    implementation(project(":core:di"))
    implementation(project(":core:crash"))
    implementation(project(":core:r"))
    implementation(project(":core:base"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))
    implementation(project(":core:database"))
    implementation(project(":core:interfaces"))
    implementation(project(":core:storage"))
}
