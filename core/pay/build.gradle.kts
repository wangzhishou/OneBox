plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.pay"

// 支付宝 / 微信 / Play Billing 均为专有 SDK, 按 flavor 隔离 (同 core/r 的范式):
//   - 非 foss 渠道:        src/main + src/nonfoss (真实 Alipay) + 微信/支付宝 SDK
//   - google 渠道:         额外 src/google (真实 GooglePlayBilling) + Play Billing SDK
//   - 国内 6 渠道 + foss:  额外 src/nonbilling (GooglePlayBilling 同签名 stub)
//   - foss 渠道:           src/foss (Alipay no-op stub), 不携带任何支付 SDK
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonfoss/java")
        }
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "foss").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonbilling/java")
        }
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(projects.core.r)
    api(projects.core.model)
    api(projects.core.base)

    // 专有支付 SDK 按 flavor 注入, foss 变体不携带
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
        add("${flavor}Api", libs.alipaysdk.android.get())
        add("${flavor}Api", libs.com.tencent.opensdk.get())
    }
    // Play Billing 仅 google 渠道使用
    "googleApi"(libs.com.android.billingclient.billing.ktx)

    api(libs.kotlinx.coroutines.core)
    api(libs.org.greenrebot.eventbus)
}
