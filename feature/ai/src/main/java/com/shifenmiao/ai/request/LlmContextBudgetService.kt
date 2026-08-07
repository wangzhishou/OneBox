package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.unified.LlmContentPart
import com.shifenmiao.model.ai.unified.LlmMessage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 上下文预算服务。
 *
 * 云端长上下文（128K / 200K）可以无脑拼历史，端侧小模型（2K-8K）必须主动裁剪。
 * 该服务作为 Phase 1 DoD 引入，规则 1-4 必须在 MVP 上线：
 *  1. 总预算来自 model.effectiveContextWindow()
 *  2. 为输出预留 reservedOutputTokens
 *  3. 系统提示尽量保留
 *  4. 当前用户最后一轮消息必须保留
 *  5. 历史从近到远保留
 *  6. 不支持图片/文件时，提前丢弃多模态 part
 *  7. 超预算时给出可解释错误（由 Adapter 转 Error 事件）
 *
 * 注：当前实现使用字符近似（1 token ≈ 4 字符英文 / 1.5 字符中文），
 * 真实 token 数应在上游按 model tokenizer 计算。本服务仅做"不会超太多"的工程化裁剪，
 * 不替代 runtime 的硬限制。
 */
interface LlmContextBudgetService {
    suspend fun trimMessages(
        messages: List<LlmMessage>,
        contextWindowTokens: Int,
        reservedOutputTokens: Int,
        protocol: AiRequestProtocol,
        supportsVision: Boolean,
    ): List<LlmMessage>
}

@Singleton
class DefaultLlmContextBudgetService @Inject constructor() : LlmContextBudgetService {

    override suspend fun trimMessages(
        messages: List<LlmMessage>,
        contextWindowTokens: Int,
        reservedOutputTokens: Int,
        protocol: AiRequestProtocol,
        supportsVision: Boolean,
    ): List<LlmMessage> {
        if (contextWindowTokens <= 0) return messages

        val reservedForOutput = reservedOutputTokens.coerceAtLeast(0)
        val availableTokens = (contextWindowTokens - reservedForOutput).coerceAtLeast(256)

        // 不支持 vision 时把图片 part 替换为文本占位，避免静默丢图后语义断裂。
        val normalized = if (protocol == AiRequestProtocol.LOCAL_ON_DEVICE && !supportsVision) {
            messages.map { it.replaceNonTextWithPlaceholder() }
        } else {
            messages
        }

        val total = normalized.sumOf { approxTokens(it) }
        if (total <= availableTokens) return normalized

        val systemMessages = normalized.filter { it.role.equals("system", ignoreCase = true) }
        val lastUser = normalized.lastOrNull { it.role.equals("user", ignoreCase = true) }
        val middle = normalized.filter {
            !it.role.equals("system", ignoreCase = true) && it !== lastUser
        }

        val fixed = systemMessages + listOfNotNull(lastUser)
        val fixedTokens = fixed.sumOf { approxTokens(it) }
        var remaining = (availableTokens - fixedTokens).coerceAtLeast(0)
        if (remaining == 0) return fixed

        val keptMiddle = mutableListOf<LlmMessage>()
        for (message in middle.asReversed()) {
            val cost = approxTokens(message)
            when {
                cost <= remaining -> {
                    keptMiddle.add(0, message)
                    remaining -= cost
                }
                remaining > 0 -> {
                    // 复用 cost 计算结果：超预算部分按 remaining 截断。
                    keptMiddle.add(0, message.truncateTo(remaining))
                    remaining = 0
                    break
                }
                else -> break
            }
        }

        return fixed + keptMiddle
    }

    private fun approxTokens(message: LlmMessage, cap: Int = Int.MAX_VALUE): Int {
        val text = message.textContent()
        if (text.isEmpty()) return 0
        val chineseRatio = text.count { it.code in 0x4E00..0x9FFF }.toDouble() / text.length
        val avgCharPerToken = if (chineseRatio > 0.3) 1.5 else 4.0
        return (text.length / avgCharPerToken).toInt().coerceAtMost(cap)
    }

    private fun LlmMessage.truncateTo(maxTokens: Int): LlmMessage {
        val maxChars = (maxTokens * 4).coerceAtLeast(0)
        val newParts = parts.map { part ->
            when (part) {
                is LlmContentPart.Text -> LlmContentPart.Text(part.text.take(maxChars))
                else -> part
            }
        }
        return copy(parts = newParts)
    }

    private fun LlmMessage.replaceNonTextWithPlaceholder(): LlmMessage {
        if (parts.isEmpty()) return this
        val hasNonText = parts.any { it !is LlmContentPart.Text }
        if (!hasNonText) return this
        val replaced = parts.map { part ->
            when (part) {
                is LlmContentPart.Text -> part
                else -> LlmContentPart.Text("[图片]")
            }
        }
        return copy(parts = replaced)
    }
}
