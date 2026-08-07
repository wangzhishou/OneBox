plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.file_transfer"

dependencies {
    // Hilt DI
    implementation(libs.dagger.hilt.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.squareup.retrofit2.converter.gson)

    // NanoHTTPD - 轻量级HTTP服务器
    implementation(libs.nanohttpd)
    implementation(libs.nanohttpd.websocket)

    // ZXing - 二维码生成
    implementation(libs.zxing.android.embedded )

    // Coil - 图片加载
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Core modules
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.database)

    // Feature common
    implementation(projects.feature.common)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Unit tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)

    // Instrumented tests (Room needs Android Context)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
