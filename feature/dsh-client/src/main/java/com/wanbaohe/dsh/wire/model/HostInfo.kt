package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable

/**
 * `host.describe` 的响应 value:就绪探针 + 能力面。
 * provider/model 在主机未配置模型时缺席。
 */
@Serializable
data class HostInfo(
    val version: String,
    val cwd: String,
    val provider: String? = null,
    val model: String? = null,
    val attachedSessions: Int,
    val canOpenPath: Boolean
)
