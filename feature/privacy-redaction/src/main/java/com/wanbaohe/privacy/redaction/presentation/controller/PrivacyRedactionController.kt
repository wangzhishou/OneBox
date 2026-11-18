/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.presentation.controller

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wanbaohe.privacy.redaction.domain.editor.PrivacyEditSession
import com.wanbaohe.privacy.redaction.domain.model.PrivacyCategory
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument
import com.wanbaohe.privacy.redaction.domain.model.PrivacyMask
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRect
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRegion
import com.wanbaohe.privacy.redaction.domain.render.PrivacyRedactionRenderer
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalysisEvent
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalyzer
import com.wanbaohe.privacy.redaction.presentation.model.PrivacyAnalysisStage
import com.wanbaohe.privacy.redaction.presentation.model.PrivacyAnalysisState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class PrivacyRedactionController(
    private val analyzer: PrivacyAnalyzer,
    private val renderer: PrivacyRedactionRenderer<Bitmap>,
    private val scope: CoroutineScope,
    private val onChanges: () -> Unit,
    private val onHistoryChange: () -> Unit,
) {
    private val sessions = mutableMapOf<String, PrivacyEditSession>()
    private var analysisJob: Job? = null
    private var analysisVersion = 0

    private var documentsState = mutableStateOf<Map<String, PrivacyDocument>>(emptyMap())
    private var currentKeyState = mutableStateOf("")

    var isActive by mutableStateOf(false)
        private set
    var selectedRegionId by mutableStateOf<String?>(null)
        private set
    var isDrawing by mutableStateOf(false)
        private set
    var analysisState by mutableStateOf(PrivacyAnalysisState())
        private set

    private val currentKey: String get() = currentKeyState.value
    val document: PrivacyDocument get() = documentFor(currentKey)
    val canUndo: Boolean get() = sessions[currentKey]?.canUndo == true
    val canRedo: Boolean get() = sessions[currentKey]?.canRedo == true

    fun activate(imageKey: String, bitmap: Bitmap?) {
        pendingBitmap = bitmap
        if (isActive && currentKey == imageKey) {
            analyzeCurrent()
            return
        }
        isActive = true
        currentKeyState.value = imageKey
        selectedRegionId = null
        onHistoryChange()
        onChanges()
        if (bitmap != null && !documentsState.value.containsKey(imageKey)) {
            analyzeCurrent()
        }
    }

    fun deactivate() {
        if (!isActive) return
        analysisVersion++
        analysisJob?.cancel()
        isActive = false
        isDrawing = false
        selectedRegionId = null
        analysisState = PrivacyAnalysisState()
        onHistoryChange()
        onChanges()
    }

    fun selectImage(imageKey: String, bitmap: Bitmap?) {
        pendingBitmap = bitmap
        if (currentKey == imageKey && documentsState.value.containsKey(imageKey)) return
        analysisVersion++
        analysisJob?.cancel()
        analysisState = PrivacyAnalysisState()
        currentKeyState.value = imageKey
        selectedRegionId = null
        isDrawing = false
        onHistoryChange()
        if (isActive && bitmap != null && !documentsState.value.containsKey(imageKey)) {
            analyzeCurrent()
        }
    }

    fun removeImage(imageKey: String) {
        documentsState.value = documentsState.value - imageKey
        sessions.remove(imageKey)
        if (currentKey == imageKey) selectImage("", null)
    }

    fun analyzeCurrent() {
        val imageKey = currentKey
        val bitmap = pendingBitmap ?: return
        if (!isActive || imageKey.isEmpty()) return
        val version = ++analysisVersion
        analysisJob?.cancel()
        analysisState = PrivacyAnalysisState(PrivacyAnalysisStage.RECOGNIZING)
        analysisJob = scope.launch {
            try {
                val regions = analyzer.analyze(bitmap) { event ->
                    if (version == analysisVersion) {
                        analysisState = when (event) {
                            is PrivacyAnalysisEvent.PreparingModel -> PrivacyAnalysisState(
                                stage = PrivacyAnalysisStage.PREPARING_MODEL,
                                progress = event.progress,
                            )
                            PrivacyAnalysisEvent.Recognizing -> PrivacyAnalysisState(
                                stage = PrivacyAnalysisStage.RECOGNIZING,
                            )
                        }
                    }
                }
                if (version != analysisVersion || currentKey != imageKey) return@launch
                val hadDocument = documentsState.value.containsKey(imageKey)
                val updated = documentFor(imageKey).copy(regions = regions)
                val session = sessionFor(imageKey)
                if (hadDocument) session.apply(updated) else session.reset(updated)
                publish(imageKey, session.document)
                selectedRegionId = regions.firstOrNull()?.id
                analysisState = PrivacyAnalysisState()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (version == analysisVersion) {
                    analysisState = PrivacyAnalysisState(PrivacyAnalysisStage.FAILED)
                }
            }
        }
    }

    private var pendingBitmap: Bitmap? = null

    fun setImageBitmap(bitmap: Bitmap?) {
        pendingBitmap = bitmap
        if (isActive && bitmap != null && !documentsState.value.containsKey(currentKey)) {
            analyzeCurrent()
        }
    }

    suspend fun render(image: Bitmap, imageKey: String): Bitmap {
        if (!isActive || imageKey != currentKey) return image
        return renderer.render(image, documentFor(imageKey))
    }

    suspend fun renderPreview(image: Bitmap): Bitmap = render(image, currentKey)

    fun selectRegion(id: String?) {
        selectedRegionId = id
    }

    fun setDrawingMode(value: Boolean) {
        isDrawing = value
        if (value) selectedRegionId = null
    }

    fun addRegion(bounds: PrivacyRect) {
        val region = PrivacyRegion(
            id = "manual-${UUID.randomUUID()}",
            bounds = bounds,
            category = PrivacyCategory.MANUAL,
        )
        applyCurrent(document.copy(regions = document.regions + region))
        selectedRegionId = region.id
        isDrawing = false
    }

    fun updateRegionBounds(id: String, bounds: PrivacyRect) {
        updateRegion(id) { it.copy(bounds = bounds) }
    }

    fun toggleRegion(id: String, enabled: Boolean) {
        updateRegion(id) { it.copy(isEnabled = enabled) }
    }

    fun deleteRegion(id: String) {
        applyCurrent(document.copy(regions = document.regions.filterNot { it.id == id }))
        if (selectedRegionId == id) selectedRegionId = null
    }

    fun updateMask(mask: PrivacyMask) {
        applyCurrent(document.copy(mask = mask), mergeKey = "mask")
    }

    fun updateStrength(strength: Float) {
        applyCurrent(document.copy(strength = strength), mergeKey = "strength")
    }

    fun clearCurrent() {
        if (!isActive || currentKey.isEmpty()) return
        val session = sessionFor(currentKey)
        session.reset(PrivacyDocument(mask = document.mask, strength = document.strength))
        selectedRegionId = null
        publish(currentKey, session.document)
    }

    fun undo() {
        val session = sessions[currentKey] ?: return
        if (session.undo()) publish(currentKey, session.document)
    }

    fun redo() {
        val session = sessions[currentKey] ?: return
        if (session.redo()) publish(currentKey, session.document)
    }

    private fun updateRegion(id: String, transform: (PrivacyRegion) -> PrivacyRegion) {
        applyCurrent(
            document.copy(
                regions = document.regions.map { region ->
                    if (region.id == id) transform(region) else region
                }
            )
        )
    }

    private fun applyCurrent(document: PrivacyDocument, mergeKey: String? = null) {
        val imageKey = currentKey
        if (imageKey.isEmpty()) return
        val session = sessionFor(imageKey)
        session.apply(document, mergeKey)
        publish(imageKey, session.document)
    }

    private fun publish(imageKey: String, document: PrivacyDocument) {
        documentsState.value = documentsState.value + (imageKey to document)
        onHistoryChange()
        onChanges()
    }

    private fun documentFor(imageKey: String): PrivacyDocument =
        sessions[imageKey]?.document ?: documentsState.value[imageKey] ?: PrivacyDocument()

    private fun sessionFor(imageKey: String): PrivacyEditSession = sessions.getOrPut(imageKey) {
        PrivacyEditSession(documentsState.value[imageKey] ?: PrivacyDocument())
    }
}
