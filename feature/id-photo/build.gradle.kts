plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.wanbaohe.idphoto"
}

dependencies {
    // 项目模块依赖
    implementation(projects.core.base)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.di)
    implementation(projects.core.domain)
    implementation(projects.core.resources)
    implementation(projects.core.settings)
    implementation(projects.core.ui)
    implementation(projects.feature.common)

    // 裁剪相关依赖
    implementation(libs.onebox.cropper)
    implementation(projects.feature.crop)


    ksp(libs.dagger.hilt.compiler)
}

