plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.imagegeneration"

dependencies {
    api(projects.core.r)
    api(projects.core.network)

    api(libs.kotlinx.coroutines.core)
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.gson)
    implementation(libs.androidx.security.crypto)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
