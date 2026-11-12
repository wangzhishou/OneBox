plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.latex"

dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.hrm.latex.renderer)
    implementation(libs.hrm.latex.fonts.katex)
}
