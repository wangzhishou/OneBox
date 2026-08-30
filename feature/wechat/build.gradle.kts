plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.app.wxapi"

// 微信 OpenSDK 为专有依赖, foss (F-Droid) 渠道不打包:
//   - 非 foss 渠道: src/main + src/nonfoss (WXEntryActivity / WXPayEntryActivity 回调入口) + SDK
//   - foss 渠道:    无微信回调 Activity, app 模块 src/foss/AndroidManifest.xml 同步移除清单声明
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonfoss/java")
        }
    }
}

dependencies {
    api(project(":core:model"))
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    // 专有 SDK 按 flavor 注入, foss 变体不携带
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
        add("${flavor}Api", libs.com.tencent.opensdk.get())
    }
    api(libs.org.greenrebot.eventbus)
}
