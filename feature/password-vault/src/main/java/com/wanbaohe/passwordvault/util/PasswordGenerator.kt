package com.wanbaohe.passwordvault.util

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 密码保险箱密码生成器。
 *
 * 设计为纯函数 + 注入式 [SecureRandom]，便于单测和依赖倒置。
 * 默认排除易混淆字符（0/O、1/l/I），可由调用方控制。
 */
data class PasswordGeneratorOptions(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeDigits: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true,
) {
    init {
        require(length in 4..128) { "length must be in 4..128" }
    }

    val isValid: Boolean
        get() = includeUppercase || includeLowercase || includeDigits || includeSymbols
}

@Singleton
class PasswordGenerator @Inject constructor() {

    private val random: SecureRandom = SecureRandom()

    fun generate(options: PasswordGeneratorOptions = PasswordGeneratorOptions()): String {
        if (!options.isValid) return ""

        val pools = mutableListOf<String>()
        if (options.includeUppercase) pools += UPPERCASE
        if (options.includeLowercase) pools += LOWERCASE
        if (options.includeDigits) pools += DIGITS
        if (options.includeSymbols) pools += SYMBOLS

        val filteredPools = pools.map { pool ->
            if (options.excludeAmbiguous) pool.filter { it !in AMBIGUOUS_CHARS } else pool
        }.filter { it.isNotEmpty() }

        if (filteredPools.isEmpty()) return ""

        val combined = filteredPools.joinToString(separator = "")

        val out = StringBuilder(options.length)
        repeat(options.length) {
            out.append(combined[random.nextInt(combined.length)])
        }
        return out.toString()
    }

    private companion object {
        const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        const val DIGITS = "0123456789"
        const val SYMBOLS = "!@#\$%^&*()-_=+[]{};:,.<>?/"
        const val AMBIGUOUS_CHARS = "0Oo1lI|`'\""
    }
}
