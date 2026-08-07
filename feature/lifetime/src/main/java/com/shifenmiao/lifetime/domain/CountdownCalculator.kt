package com.shifenmiao.lifetime.domain

import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 倒数日计算：给定一组倒数日和当前日期，返回每条的 [CountdownStatus]。
 *
 * - 距离目标 0 天：标记 `isToday = true`
 * - 目标 < 今天：标记 `isPast = true`，`daysUntil` 取绝对值（仍展示）
 * - 进度：0~1，仅对未过期条目按「距离未来的密度」粗略计算，恒正整数
 */
@Singleton
class CountdownCalculator @Inject constructor() {

    fun calculateAll(
        events: List<CountdownEvent>,
        now: LocalDate = LocalDate.now()
    ): List<CountdownStatus> {
        return events.map { calculate(it, now) }
            .sortedWith(
                compareBy<CountdownStatus> { it.isPast }
                    .thenBy { it.daysUntil }
            )
    }

    fun calculate(event: CountdownEvent, now: LocalDate): CountdownStatus {
        val next = event.nextOccurrence(now)
        if (next == null) {
            return CountdownStatus(
                event = event,
                nextOccurrence = null,
                daysUntil = 0,
                isToday = false,
                isPast = false,
                progress = 0f,
            )
        }
        val rawDays = ChronoUnit.DAYS.between(now, next)
        val isToday = rawDays == 0L
        val isPast = rawDays < 0L
        val absDays = kotlin.math.abs(rawDays)
        val progress = when {
            isToday -> 1f
            isPast -> 0f
            else -> (1f / (1f + absDays.toFloat())).coerceIn(0.01f, 0.99f)
        }
        return CountdownStatus(
            event = event,
            nextOccurrence = next,
            daysUntil = absDays,
            isToday = isToday,
            isPast = isPast,
            progress = progress,
        )
    }
}
