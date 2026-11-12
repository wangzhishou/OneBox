plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.diagram"

dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.hrm.diagram.render)
}
