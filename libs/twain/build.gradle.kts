plugins {
    alias(libs.plugins.image.toolbox.library)
}
android.namespace = "com.wanbaohe.twain"

dependencies {
    implementation(projects.core.resources)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(libs.bundles.markwon) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    implementation(libs.bundles.compose)
    implementation(libs.appCompat)
    implementation(libs.coil.compose)
    implementation(libs.coilGif)
    implementation(libs.jsoup)
    implementation(libs.androidx.constraintlayout)
}
