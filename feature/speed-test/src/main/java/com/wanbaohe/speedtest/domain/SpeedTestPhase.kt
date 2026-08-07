package com.wanbaohe.speedtest.domain

/** 测速进度密封类，通过 Flow 向 Component 推送 */
sealed class SpeedTestPhase {
    /** 测量延迟中 */
    data object MeasuringLatency : SpeedTestPhase()

    /** 下载测速中，liveMbps = 实时速度，progress = 0~1 */
    data class Downloading(val liveMbps: Float, val progress: Float) : SpeedTestPhase()

    /** 测速完成 */
    data class Done(val record: SpeedTestRecord) : SpeedTestPhase()

    /** 测速出错 */
    data class Error(val message: String) : SpeedTestPhase()
}

