plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.adwatch"

// 激励广告 SDK 按渠道隔离 (同 feature/login、core/pay 的 flavor 隔离范式):
//   - google 渠道:   src/main + src/google (真实 GmsRewardedAdController, AdMob) + play-services-ads
//   - 国内 6 渠道:   src/main + src/domestic (真实 GmRewardedAdController, GroMore 聚合, 内含穿山甲) + mediation-sdk
//   - foss 渠道:     src/main + src/nogms (同签名 NoopRewardedAdController stub), 不携带广告 SDK
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
        getByName("foss").kotlin.srcDir("src/nogms/java")
    }
}

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.storage)
    api(projects.feature.common)

    // MMKV (by ABI, 每日观看计数持久化)
    "arm64Api"(libs.com.tencent.mmkv)

    // AdMob 激励广告仅 google 渠道, GroMore 聚合激励广告仅国内 6 渠道, foss 不携带广告 SDK
    "googleApi"(libs.play.services.ads)
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei").forEach { flavor ->
        add("${flavor}Api", libs.gromore.mediation.sdk)
    }
}
