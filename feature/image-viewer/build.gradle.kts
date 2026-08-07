plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.imageviewer"

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.network)
    
    // Coil 3
    api(libs.coil)
    api(libs.coil.compose)
    api(libs.coilGif)
    api(libs.coilSvg)
    api(libs.coil.network)
    api(libs.ktor)

    // Common
    implementation(projects.feature.common)

    // 依赖注入
    ksp(libs.dagger.hilt.compiler)

}
