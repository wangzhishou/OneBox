package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable

/**
 * skill 域 wire 模型(DSH-PROTOCOL §5)。
 *
 * skill 无专线调用:目录由 skill.list 拉取(sessionId 必填,主机从会话头解析项目根目录),
 * 调用 = 内容恰好是单个 "/" 开头文本块的普通 prompt。
 */

/** 单个技能条目;[modelInvocable] = 是否允许模型自主调用 */
@Serializable
data class SkillEntry(
    val name: String,
    val description: String,
    val whenToUse: String? = null,
    val modelInvocable: Boolean
)

/** skill.list 的响应 value */
@Serializable
data class SkillListValue(
    val skills: List<SkillEntry>
)
