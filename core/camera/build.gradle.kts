plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.camera"

dependencies {
    implementation(projects.core.cameraApi)
    implementation(projects.core.settings)
    implementation(projects.core.ui)
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.activityCompose)
    implementation(libs.androidx.exifinterface)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.onebox.cropper)
    implementation(projects.core.r)
}
