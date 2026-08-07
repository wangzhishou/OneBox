package com.wanbaohe.speedtest.domain

/** 测速历史领域模型（UI / 业务层使用，不依赖 Room） */
data class SpeedTestRecord(
    val id: Long = 0,
    /** 网络类型，如 "5G移动网络"、"WiFi" */
    val networkType: String,
    /** 下载速度 (Mbps) */
    val downloadMbps: Float,
    /** 网络延迟 (ms)，-1 表示测量失败 */
    val latencyMs: Int,
    /** 记录时间戳 (ms) */
    val recordedAt: Long = System.currentTimeMillis()
) {
    /** 以 MB/s 表示的下载速度 */
    val downloadMbPerSec: Float get() = downloadMbps / 8f

    /** 延迟显示文本 */
    val latencyDisplay: String get() = if (latencyMs >= 0) "${latencyMs}ms" else "--"
}

