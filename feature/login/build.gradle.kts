plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.login"

// GMS Google 登录仅 google 渠道可用 (同 core/r 的 flavor 隔离范式):
//   - Google 渠道:        src/main + src/google (真实 rememberGoogleSignInAction) + play-services-auth
//   - 国内渠道 + foss:    src/main + src/nogms (同签名 stub), 不携带 GMS
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "foss").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nogms/java")
        }
    }
}

dependencies {
    /**
     * 模块依赖
     */
    api(project(":core:base"))
    api(project(":core:model"))
    api(project(":core:theme"))
    api(projects.core.network)
    api(projects.feature.common)
    api(projects.feature.wechat)
    api(libs.org.greenrebot.eventbus)
    // GMS 登录仅 google 渠道
    "googleApi"(libs.play.services.auth)
}
