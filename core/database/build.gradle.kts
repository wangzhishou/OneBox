plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.database"

// 在现有的plugins和dependencies之间添加此配置块
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    // 以下是额外的选项
    arg("room.generateKotlin", "true") // 生成Kotlin代码而非Java
    arg("room.warn.paging.access", "true") // 为分页查询添加警告
}

dependencies {

    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.storage)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
}