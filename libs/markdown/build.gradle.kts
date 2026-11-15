plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.markdown"

dependencies {
    implementation(libs.hrm.markdown.renderer)
    implementation(libs.hrm.codehigh.render)
    implementation(libs.hrm.latex.renderer)
    implementation(libs.hrm.latex.fonts.katex)
    implementation(projects.libs.diagram)
    implementation(libs.bundles.compose)
    implementation(projects.core.ui)
}
