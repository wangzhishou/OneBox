package com.shifenmiao.base.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.interfaces.singleton.AppContext
import kotlin.random.Random

object StringUtils {

    fun isValidEmail(email: String): Boolean {
        if (email.trim().isEmpty()) {
            return false
        }
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$")
        return regex.matches(email)
    }

    fun isValidCode(code: String): Boolean {
        if (code.trim().isEmpty()) {
            return false
        }
        val regex = Regex("^[0-9]{6}$")
        return regex.matches(code)
    }

    fun isValidChinesePhoneNumber(phoneNumber: String): Boolean {
        if (phoneNumber.trim().isEmpty()) {
            return false
        }
        val regex = Regex("^1[3-9]\\d{9}$")
        return regex.matches(phoneNumber)
    }

    fun validateInvitationCode(invitationCode: String): Boolean {
        return invitationCode.isNotEmpty() && invitationCode.length == 8
    }


    fun getFirstCharacter(text: String): String {
        if (text.isEmpty()) {
            return getRandomCharacterFromAppName()
        } else {
            return text.first().toString()
        }
    }


    fun getRandomCharacterFromAppName(): String {
        val stringResource: String = AppContext.getString(R.string.app_name)
        if (stringResource.isEmpty()) {
            return "万"
        }
        val randomIndex = Random.nextInt(stringResource.length)
        return stringResource[randomIndex].toString()
    }

    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return false
        }
        if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return false
        }
        return try {
            !java.net.URI(trimmed).host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 规范化 API base URL：去空白、补全 http(s)://、统一 scheme 小写、保证以 '/' 结尾。
     * 用户常漏写 scheme 或结尾斜杠，在失焦/保存时静默修好，避免校验报错打断填写。
     */
    fun normalizeApiBaseUrl(url: String): String {
        var result = url.trim()
        if (result.isEmpty()) {
            return result
        }
        if (!result.startsWith("http://", ignoreCase = true) &&
            !result.startsWith("https://", ignoreCase = true)
        ) {
            result = "https://$result"
        }
        result = when {
            result.startsWith("http://", ignoreCase = true) ->
                "http://" + result.substring(7)
            result.startsWith("https://", ignoreCase = true) ->
                "https://" + result.substring(8)
            else -> result
        }
        if (!result.endsWith("/")) {
            result = "$result/"
        }
        return result
    }

    /**
     * 规范化 API 路径：去空白，非空时补全前导 '/'。
     */
    fun normalizeApiPath(path: String): String {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            return trimmed
        }
        return if (trimmed.startsWith("/")) trimmed else "/$trimmed"
    }

    /**
     * 清洗授权码粘贴内容：去掉 Bearer / x-api-key: 等前缀和引号。
     * 用户常从文档/curl 整段复制，直接落库会带上请求头导致鉴权失败。
     */
    fun sanitizeApiToken(raw: String): String {
        if (raw.isBlank()) return raw
        val trimmed = raw.trim()

        // curl / 多行 header 粘贴：优先抽出 x-api-key / Authorization 里的 token
        val headerToken = Regex(
            pattern = """(?i)(?:x-api-key|api-key|authorization)\s*[:=]\s*(?:bearer\s+|basic\s+|token\s+)?["']?([^\s"'\\,]+)["']?"""
        ).find(trimmed)?.groupValues?.get(1)
        if (headerToken != null && (trimmed.contains('\n') || trimmed.contains("curl", ignoreCase = true) || trimmed.contains(':'))) {
            return headerToken.trim().trim('"', '\'')
        }

        var s = trimmed
        // 行首常见鉴权前缀（可重复，如 Authorization: Bearer xxx）
        repeat(3) {
            s = s.replace(
                Regex("""^(?i)(?:authorization\s*[:=]\s*)?(?:bearer|basic|token|x-api-key|api-key)\s*[:=]?\s*"""),
                "",
            )
        }
        return s.trim().trim('"', '\'', '`')
    }

    fun formatPriceWithUnit(price: Float): String {
        val priceInt = price.toInt()
        val formattedPrice = if (price - priceInt == 0f) priceInt.toString() else price.toString()
        return when {
            price < 10000 -> formattedPrice
            else -> "${price / 10000}万"
        }
    }

    fun calculateRewardPoints(price: Float): Int {
        val baseRate = 1000 // Base conversion rate: 1f = 1000 points
        val rewardTiers = listOf(
            0f to 1f, // For amounts up to 10, no increase
            10f to 1.1f, // For amounts over 10 to 50, increase by 10%
            50f to 1.15f, // For amounts over 50 to 100, increase by 15%
            100f to 1.20f, // For amounts over 100 to 500, increase by 20%
            500f to 1.25f, // For amounts over 500 to 1000, increase by 25%
            1000f to 1.3f // For amounts over 1000, increase by 30%
        )

        var points = 0f
        var remainingAmount = price

        for (i in rewardTiers.indices.reversed()) {
            val (threshold, multiplier) = rewardTiers[i]
            if (remainingAmount >= threshold) {
                val applicableAmount = remainingAmount - threshold
                points += applicableAmount * baseRate * multiplier
                remainingAmount = threshold
            }
        }

        return points.toInt()
    }

    fun calculateLadderPoints(amount: Float): Int {
        val baseRate = 1000 // 基础兑换率：1f 兑换 1000积分
        val discountRates = listOf(
            1f to 10f, // 10元以内不打折
            0.95f to 50f, // 50元以内95折
            0.9f to 100f, // 50-100打9折
            0.85f to 500f, // 100-500打8.5折
            0.8f to 1000f, // 500-1000打8折
            0.7f to Float.MAX_VALUE // 1000元以上打7折
        )

        var remainingAmount = amount
        var totalPoints = 0f

        for ((discountRate, maxAmount) in discountRates) {
            if (remainingAmount <= 0) break // 如果没有剩余金额，跳出循环

            val applicableAmount = if (remainingAmount > maxAmount) maxAmount else remainingAmount
            totalPoints += applicableAmount * baseRate * discountRate

            remainingAmount -= applicableAmount // 减去已经计算过的金额部分
            if (applicableAmount == maxAmount) continue // 如果当前阶段金额已达上限，继续下一阶段
        }

        return totalPoints.toInt()
    }

    fun formatNumber(points: Int): String {
        return java.text.NumberFormat.getIntegerInstance().format(points)
    }

    fun formatNumber(points: Long): String {
        return java.text.NumberFormat.getIntegerInstance().format(points)
    }

    fun calculateTokens(input: String): Int {
        if (input.isEmpty()) {
            return 0
        }
        // Split the input string by spaces, punctuation marks, and Chinese characters to get the tokens
        val tokens =
            input.split(Regex("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})|(?=\\p{IsHan})|(?<=\\p{IsHan})"))
        // Filter out any empty tokens and return the count
        return tokens.filter { it.isNotEmpty() }.size
    }

    fun getHighlightedDescription(
        text: String,
        query: String,
        highlightColor: Color = Color.Red,
        highlightBackgroundColor: Color = Color.Red.copy(0.1f),
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString(text)
        val snippet = firstReadableSnippet(text)
        if (query.isEmpty()) return AnnotatedString(snippet)

        return buildAnnotatedString {
            append(snippet)
            Regex.escape(query).toRegex(RegexOption.IGNORE_CASE).findAll(snippet).forEach { result ->
                addStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        background = highlightBackgroundColor
                    ),
                    start = result.range.first,
                    end = result.range.last + 1
                )
            }
        }
    }

    /**
     * 搜索卡片说明从第一句开始。超长时在单词边界截断，避免从单词中间起头。
     */
    private fun firstReadableSnippet(text: String, maxChars: Int = 110): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return trimmed
        val sentenceEnd = trimmed.indexOfFirst { it in SENTENCE_ENDINGS }
        val sentence = if (sentenceEnd in 8..maxChars) {
            trimmed.substring(0, sentenceEnd + 1).trim()
        } else {
            trimmed
        }
        if (sentence.length <= maxChars) return sentence
        val space = sentence.lastIndexOf(' ', maxChars)
        val cut = if (space >= maxChars / 2) space else maxChars
        return sentence.substring(0, cut).trimEnd() + "..."
    }

    private val SENTENCE_ENDINGS = charArrayOf('.', '!', '?', '。', '！', '？')
}