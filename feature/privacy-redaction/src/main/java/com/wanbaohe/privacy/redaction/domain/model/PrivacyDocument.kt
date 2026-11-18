/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.domain.model

enum class PrivacyCategory {
    PHONE,
    ID_CARD,
    BANK_CARD,
    EMAIL,
    MANUAL,
}

enum class PrivacyMask {
    MOSAIC,
    SOLID,
}

data class PrivacyRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun resized(centerX: Float, centerY: Float, scale: Float): PrivacyRect {
        val newWidth = (width * scale).coerceIn(MIN_SIZE, 1f)
        val newHeight = (height * scale).coerceIn(MIN_SIZE, 1f)
        val boundedCenterX = centerX.coerceIn(newWidth / 2f, 1f - newWidth / 2f)
        val boundedCenterY = centerY.coerceIn(newHeight / 2f, 1f - newHeight / 2f)
        return PrivacyRect(
            left = boundedCenterX - newWidth / 2f,
            top = boundedCenterY - newHeight / 2f,
            right = boundedCenterX + newWidth / 2f,
            bottom = boundedCenterY + newHeight / 2f,
        )
    }

    companion object {
        const val MIN_SIZE = 0.008f

        fun fromCorners(startX: Float, startY: Float, endX: Float, endY: Float): PrivacyRect? {
            val left = minOf(startX, endX).coerceIn(0f, 1f)
            val top = minOf(startY, endY).coerceIn(0f, 1f)
            val right = maxOf(startX, endX).coerceIn(0f, 1f)
            val bottom = maxOf(startY, endY).coerceIn(0f, 1f)
            return PrivacyRect(left, top, right, bottom).takeIf {
                it.width >= MIN_SIZE && it.height >= MIN_SIZE
            }
        }
    }
}

data class PrivacyRegion(
    val id: String,
    val bounds: PrivacyRect,
    val category: PrivacyCategory,
    val isEnabled: Boolean = true,
)

data class PrivacyDocument(
    val regions: List<PrivacyRegion> = emptyList(),
    val mask: PrivacyMask = PrivacyMask.MOSAIC,
    val strength: Float = 0.75f,
) {
    val enabledRegions: List<PrivacyRegion>
        get() = regions.filter(PrivacyRegion::isEnabled)
}
