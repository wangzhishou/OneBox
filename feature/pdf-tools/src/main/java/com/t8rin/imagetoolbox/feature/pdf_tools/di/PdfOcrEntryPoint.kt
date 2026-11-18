/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.pdf_tools.di

import com.shifenmiao.interfaces.ocr.OcrLayoutService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * PDF 预览的 fragment 由 fragment-compose 反射创建, 拿不到构造注入,
 * 用 EntryPoint 在 onInitOcrProvider() 里取一次 OCR 服务.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PdfOcrEntryPoint {
    fun ocrLayoutService(): OcrLayoutService
}
