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

import com.wanbaohe.privacy.redaction.domain.model.PrivacyCategory
import javax.inject.Inject

class SensitiveTextDetector @Inject constructor() {
    fun detect(lines: List<PrivacyTextLine>): List<PrivacyTextMatch> = lines.mapNotNull { line ->
        val text = SensitiveTextNormalizer.normalize(line.text)
        val category = when {
            ID_CARD.containsMatchIn(text) && ID_CARD.findAll(text).any {
                SensitiveValueValidators.isChineseId(it.value)
            } -> PrivacyCategory.ID_CARD

            BANK_CANDIDATE.findAll(text).any { match ->
                SensitiveValueValidators.isBankCard(match.value.filter(Char::isDigit))
            } -> PrivacyCategory.BANK_CARD

            CN_PHONE.containsMatchIn(text) || INTERNATIONAL_PHONE.containsMatchIn(text) ->
                PrivacyCategory.PHONE

            EMAIL.containsMatchIn(text) -> PrivacyCategory.EMAIL
            else -> null
        }
        category?.let { PrivacyTextMatch(it, line) }
    }

    private companion object {
        val ID_CARD = Regex(
            "(?<![0-9Xx])[1-9]\\d{5}(?:19|20)\\d{2}" +
                "(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx](?![0-9Xx])"
        )
        val BANK_CANDIDATE = Regex("(?<!\\d)\\d[\\d\\s-]{14,38}\\d(?!\\d)")
        val CN_PHONE = Regex("(?<!\\d)(?:\\+?86[ -]?)?1[3-9](?:[ -]?\\d){9}(?!\\d)")
        val INTERNATIONAL_PHONE = Regex("(?<!\\d)\\+[1-9](?:[ -]?\\d){7,14}(?!\\d)")
        val EMAIL = Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE)
    }
}
