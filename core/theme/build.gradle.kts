plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.theme"

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:resources"))
}