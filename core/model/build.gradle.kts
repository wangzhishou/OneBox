plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.shifenmiao.model"

// 微信 OpenSDK 为专有依赖, foss (F-Droid) 渠道不打包:
//   - 非 foss 渠道: src/main + src/nonfoss (真实 Wechat 对象) + SDK
//   - foss 渠道:    src/main + src/foss (同签名 no-op stub), 同 core/r 的 flavor 隔离范式
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonfoss/java")
        }
    }
}

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

    // 专有 SDK 按 flavor 注入, foss 变体不携带
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
        add("${flavor}Api", libs.com.tencent.opensdk.get())
    }
    api(libs.kotlinx.coroutines.core)
}