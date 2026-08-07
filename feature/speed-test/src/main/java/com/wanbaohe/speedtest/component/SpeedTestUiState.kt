package com.wanbaohe.speedtest.component

import androidx.compose.runtime.Immutable
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.domain.SpeedTestRecord

/** 当前测速 UI 阶段 */
enum class SpeedTestStatus { IDLE, MEASURING, DONE }

@Immutable
data class SpeedTestUiState(
    val status: SpeedTestStatus = SpeedTestStatus.IDLE,
    /** 实时下载速度 (Mbps)，仅 MEASURING 时有意义 */
    val liveMbps: Float = 0f,
    /** 下载进度 0~1，仅 MEASURING 时有意义 */
    val progress: Float = 0f,
    /** 最终测速结果，仅 DONE 时非空 */
    val result: SpeedTestRecord? = null,
    /** 历史记录列表 */
    val history: List<SpeedTestRecord> = emptyList(),
    /** 当前网络类型文本 */
    val networkType: String = "",
    /** 当前激活的测速配置 */
    val config: SpeedTestConfig = SpeedTestConfig(),
    /** 所有配置列表 */
    val configList: List<SpeedTestConfig> = emptyList(),
    /** 错误提示 */
    val errorMsg: String? = null
)
