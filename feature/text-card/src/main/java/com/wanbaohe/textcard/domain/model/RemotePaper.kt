package com.wanbaohe.textcard.domain.model

/**
 * Strapi 远程配置的纸张背景(text-card-paper),图片已下载到本地。
 * 点击后等同自定义图背景(BackgroundSpec.Image,uri 用本地文件路径)。
 */
data class RemotePaper(
    val title: String,
    val localPath: String,
)
