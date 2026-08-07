plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.altitude"

dependencies {

    api(projects.core.weather)
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)
    api(libs.gson)
    api(libs.eddsa)
    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.database)
    api(projects.feature.common)
}
