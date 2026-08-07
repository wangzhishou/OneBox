plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
}
android.namespace = "com.halilibo.richtext"

dependencies {
    implementation(projects.core.resources)
    implementation(projects.core.theme)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.interfaces)
    implementation(libs.bundles.markwon) {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
    implementation(libs.bundles.compose)
    implementation(libs.icons.extended)
    implementation(libs.appCompat)
    implementation(libs.coil.compose)
    implementation(libs.coilGif)
    implementation(libs.jsoup)
    implementation(projects.libs.twain)
    implementation(libs.androidx.constraintlayout)
}