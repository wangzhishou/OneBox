plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.wanbaohe.dynamicui"

dependencies {
    // JSON parsing
    api(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.coroutinesAndroid)
    implementation(libs.kotlinx.coroutines.core)

    // Coil (AsyncImage)
    implementation(libs.coil.compose)

    // Lottie
    implementation(libs.com.airbnb.lottie)

    // OkHttp (HttpAction)
    implementation(libs.com.squareup.okhttp3.okhttp)

    // Material3
    implementation(libs.androidx.material3)

    // IconRegistry from core/ui for efficient icon resolution
    implementation(projects.core.ui)
}
