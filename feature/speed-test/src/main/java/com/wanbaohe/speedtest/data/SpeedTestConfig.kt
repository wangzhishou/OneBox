package com.wanbaohe.speedtest.data

/** 测速配置（运行时 + 持久化） */
data class SpeedTestConfig(
    val id: Long = 0,
    /** 配置名称，显示在主界面 */
    val name: String = "Cloudflare 50MB",
    /** 测速文件下载 URL */
    val testUrl: String = DEFAULT_TEST_URL,
    /** 预估消耗流量（MB），仅供界面展示 */
    val estimatedDataMb: Int = DEFAULT_ESTIMATED_MB,
    /** 最大测速时长（秒） */
    val durationSeconds: Int = DEFAULT_DURATION_SECONDS,
    /** 是否为内置预设（预设不允许删除） */
    val isPreset: Boolean = false,
    /** 是否为当前激活配置（DB 写入的 isActive 字段镜像） */
    val isActive: Boolean = false
) {
    companion object {
        const val DEFAULT_TEST_URL = "https://speed.cloudflare.com/__down?bytes=52428800"
        const val DEFAULT_ESTIMATED_MB = 50
        const val DEFAULT_DURATION_SECONDS = 15

        /** 内置预设配置列表（首次启动时写入数据库） */
        val DEFAULT_CONFIGS: List<SpeedTestConfig> = listOf(
            SpeedTestConfig(
                name = "Cloudflare 50MB",
                testUrl = "https://speed.cloudflare.com/__down?bytes=52428800",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            ),
            SpeedTestConfig(
                name = "阿里云镜像 50MB",
                testUrl = "https://mirrors.aliyun.com/kernel/v5.x/patch-5.15.176.xz",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            ),
            SpeedTestConfig(
                name = "腾讯CDN 50MB",
                testUrl = "https://dldir1.qq.com/weixin/Windows/WeChatSetup.exe",
                estimatedDataMb = 50,
                durationSeconds = 15,
                isPreset = true
            )
        )
    }
}
