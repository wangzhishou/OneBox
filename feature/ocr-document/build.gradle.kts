plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.feature.ocr.document"

dependencies {
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.ui)
    implementation(projects.core.base)
    implementation(projects.core.utils)
    implementation(projects.core.domain)
    implementation(projects.feature.common)
    implementation(projects.feature.webview)


    implementation(libs.dagger.hilt.android)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.compose.foundation.layout)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.com.squareup.retrofit2.retrofit)

    ksp(libs.dagger.hilt.compiler)
}
