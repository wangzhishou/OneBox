/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.watermark.template.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 模板列表契约。
 *
 * 图片水印页注入包含「内置 + 用户自定义」的实现；
 * 后续拍照入口注入 [BuiltinTemplateCatalog]，只需选样式、不依赖渲染栈。
 */
interface TemplateCatalog {
    fun observe(): Flow<List<WatermarkTemplate>>
    suspend fun findById(id: String): WatermarkTemplate?
}

/** 只读内置模板目录，供不承担编辑能力的入口使用。 */
object BuiltinTemplateCatalog : TemplateCatalog {
    private val entries = BuiltinTemplateRegistry.entries

    override fun observe(): Flow<List<WatermarkTemplate>> = flowOf(entries)

    override suspend fun findById(id: String): WatermarkTemplate? = entries.firstOrNull { it.id == id }
}
