/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.ocr.data

import com.shifenmiao.interfaces.ocr.OcrModelService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OnDeviceOcrModelService @Inject constructor(
    private val modelStore: OcrModelStore,
) : OcrModelService {
    override fun isReady(): Boolean = modelStore.isReady()

    override suspend fun ensureReady(onProgress: (Float?) -> Unit) {
        modelStore.ensureDownloaded(onProgress)
    }
}
