/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.editor

import com.t8rin.imagetoolbox.core.domain.history.DocumentHistory
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument

class WatermarkEditSession(
    initialDocument: WatermarkDocument = WatermarkDocument(),
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val history = DocumentHistory<WatermarkDocument>(
        maxSize = 50,
        freeze = { document -> document.copy(elements = document.elements.toList()) },
    )

    var document: WatermarkDocument = initialDocument
        private set

    val canUndo: Boolean get() = history.canUndo
    val canRedo: Boolean get() = history.canRedo

    private var compoundEditActive = false
    private var lastMergeKey: String? = null
    private var lastMergeAt = 0L

    /**
     * 连续输入(打字、拖滑块)用同一个 mergeKey 调用时共用一条撤销记录,
     * 避免 50 条历史被一次输入里的每个字符占满。
     */
    fun apply(document: WatermarkDocument, mergeKey: String? = null) {
        if (document == this.document) return
        val now = nowMillis()
        val canMerge = mergeKey != null &&
            !compoundEditActive &&
            mergeKey == lastMergeKey &&
            (now - lastMergeAt) in 0..MERGE_WINDOW_MS
        if (!canMerge) history.snapshot(this.document)
        this.document = document
        lastMergeKey = mergeKey
        lastMergeAt = now
    }

    fun beginCompoundEdit() {
        if (compoundEditActive) return
        history.snapshot(document)
        compoundEditActive = true
        clearMergeState()
    }

    fun updateTransient(document: WatermarkDocument) {
        this.document = document
        clearMergeState()
    }

    fun endCompoundEdit() {
        compoundEditActive = false
        clearMergeState()
    }

    fun undo(): Boolean {
        val current = document
        val restored = history.undo(current) ?: return false
        document = restored
        compoundEditActive = false
        clearMergeState()
        return true
    }

    fun redo(): Boolean {
        val current = document
        val restored = history.redo(current) ?: return false
        document = restored
        compoundEditActive = false
        clearMergeState()
        return true
    }

    fun reset(document: WatermarkDocument) {
        history.clear()
        compoundEditActive = false
        clearMergeState()
        this.document = document
    }

    private fun clearMergeState() {
        lastMergeKey = null
        lastMergeAt = 0L
    }

    private companion object {
        const val MERGE_WINDOW_MS = 800L
    }
}
