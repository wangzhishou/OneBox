plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.search"

dependencies {

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    implementation(projects.core.ui)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)

    implementation(projects.feature.common)
    implementation(projects.feature.online)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
}