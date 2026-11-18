/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.domain.detection

internal object SensitiveValueValidators {
    private val idWeights = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
    private const val ID_CHECK_CODES = "10X98765432"

    fun isChineseId(value: String): Boolean {
        if (value.length != 18) return false
        val first17 = value.take(17)
        if (first17.any { !it.isDigit() }) return false
        val checksum = first17.indices.sumOf { index ->
            (first17[index] - '0') * idWeights[index]
        } % 11
        return value.last().uppercaseChar() == ID_CHECK_CODES[checksum]
    }

    fun isBankCard(digits: String): Boolean {
        if (digits.length !in 16..19 || digits.any { !it.isDigit() }) return false
        var sum = 0
        var doubleDigit = false
        for (index in digits.indices.reversed()) {
            var digit = digits[index] - '0'
            if (doubleDigit) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
            doubleDigit = !doubleDigit
        }
        return sum % 10 == 0
    }
}
