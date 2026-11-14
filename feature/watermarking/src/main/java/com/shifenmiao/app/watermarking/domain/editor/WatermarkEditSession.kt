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

    fun apply(document: WatermarkDocument) {
        if (document == this.document) return
        history.snapshot(this.document)
        this.document = document
    }

    fun beginCompoundEdit() {
        if (compoundEditActive) return
        history.snapshot(document)
        compoundEditActive = true
    }

    fun updateTransient(document: WatermarkDocument) {
        this.document = document
    }

    fun endCompoundEdit() {
        compoundEditActive = false
    }

    fun undo(): Boolean {
        val current = document
        val restored = history.undo(current) ?: return false
        document = restored
        compoundEditActive = false
        return true
    }

    fun redo(): Boolean {
        val current = document
        val restored = history.redo(current) ?: return false
        document = restored
        compoundEditActive = false
        return true
    }

    fun reset(document: WatermarkDocument) {
        history.clear()
        compoundEditActive = false
        this.document = document
    }
}
