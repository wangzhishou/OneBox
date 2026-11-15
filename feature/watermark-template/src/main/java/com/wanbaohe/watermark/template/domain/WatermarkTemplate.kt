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

import androidx.annotation.StringRes

/** 模板声明依赖的照片字段，宿主据此在字段缺失时做降级提示。 */
enum class WatermarkField {
    APERTURE,
    ISO,
    SHUTTER,
    FOCAL_LENGTH,
    FOCAL_LENGTH_35MM,
    CAMERA_MAKE,
    CAMERA_MODEL,
    LENS_MODEL,
    CAPTURED_AT,
    GPS,
    ALTITUDE,
    ORIENTATION,
}

enum class TemplateCategory { QUICK, CUSTOM }

enum class TemplateSource { BUILTIN, USER }

/**
 * 模板元数据。
 *
 * [payload] 只允许宿主写入与读取：共享模块只透传、不解析、不持久化。
 * 用户模板的 JSON 仍由 feature/watermarking 的 FileWatermarkPresetStore 保存，
 * 因此模板数据全局只有一份持久化真相。
 */
data class WatermarkTemplate(
    val id: String,
    val name: String = "",
    @StringRes val nameRes: Int = 0,
    val category: TemplateCategory,
    val source: TemplateSource,
    @androidx.annotation.DrawableRes val coverResId: Int = 0,
    val requiredFields: Set<WatermarkField> = emptySet(),
    val schemaVersion: Int = 2,
    val updatedAt: Long = 0L,
    val payload: String? = null,
    val needsUpgrade: Boolean = false,
)
