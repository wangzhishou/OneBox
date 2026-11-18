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

import com.t8rin.imagetoolbox.core.resources.R as CoreResourcesR
import com.wanbaohe.watermark.template.R

/**
 * 内置模板注册表：纯元数据，不含 WatermarkDocument。
 *
 * 新增内置模板时先在 BuiltinTemplateId 加枚举值，这里与
 * feature/watermarking 的 BuiltinTemplateDocumentProvider 都会因穷尽 when 编译失败，
 * 直到两侧都补齐。
 */
object BuiltinTemplateRegistry {
    /** 只控制排序与「最近使用」水位，新增内置模板时 +1，永不覆盖用户数据。 */
    const val REGISTRY_VERSION = 3

    val entries: List<WatermarkTemplate> = BuiltinTemplateId.entries.map(::metadata)

    private fun metadata(id: BuiltinTemplateId): WatermarkTemplate = when (id) {
        BuiltinTemplateId.COPYRIGHT_TILE -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_copyright,
        )

        BuiltinTemplateId.BRAND_LOGO -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_logo,
        )

        BuiltinTemplateId.SIMPLE_STAMP -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_stamp,
        )

        BuiltinTemplateId.PHOTO_TIME -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_time,
            requiredFields = setOf(WatermarkField.CAPTURED_AT),
        )

        BuiltinTemplateId.OUTDOOR -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_outdoor,
            requiredFields = setOf(
                WatermarkField.CAPTURED_AT,
                WatermarkField.GPS,
                WatermarkField.ALTITUDE,
            ),
        )

        BuiltinTemplateId.WEATHER_CHECKIN -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_weather,
            requiredFields = setOf(WatermarkField.CAPTURED_AT),
        )

        BuiltinTemplateId.COORDINATES -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_coordinates,
            requiredFields = setOf(WatermarkField.GPS),
        )

        BuiltinTemplateId.HIDDEN_COPYRIGHT -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_hidden,
        )

        BuiltinTemplateId.LEICA_CLASSIC -> preset(
            id = id,
            nameRes = R.string.watermark_template_leica_classic,
            requiredFields = setOf(
                WatermarkField.CAMERA_MAKE,
                WatermarkField.CAMERA_MODEL,
                WatermarkField.FOCAL_LENGTH,
                WatermarkField.APERTURE,
                WatermarkField.SHUTTER,
                WatermarkField.ISO,
                WatermarkField.CAPTURED_AT,
                WatermarkField.GPS,
            ),
        )

        BuiltinTemplateId.PRIVACY_REDACTION -> preset(
            id = id,
            nameRes = CoreResourcesR.string.watermark_preset_privacy,
        )
    }

    private fun preset(
        id: BuiltinTemplateId,
        nameRes: Int,
        requiredFields: Set<WatermarkField> = emptySet(),
    ) = WatermarkTemplate(
        id = id.id,
        nameRes = nameRes,
        category = TemplateCategory.QUICK,
        source = TemplateSource.BUILTIN,
        requiredFields = requiredFields,
    )
}
