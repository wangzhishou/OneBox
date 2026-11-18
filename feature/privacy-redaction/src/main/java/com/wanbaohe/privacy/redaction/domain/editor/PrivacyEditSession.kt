/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.domain.editor

import com.t8rin.imagetoolbox.core.domain.history.DocumentHistory
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument

class PrivacyEditSession(
    initialDocument: PrivacyDocument = PrivacyDocument(),
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val history = DocumentHistory<PrivacyDocument>(
        maxSize = 50,
        freeze = { document -> document.copy(regions = document.regions.toList()) },
    )

    var document: PrivacyDocument = initialDocument
        private set

    val canUndo: Boolean get() = history.canUndo
    val canRedo: Boolean get() = history.canRedo

    private var lastMergeKey: String? = null
    private var lastMergeAt = 0L

    fun apply(document: PrivacyDocument, mergeKey: String? = null) {
        if (document == this.document) return
        val now = nowMillis()
        val canMerge = mergeKey != null && mergeKey == lastMergeKey &&
            (now - lastMergeAt) in 0..MERGE_WINDOW_MS
        if (!canMerge) history.snapshot(this.document)
        this.document = document
        lastMergeKey = mergeKey
        lastMergeAt = now
    }

    fun undo(): Boolean {
        val restored = history.undo(document) ?: return false
        document = restored
        clearMergeState()
        return true
    }

    fun redo(): Boolean {
        val restored = history.redo(document) ?: return false
        document = restored
        clearMergeState()
        return true
    }

    fun reset(document: PrivacyDocument) {
        history.clear()
        this.document = document
        clearMergeState()
    }

    private fun clearMergeState() {
        lastMergeKey = null
        lastMergeAt = 0L
    }

    private companion object {
        const val MERGE_WINDOW_MS = 800L
    }
}
