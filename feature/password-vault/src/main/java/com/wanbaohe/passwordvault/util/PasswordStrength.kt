package com.wanbaohe.passwordvault.util

import androidx.compose.ui.graphics.Color

/**
 * 密码强度档位。
 *
 * 评级基于字符集多样性 + 长度 + 常见弱密码/序列/重复 减分项，
 * 不引入 zxcvbn 等第三方库以保持包体小、零依赖、纯函数可单测。
 */
enum class PasswordStrength(val score: Int) {
    EMPTY(0),
    WEAK(1),
    FAIR(2),
    GOOD(3),
    STRONG(4);

    val labelRes: Int
        get() = when (this) {
            EMPTY -> com.wanbaohe.passwordvault.R.string.password_vault_strength_empty
            WEAK -> com.wanbaohe.passwordvault.R.string.password_vault_strength_weak
            FAIR -> com.wanbaohe.passwordvault.R.string.password_vault_strength_fair
            GOOD -> com.wanbaohe.passwordvault.R.string.password_vault_strength_good
            STRONG -> com.wanbaohe.passwordvault.R.string.password_vault_strength_strong
        }

    companion object {
        fun colorFor(level: PasswordStrength): Color = when (level) {
            EMPTY -> Color(0xFF9E9E9E)
            WEAK -> Color(0xFFD32F2F)
            FAIR -> Color(0xFFF57C00)
            GOOD -> Color(0xFFFBC02D)
            STRONG -> Color(0xFF388E3C)
        }
    }
}

data class PasswordStrengthResult(
    val level: PasswordStrength,
    val suggestions: List<Int>,
)

object PasswordStrengthEvaluator {

    private val commonWeakPasswords: Set<String> = setOf(
        "123456", "password", "qwerty", "111111", "123123",
        "abc123", "12345678", "123456789", "000000", "iloveyou",
        "admin", "letmein", "welcome", "monkey", "dragon",
    )

    fun evaluate(password: String): PasswordStrengthResult {
        if (password.isEmpty()) {
            return PasswordStrengthResult(PasswordStrength.EMPTY, emptyList())
        }

        var score = 0

        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++

        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        if (hasLower && hasUpper) score++

        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        if (password.lowercase() in commonWeakPasswords) {
            score = minOf(score, PasswordStrength.WEAK.score)
        }

        if (hasRepeatedRun(password)) score--
        if (hasSequentialRun(password, minRun = 4)) score--

        val level = when (score.coerceIn(0, PasswordStrength.STRONG.score)) {
            0 -> PasswordStrength.WEAK
            1 -> PasswordStrength.WEAK
            2 -> PasswordStrength.FAIR
            3 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }

        return PasswordStrengthResult(level, buildSuggestions(password, hasLower, hasUpper))
    }

    private fun buildSuggestions(
        password: String,
        hasLower: Boolean,
        hasUpper: Boolean,
    ): List<Int> {
        val suggestions = mutableListOf<Int>()
        if (password.length < 12) {
            suggestions += com.wanbaohe.passwordvault.R.string.password_vault_strength_suggestion_length
        }
        if (!(hasLower && hasUpper)) {
            suggestions += com.wanbaohe.passwordvault.R.string.password_vault_strength_suggestion_case
        }
        if (password.none { it.isDigit() }) {
            suggestions += com.wanbaohe.passwordvault.R.string.password_vault_strength_suggestion_digit
        }
        if (password.none { !it.isLetterOrDigit() }) {
            suggestions += com.wanbaohe.passwordvault.R.string.password_vault_strength_suggestion_symbol
        }
        if (password.lowercase() in commonWeakPasswords) {
            suggestions += com.wanbaohe.passwordvault.R.string.password_vault_strength_suggestion_common
        }
        return suggestions
    }

    private fun hasRepeatedRun(password: String): Boolean {
        if (password.length < 3) return false
        var i = 0
        while (i < password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                return true
            }
            i++
        }
        return false
    }

    private fun hasSequentialRun(password: String, minRun: Int): Boolean {
        if (password.length < minRun) return false
        val lowered = password.lowercase()
        var ascending = 1
        var descending = 1
        for (i in 1 until lowered.length) {
            val diff = lowered[i].code - lowered[i - 1].code
            if (diff == 1) {
                ascending++
                descending = 1
            } else if (diff == -1) {
                descending++
                ascending = 1
            } else {
                ascending = 1
                descending = 1
            }
            if (ascending >= minRun || descending >= minRun) return true
        }
        return false
    }
}
