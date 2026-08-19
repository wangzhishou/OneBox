package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.extractEventText
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 轨迹视图纯客户端模型(对齐 Flutter trajectory_model.dart,DSH-PROTOCOL §9:
 * 轨迹视图零新 RPC —— 数据 = SessionLog 事件;loadOlder 即 session.history 分页)。
 *
 * 契约(纯函数、可重放:同输入必同输出,无外部状态):
 * - 输入乱序先按 seq 排序;turn/start·turn/end 分组
 * - 未闭合尾轮 = 进行中(endSeq == null);异常新轮出现时前轮按进行中收尾
 * - 每轮含 List<TrajectoryRow>:seq/type/角色标记/耗时(与同轮上一事件或
 *   turn/start 的 time 差,毫秒)/摘要行(截断 120)
 * - Between-turns 区段:轮外事件(compaction/start|end 等落在轮外、无主 turn/end)
 * - turn/start·turn/end 本身不进行(是分隔符,不产出行)
 */

/** 行角色标记(web trajectory ledger 同款分类的移动化子集) */
enum class TrajectoryRole { User, Assistant, Tool, Compaction, Retry, Error, Other }

/** 单行轨迹记录(轮内或轮外) */
data class TrajectoryRow(
    val seq: Int,
    val type: String,
    val role: TrajectoryRole,
    /** 原始事件时间(epoch 毫秒,与 SessionEvent.time 同源) */
    val time: Double,
    /** 与同轮上一事件(首行为 turn/start)的 time 差,毫秒;轮外行/无可参考时为 null */
    val durationMs: Double?,
    /** 摘要行(截断 120;检查器用 [TrajectoryExtractor.fullSummary] 取全文) */
    val summary: String,
    /** 来源事件(检查器原始 JSON 的出处) */
    val event: SessionEvent
)

/** 一个轮次(turn/start·turn/end 分组) */
data class TrajectoryTurn(
    val startSeq: Int,
    val startTime: Double,
    val rows: List<TrajectoryRow>,
    /** 闭合轮 = turn/end 的 seq;进行中轮为 null */
    val endSeq: Int? = null,
    val endTime: Double? = null
) {
    /** 未闭合尾轮 = 进行中 */
    val inProgress: Boolean get() = endSeq == null

    /** 轮耗时:闭合轮 = end-start;进行中轮 = 末行时间-start;空轮为 null */
    val durationMs: Double?
        get() = endTime?.let { it - startTime }
            ?: rows.lastOrNull()?.let { it.time - startTime }

    /** 轮尾 seq(闭合 = endSeq;进行中 = 末行 seq,空轮退化为 startSeq) */
    val endOrLastSeq: Int get() = endSeq ?: rows.lastOrNull()?.seq ?: startSeq
}

/** 轮外区段:连续落在所有轮次之外的轨迹行(compaction/start|end 等) */
data class TrajectoryBetween(
    val rows: List<TrajectoryRow>
)

/** 全局有序视图项:轮次或轮外区段(UI 直接消费,保持全局 seq 顺序) */
sealed class TrajectoryItem {
    data class TurnItem(val turn: TrajectoryTurn) : TrajectoryItem()
    data class BetweenItem(val between: TrajectoryBetween) : TrajectoryItem()
}

/** 提取结果:全局有序项 + 轮次表 + 轮外行表(后两者为便捷投影) */
data class TrajectoryView(
    val items: List<TrajectoryItem>,
    val turns: List<TrajectoryTurn>,
    val between: List<TrajectoryRow>
)

object TrajectoryExtractor {

    /** 摘要行截断上限 */
    const val SummaryMax = 120

    /** 核心入口:事件日志 → 全局有序视图 */
    fun extract(events: List<SessionEvent>): TrajectoryView {
        val sorted = events.sortedBy { it.seq }
        val items = ArrayList<TrajectoryItem>()
        val turns = ArrayList<TrajectoryTurn>()
        val between = ArrayList<TrajectoryRow>()

        var inTurn = false
        var openStartSeq = 0
        var openStartTime = 0.0
        val open = ArrayList<SessionEvent>() // 轮内事件(不含 turn/start|end)
        val pending = ArrayList<SessionEvent>() // 轮外事件缓冲

        fun flushPending() {
            if (pending.isEmpty()) return
            val rows = pending.map { row(it, null) }
            between.addAll(rows)
            items.add(TrajectoryItem.BetweenItem(TrajectoryBetween(rows)))
            pending.clear()
        }

        fun closeTurn(end: SessionEvent?) {
            val rows = ArrayList<TrajectoryRow>(open.size)
            var prevTime: Double? = openStartTime // 首行耗时相对 turn/start
            for (e in open) {
                rows.add(row(e, prevTime))
                prevTime = e.time
            }
            val turn = TrajectoryTurn(
                startSeq = openStartSeq,
                startTime = openStartTime,
                endSeq = end?.seq,
                endTime = end?.time,
                rows = rows
            )
            turns.add(turn)
            items.add(TrajectoryItem.TurnItem(turn))
            open.clear()
        }

        for (e in sorted) {
            when {
                e.type == "turn/start" -> {
                    if (inTurn) closeTurn(null) // 异常:前轮未闭合 → 按进行中收尾
                    flushPending() // 轮外缓冲先于新一轮落地,保持全局顺序
                    inTurn = true
                    openStartSeq = e.seq
                    openStartTime = e.time
                }

                e.type == "turn/end" -> {
                    if (inTurn) {
                        closeTurn(e)
                        inTurn = false
                    } else {
                        pending.add(e) // 无主 turn/end → 轮外
                    }
                }

                inTurn -> open.add(e)
                else -> pending.add(e)
            }
        }
        if (inTurn) closeTurn(null) // 未闭合尾轮 = 进行中
        flushPending()
        return TrajectoryView(items = items, turns = turns, between = between)
    }

    /** 角色标记:按事件类型前缀分类 */
    fun roleOf(type: String): TrajectoryRole = when {
        type.startsWith("user/") -> TrajectoryRole.User
        type.startsWith("assistant/") -> TrajectoryRole.Assistant
        type.startsWith("tool/") || type.startsWith("tool:") -> TrajectoryRole.Tool
        type.startsWith("compaction") -> TrajectoryRole.Compaction
        type.startsWith("llm/retry") -> TrajectoryRole.Retry
        type.startsWith("turn/error") -> TrajectoryRole.Error
        else -> TrajectoryRole.Other
    }

    /**
     * 完整摘要(不截断;检查器用)。优先复用事件文本提取;
     * 无文本时按已知键(summary/text/message/reason/error)回退,最后紧凑 JSON 兜底。
     */
    fun fullSummary(event: SessionEvent): String {
        val text = event.data.extractEventText().trim()
        if (text.isNotEmpty()) return text
        return fallbackText(event)
    }

    /** 摘要行(截断 [SummaryMax];换行折叠为单行) */
    fun summaryOf(event: SessionEvent, max: Int = SummaryMax): String =
        truncate(fullSummary(event), max)

    private fun fallbackText(event: SessionEvent): String {
        val data = event.data
        for (key in listOf("summary", "text", "message", "reason", "error")) {
            val v = data[key] ?: continue
            if (v is JsonPrimitive) {
                v.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            } else if (v is JsonObject) {
                // 嵌套错误/消息对象:{error:{message:...}} 等,解一层取可读文本
                for (inner in listOf("message", "text", "summary")) {
                    ((v[inner]) as? JsonPrimitive)?.contentOrNull?.trim()
                        ?.takeIf { it.isNotEmpty() }?.let { return it }
                }
            }
        }
        return data.toString()
    }

    private fun truncate(s: String, max: Int): String {
        val out = s.replace(Regex("\\s+"), " ").trim()
        return if (out.length <= max) out else out.take(max) + "…"
    }

    private fun row(e: SessionEvent, prevTime: Double?): TrajectoryRow = TrajectoryRow(
        seq = e.seq,
        type = e.type,
        role = roleOf(e.type),
        time = e.time,
        durationMs = prevTime?.let { e.time - it },
        summary = summaryOf(e),
        event = e
    )
}
