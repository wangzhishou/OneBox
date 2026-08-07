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
        if (url.trim().isEmpty()) {
            return false
        }
        val urlRegex =
            "^(http://|https://)[a-z0-9]+([-.]{1}[a-z0-9]+)*.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?/$".toRegex()
        return urlRegex.matches(url)
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
        return points.toString()
    }

    fun formatNumber(points: Long): String {
        return points.toString()
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
        if (query.isEmpty() || text.isEmpty()) return AnnotatedString(text)

        // Find the first occurrence of the query (case insensitive)
        val firstOccurrence = text.indexOf(query, ignoreCase = true)
        if (firstOccurrence == -1) return AnnotatedString(text.take(90))

        // Calculate context window
        val startPosition = maxOf(0, firstOccurrence - 30)
        val endPosition = minOf(text.length, firstOccurrence + query.length + 30)

        // Extract context
        val hasLeadingEllipsis = startPosition > 0
        val hasTrailingEllipsis = endPosition < text.length

        val contextText = buildString {
            if (hasLeadingEllipsis) append("...")
            append(text.substring(startPosition, endPosition))
            if (hasTrailingEllipsis) append("...")
        }

        // Build annotated string with highlighted query
        return buildAnnotatedString {
            append(contextText)

            val extractedText = text.substring(startPosition, endPosition)
            val queryRegex = query.toRegex(RegexOption.IGNORE_CASE)
            val adjustedStartPos = if (hasLeadingEllipsis) 3 else 0

            queryRegex.findAll(extractedText).forEach { result ->
                addStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        background = highlightBackgroundColor
                    ),
                    start = result.range.first + adjustedStartPos,
                    end = result.range.last + 1 + adjustedStartPos
                )
            }
        }
    }
}