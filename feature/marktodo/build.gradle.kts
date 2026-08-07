plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.marktodo"

dependencies {
    // Feature common
    implementation(projects.feature.common)
    implementation(project(":feature:schedule"))
}
