plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.demo"

dependencies {
    /**
     * mmkv - 根据不同架构使用不同的依赖
     */
    "arm64Api"(libs.com.tencent.mmkv)
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.com.squareup.retrofit2.converter.gson)

    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.network)


    implementation(projects.feature.common)

    // A2UI renderer gallery
    implementation(projects.core.a2ui)
}