plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.feature)
}

android.namespace = "com.shifenmiao.common"

dependencies {
    implementation(projects.core.domain)
    implementation(projects.libs.twain)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    implementation(projects.core.theme)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.network)
    api(projects.core.tts)
    implementation(projects.core.ui)
    ksp(libs.dagger.hilt.compiler)

    implementation(projects.libs.opencvTools)

    implementation(libs.com.airbnb.lottie)

    // Coil 3
    api(libs.coil)
    api(libs.coil.compose)
}