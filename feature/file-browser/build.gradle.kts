plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.file.browser"

dependencies {
    // Core dependencies
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.utils)
    implementation(projects.core.domain)

    // Feature dependencies
    implementation(projects.feature.common)

    // Dependency Injection
    implementation(libs.dagger.hilt.android)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.coroutinesAndroid)
}

