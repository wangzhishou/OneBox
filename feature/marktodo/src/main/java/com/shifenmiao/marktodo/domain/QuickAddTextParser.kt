package com.shifenmiao.marktodo.domain

import com.shifenmiao.marktodo.model.QuickAddParseResult
import com.shifenmiao.marktodo.model.QuickAddToken
import com.shifenmiao.marktodo.model.QuickAddTokenType
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import java.util.Calendar
import kotlin.math.min

object QuickAddTextParser {

    fun parse(
        text: String,
        categories: List<TodoCategory>,
        tags: List<TodoTag>,
        nowMillis: Long = System.currentTimeMillis(),
    ): QuickAddParseResult {
        if (text.isBlank()) return QuickAddParseResult()

        val tokens = mutableListOf<QuickAddToken>()
        val dateMatch = DATE_REGEX.find(text)
        val dateOffset = dateMatch?.let { match ->
            when (match.value.lowercase().replace(Regex("\\s+"), " ")) {
                "今天", "today" -> 0
                "明天", "tomorrow" -> 1
                else -> 2
            }
        }
        dateMatch?.let { match ->
            tokens += QuickAddToken(
                type = QuickAddTokenType.DATE,
                raw = match.value,
                value = dateOffset.toString(),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        val time = TIME_REGEXES.firstNotNullOfOrNull { regex ->
            regex.findAll(text).mapNotNull(::parseTimeMatch).firstOrNull()
        }
        time?.let { parsed ->
            tokens += QuickAddToken(
                type = QuickAddTokenType.TIME,
                raw = parsed.raw,
                value = parsed.minutesOfDay.toString(),
                start = parsed.start,
                end = parsed.end,
            )
        }

        PRIORITY_REGEX.find(text)?.let { match ->
            val priority = when (match.groupValues[1].lowercase()) {
                "高", "high" -> TodoTask.PRIORITY_HIGH
                "低", "low" -> TodoTask.PRIORITY_LOW
                else -> TodoTask.PRIORITY_MEDIUM
            }
            tokens += QuickAddToken(
                type = QuickAddTokenType.PRIORITY,
                raw = match.value,
                value = priority.toString(),
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        val selectedTags = mutableListOf<String>()
        TAG_REGEX.findAll(text).forEach { match ->
            val tag = tags.firstOrNull { it.name.equals(match.groupValues[1], ignoreCase = true) }
                ?: return@forEach
            if (selectedTags.none { it.equals(tag.name, ignoreCase = true) }) {
                selectedTags += tag.name
            }
            tokens += QuickAddToken(
                type = QuickAddTokenType.TAG,
                raw = match.value,
                value = tag.name,
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        var selectedCategoryId: String? = null
        CATEGORY_REGEX.findAll(text).forEach { match ->
            if (selectedCategoryId != null) return@forEach
            val category = categories.firstOrNull {
                it.title.equals(match.groupValues[1], ignoreCase = true)
            } ?: return@forEach
            selectedCategoryId = category.id
            tokens += QuickAddToken(
                type = QuickAddTokenType.CATEGORY,
                raw = match.value,
                value = category.id,
                start = match.range.first,
                end = match.range.last + 1,
            )
        }

        val hasDateOrTime = dateOffset != null || time != null
        val dueDateMillis = if (hasDateOrTime) {
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, dateOffset ?: 0)
                set(Calendar.HOUR_OF_DAY, time?.let { it.minutesOfDay / 60 } ?: 0)
                set(Calendar.MINUTE, time?.let { it.minutesOfDay % 60 } ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else {
            null
        }

        return QuickAddParseResult(
            cleanedTitle = removeRecognizedTokens(text, tokens),
            dueDateMillis = dueDateMillis,
            dueTimeMinutes = time?.minutesOfDay,
            priority = tokens.firstOrNull { it.type == QuickAddTokenType.PRIORITY }
                ?.value?.toIntOrNull(),
            tagNames = selectedTags,
            categoryId = selectedCategoryId,
            tokens = tokens.sortedBy { it.start },
        )
    }

    private fun parseTimeMatch(match: MatchResult): ParsedTime? {
        val pattern = match.groupValues
        val raw = match.value
        val hour: Int
        val minute: Int
        when {
            raw.contains('点') || raw.contains('时') -> {
                hour = pattern[2].toIntOrNull() ?: return null
                minute = when {
                    pattern[3] == "半" -> 30
                    pattern[4].isNotEmpty() -> pattern[4].toIntOrNull() ?: return null
                    else -> 0
                }
                val period = pattern[1]
                if (period.isNotEmpty() && hour !in 1..12) return null
                val adjustedHour = when (period) {
                    "下午", "晚上" -> if (hour == 12) 12 else hour + 12
                    "中午" -> if (hour == 12) 12 else hour + 12
                    "上午", "早上", "凌晨" -> if (hour == 12) 0 else hour
                    else -> hour
                }
                return ParsedTime(raw, adjustedHour * 60 + minute, match.range.first, match.range.last + 1)
            }

            pattern.size > 3 && pattern[3].isNotEmpty() -> {
                hour = pattern[1].toIntOrNull() ?: return null
                minute = pattern[2].toIntOrNull() ?: return null
                if (hour !in 1..12) return null
                val isPm = pattern[3].equals("pm", ignoreCase = true)
                val adjustedHour = when {
                    isPm && hour != 12 -> hour + 12
                    !isPm && hour == 12 -> 0
                    else -> hour
                }
                return ParsedTime(raw, adjustedHour * 60 + minute, match.range.first, match.range.last + 1)
            }

            raw.contains(':') -> {
                hour = pattern[1].toIntOrNull() ?: return null
                minute = pattern[2].toIntOrNull() ?: return null
            }

            else -> {
                val rawHour = pattern[1].toIntOrNull() ?: return null
                if (rawHour !in 1..12) return null
                val isPm = pattern[2].equals("pm", ignoreCase = true)
                hour = when {
                    isPm && rawHour != 12 -> rawHour + 12
                    !isPm && rawHour == 12 -> 0
                    else -> rawHour
                }
                minute = 0
            }
        }
        if (hour !in 0..23 || minute !in 0..59) return null
        return ParsedTime(raw, hour * 60 + minute, match.range.first, match.range.last + 1)
    }

    private fun removeRecognizedTokens(text: String, tokens: List<QuickAddToken>): String {
        val ranges = tokens.map { it.start..<it.end }.sortedBy { it.first }
        val result = StringBuilder(text.length)
        var cursor = 0
        ranges.forEach { range ->
            if (range.first < cursor) return@forEach
            result.append(text, cursor, min(range.first, text.length))
            cursor = min(range.last + 1, text.length)
        }
        result.append(text, cursor, text.length)
        return result.toString().replace(Regex("\\s+"), " ").trim()
    }

    private data class ParsedTime(
        val raw: String,
        val minutesOfDay: Int,
        val start: Int,
        val end: Int,
    )

    private val DATE_REGEX = Regex(
        "后天|今天|明天|(?<![A-Za-z])(?:the\\s+day\\s+after\\s+tomorrow|day\\s+after\\s+tomorrow|today|tomorrow)(?![A-Za-z])",
        RegexOption.IGNORE_CASE,
    )
    private val CHINESE_TIME_REGEX = Regex(
        "(?<![\\d])(上午|早上|凌晨|中午|下午|晚上)?\\s*([01]?\\d|2[0-3])\\s*(?:点|时)\\s*(?:(半)|([0-5]?\\d)\\s*分?)?"
    )
    private val CLOCK_TIME_REGEX = Regex(
        "(?<![\\d:])([01]?\\d|2[0-3]):([0-5]\\d)\\s*([aApP][mM])?(?![\\d:])"
    )
    private val MERIDIEM_TIME_REGEX = Regex(
        "(?<![A-Za-z\\d])([01]?\\d|2[0-3])\\s*([aApP][mM])(?![A-Za-z])",
        RegexOption.IGNORE_CASE,
    )
    private val TIME_REGEXES = listOf(CHINESE_TIME_REGEX, CLOCK_TIME_REGEX, MERIDIEM_TIME_REGEX)
    private val PRIORITY_REGEX = Regex("!(high|medium|low|高|中|低)(?![\\p{L}\\p{N}_])", RegexOption.IGNORE_CASE)
    private val TAG_REGEX = Regex("(?<![#@])#([\\p{L}\\p{N}_-]+)")
    private val CATEGORY_REGEX = Regex("(?<![#@])@([\\p{L}\\p{N}_-]+)")
}
