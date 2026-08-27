plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.ai.image"

dependencies {
    /**
     * 模块依赖
     */
    api(project(":core:base"))
    api(project(":core:model"))
    api(project(":core:theme"))
    api(projects.core.network)
    api(projects.core.imageGeneration)
    api(projects.feature.common)
    api(projects.feature.wechat)
    api(libs.org.greenrebot.eventbus)
}