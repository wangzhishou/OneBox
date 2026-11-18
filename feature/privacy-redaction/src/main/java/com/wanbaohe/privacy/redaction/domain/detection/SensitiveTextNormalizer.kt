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

internal object SensitiveTextNormalizer {
    fun normalize(text: String): String = buildString(text.length) {
        text.forEach { char ->
            append(
                when (char) {
                    in '\uFF10'..'\uFF19' -> '0' + (char - '\uFF10')
                    in '\uFF21'..'\uFF3A' -> 'A' + (char - '\uFF21')
                    in '\uFF41'..'\uFF5A' -> 'a' + (char - '\uFF41')
                    '\uFF20' -> '@'
                    '\u3000' -> ' '
                    else -> char
                }
            )
        }
    }
}
