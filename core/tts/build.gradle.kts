plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.tts"

dependencies {
    api(projects.core.model)
    api(projects.core.r)
    api(projects.core.interfaces)
    api(projects.core.network)
    api(projects.core.database)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.com.squareup.okhttp3.okhttp)
}
