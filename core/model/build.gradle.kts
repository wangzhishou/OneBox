plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.model"

dependencies {
    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)
    api(libs.com.squareup.retrofit2.converter.gson) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(libs.com.squareup.okhttp3.okhttp)
    api(projects.core.r)
    api(projects.core.resources)
    api(projects.core.interfaces)

    api(libs.com.tencent.opensdk)
    api(libs.kotlinx.coroutines.core)
}