package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * messageFeedback 远程端点模型(DSH-PROTOCOL §9,对齐 Flutter feedback_store.dart)。
 *
 * - rating 枚举冻结事实:'positive' | 'negative'
 * - version 是不透明 CAS token(数字或字符串),原样保留回传 ifVersion
 * - list → {items:[...]};put → 更新后的条目(新 version);delete → {absent:true}(幂等)
 */

/** 评分 wire 字面量(封闭枚举,只认这两个) */
const val FeedbackRatingPositive = "positive"
const val FeedbackRatingNegative = "negative"

/** 备注长度上限(服务端策略 maxNoteBytes=8192;UI 本地预拒同值) */
const val FeedbackNoteMaxBytes = 8192

/** 一条反馈条目(list items 元素;put 成功也返回同形) */
data class FeedbackItem(
    val messageId: String,
    /** 'positive' | 'negative'(封闭枚举,解析时校验) */
    val rating: String,
    val note: String? = null,
    /** 不透明 CAS token(数字或字符串 JsonElement),原样回传 ifVersion */
    val version: JsonElement? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

/** 防御式解析:rating 非封闭枚举抛 IllegalArgumentException(防静默);缺字段给空值 */
fun parseFeedbackItem(element: JsonElement): FeedbackItem {
    val obj = element as? JsonObject
        ?: throw IllegalArgumentException("feedback item 不是对象")
    val rating = (obj["rating"] as? JsonPrimitive)?.contentOrNull
    if (rating != FeedbackRatingPositive && rating != FeedbackRatingNegative) {
        throw IllegalArgumentException("未知评分枚举: $rating")
    }
    return FeedbackItem(
        messageId = (obj["messageId"] as? JsonPrimitive)?.contentOrNull.orEmpty(),
        rating = rating,
        note = (obj["note"] as? JsonPrimitive)?.contentOrNull,
        version = obj["version"],
        createdAt = (obj["createdAt"] as? JsonPrimitive)?.contentOrNull.orEmpty(),
        updatedAt = (obj["updatedAt"] as? JsonPrimitive)?.contentOrNull.orEmpty()
    )
}
