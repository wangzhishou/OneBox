package com.shifenmiao.model.ai.tool

/**
 * 工具风险等级：
 * - SAFE: 可自动启用和执行
 * - SENSITIVE: 可推荐，但执行前可能需要进一步确认
 * - DANGEROUS: 默认不自动启用，必须显式授权
 */
enum class ToolRiskLevel {
    SAFE,
    SENSITIVE,
    DANGEROUS
}
