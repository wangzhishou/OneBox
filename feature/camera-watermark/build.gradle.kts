plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.camera.watermark"

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.theme)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.ui)
    implementation(projects.core.storage)
    implementation(projects.feature.common)

    // ExifInterface for reading image metadata
    implementation(libs.androidx.exifinterface)
    ksp(libs.dagger.hilt.compiler)
}

