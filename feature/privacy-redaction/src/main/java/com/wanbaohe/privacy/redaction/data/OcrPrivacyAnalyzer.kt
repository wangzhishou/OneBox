/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.data

import android.graphics.Bitmap
import com.shifenmiao.interfaces.ocr.OcrLayoutService
import com.shifenmiao.interfaces.ocr.OcrModelService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.privacy.redaction.domain.detection.PrivacyTextLine
import com.wanbaohe.privacy.redaction.domain.detection.SensitiveTextDetector
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRect
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRegion
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalysisEvent
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalyzer
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class OcrPrivacyAnalyzer @Inject constructor(
    private val ocrLayoutService: OcrLayoutService,
    private val ocrModelService: OcrModelService,
    private val detector: SensitiveTextDetector,
    dispatchersHolder: DispatchersHolder,
) : PrivacyAnalyzer, DispatchersHolder by dispatchersHolder {
    override suspend fun analyze(
        image: Bitmap,
        onEvent: (PrivacyAnalysisEvent) -> Unit,
    ): List<PrivacyRegion> = withContext(defaultDispatcher) {
        if (!ocrModelService.isReady()) {
            onEvent(PrivacyAnalysisEvent.PreparingModel(null))
            ocrModelService.ensureReady { progress ->
                onEvent(PrivacyAnalysisEvent.PreparingModel(progress))
            }
        }
        onEvent(PrivacyAnalysisEvent.Recognizing)
        val lines = ocrLayoutService.recognizeLines(image).map { line ->
            PrivacyTextLine(
                text = line.text,
                confidence = line.confidence,
                left = line.left,
                top = line.top,
                right = line.right,
                bottom = line.bottom,
            )
        }
        detector.detect(lines).mapIndexed { index, match ->
            val line = match.line
            val lineHeight = (line.bottom - line.top).coerceAtLeast(1)
            val paddingX = lineHeight * HORIZONTAL_PADDING_RATIO
            val paddingY = lineHeight * VERTICAL_PADDING_RATIO
            PrivacyRegion(
                id = "auto-$index-${match.category.name}",
                category = match.category,
                bounds = PrivacyRect(
                    left = ((line.left - paddingX) / image.width).coerceIn(0f, 1f),
                    top = ((line.top - paddingY) / image.height).coerceIn(0f, 1f),
                    right = ((line.right + paddingX) / image.width).coerceIn(0f, 1f),
                    bottom = ((line.bottom + paddingY) / image.height).coerceIn(0f, 1f),
                ),
            )
        }.filter { region -> region.bounds.width > 0f && region.bounds.height > 0f }
    }

    private companion object {
        const val HORIZONTAL_PADDING_RATIO = 0.16f
        const val VERTICAL_PADDING_RATIO = 0.22f
    }
}
