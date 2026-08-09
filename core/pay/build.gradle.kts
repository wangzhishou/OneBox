plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.pay"

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(projects.core.r)
    api(projects.core.model)
    api(projects.core.base)
    api(libs.alipaysdk.android)
    api(libs.com.tencent.opensdk)
    api(libs.com.android.billingclient.billing.ktx)
    api(libs.kotlinx.coroutines.core)
    api(libs.org.greenrebot.eventbus)
}

