/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.preset

import com.shifenmiao.app.watermarking.domain.model.PhotoMount

import com.shifenmiao.app.watermarking.domain.model.ExifFieldKey
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkFrame
import com.shifenmiao.app.watermarking.domain.model.WatermarkHorizontalAlignment
import com.shifenmiao.app.watermarking.domain.model.WatermarkLayout
import com.shifenmiao.app.watermarking.domain.model.WatermarkStyle
import com.shifenmiao.app.watermarking.domain.model.defaultWatermarkElement
import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId
import com.wanbaohe.watermark.template.domain.BuiltinTemplateRegistry
import com.wanbaohe.watermark.template.R as TemplateR

/**
 * 内置预设的 document 提供者。
 *
 * 与 [BuiltinTemplateRegistry] 共用 [BuiltinTemplateId]：新增模板时两侧都要补分支，
 * 任何一侧漏掉都会因穷尽 when 在编译期报错，不需要运行时 id 对齐自检。
 */
object BuiltinWatermarkPresets {
    const val DOCUMENT_USE = "document_use"
    const val SIGNATURE = "work_signature"
    const val PHOTO_MOUNT = "photo_mount"

    private val mountPreset = WatermarkPreset(
        id = PHOTO_MOUNT,
        nameRes = R.string.watermark_photo_mount,
        document = WatermarkDocument(elements = emptyList(), mount = PhotoMount()),
        isBuiltin = true,
    )

    fun textPreset(id: String, nameRes: Int, text: String): WatermarkPreset {
        require(id == DOCUMENT_USE || id == SIGNATURE)
        val tile = id == DOCUMENT_USE
        val base = defaultWatermarkElement(WatermarkContent.Text(text.trim()))
        val layer = base.copy(
            transform = base.transform.copy(
                centerX = if (tile) 0.5f else 0.78f,
                centerY = if (tile) 0.5f else 0.92f,
                rotation = if (tile) -28f else 0f,
                alpha = if (tile) 0.42f else 0.94f,
            ),
            style = base.style.copy(
                color = if (tile) 0xFF535967.toInt() else -1,
                backgroundColor = if (tile) 0 else 0x66000000,
                sizeRatio = if (tile) 0.038f else 0.035f,
                maxWidthRatio = if (tile) 0.6f else 0.38f,
            ),
            layout = WatermarkLayout(isRepeated = tile, repeatSpacing = 1.4f),
        )
        return WatermarkPreset(id = id, nameRes = nameRes, document = document(listOf(layer)), isBuiltin = true)
    }

    private val featuredIds = listOf(
        BuiltinTemplateId.COPYRIGHT_TILE,
        BuiltinTemplateId.LEICA_CLASSIC,
    )

    val entries: List<WatermarkPreset> by lazy {
        BuiltinTemplateId.entries.map(::presetFor)
    }

    val featuredEntries: List<WatermarkPreset> by lazy {
        featuredIds.map { id -> entries.first { it.id == id.id } } + mountPreset
    }

    fun findById(id: String): WatermarkPreset? =
        if (id == PHOTO_MOUNT) mountPreset else entries.firstOrNull { it.id == id }

    private fun presetFor(id: BuiltinTemplateId): WatermarkPreset {
        val metadata = BuiltinTemplateRegistry.entries.first { it.id == id.id }
        return WatermarkPreset(
            id = id.id,
            nameRes = metadata.nameRes,
            document = documentFor(id),
            isBuiltin = true,
        )
    }

    fun documentFor(id: BuiltinTemplateId): WatermarkDocument = when (id) {
        BuiltinTemplateId.COPYRIGHT_TILE -> document(
            elements = listOf(
                defaultWatermarkElement(WatermarkContent.Text("© OneBox")).copy(
                    transform = LayerTransform(centerX = 0.5f, centerY = 0.5f, rotation = -30f, alpha = 0.26f),
                    style = WatermarkStyle(color = -1, sizeRatio = 0.075f),
                    layout = WatermarkLayout(isRepeated = true, repeatSpacing = 2f),
                )
            ),
        )

        BuiltinTemplateId.BRAND_LOGO -> document(
            elements = listOf(
                defaultWatermarkElement(WatermarkContent.Image()).copy(
                    transform = LayerTransform(centerX = 0.82f, centerY = 0.86f, alpha = 0.85f),
                    layout = WatermarkLayout(isRepeated = false),
                )
            ),
        )

        BuiltinTemplateId.SIMPLE_STAMP -> document(
            elements = listOf(defaultWatermarkElement(WatermarkContent.Stamp("OneBox"))),
        )

        BuiltinTemplateId.PHOTO_TIME -> document(
            elements = listOf(defaultWatermarkElement(WatermarkContent.Timestamp())),
        )

        BuiltinTemplateId.OUTDOOR -> document(
            elements = listOf(
                infoElement(WatermarkContent.Timestamp(), 0.28f, 0.88f),
                infoElement(WatermarkContent.Coordinates(), 0.72f, 0.88f),
                infoElement(WatermarkContent.Altitude(), 0.72f, 0.95f),
            ),
        )

        BuiltinTemplateId.WEATHER_CHECKIN -> document(
            elements = listOf(
                infoElement(WatermarkContent.Weather(), 0.32f, 0.9f),
                infoElement(WatermarkContent.Timestamp(), 0.76f, 0.9f),
            ),
        )

        BuiltinTemplateId.COORDINATES -> document(
            elements = listOf(defaultWatermarkElement(WatermarkContent.Coordinates())),
        )

        BuiltinTemplateId.HIDDEN_COPYRIGHT -> document(
            elements = listOf(
                defaultWatermarkElement(WatermarkContent.Text("© OneBox")).copy(
                    transform = LayerTransform(centerX = 0.5f, centerY = 0.85f, visible = false),
                    layout = WatermarkLayout(isRepeated = false),
                )
            ),
            hidden = HiddenWatermarkParams(enabled = true),
        )

        BuiltinTemplateId.LEICA_CLASSIC -> leicaDocument()

        BuiltinTemplateId.PRIVACY_REDACTION -> document(elements = emptyList())
    }

    /**
     * 旧相机水印「徕卡经典」收敛成的内置模板：
     * 原图下方扩一条白底参数带，带内左机型/时间、中间 logo、竖分隔线、右参数/GPS。
     * 所有元素锚定 [WatermarkAnchor.BAND]，坐标只在白带 rect 内归一化，不随原图宽高比漂移。
     */
    private fun leicaDocument(): WatermarkDocument = document(
        frame = WatermarkFrame.BottomBand(color = -1),
        elements = listOf(
            bandElement(
                content = WatermarkContent.CameraParams(
                    fields = listOf(ExifFieldKey.CAMERA_MAKE, ExifFieldKey.CAMERA_MODEL),
                    separator = " ",
                ),
                centerX = 24f / 1080f,
                centerY = 0.34f,
                sizeRatio = 0.257f,
                bold = true,
                horizontalAlignment = WatermarkHorizontalAlignment.START,
                maxWidthRatio = 0.46f,
            ),
            bandElement(
                content = WatermarkContent.Timestamp(format = "yyyy.MM.dd HH:mm:ss"),
                centerX = 24f / 1080f,
                centerY = 0.63f,
                sizeRatio = 0.161f,
                color = 0xFF666666.toInt(),
                horizontalAlignment = WatermarkHorizontalAlignment.START,
                maxWidthRatio = 0.46f,
            ),
            WatermarkElement(
                content = WatermarkContent.Image(
                    builtinResId = TemplateR.drawable.leica_logo,
                    size = 0.513f,
                ),
                transform = LayerTransform(centerX = 0.55f, centerY = 0.5f, alpha = 1f),
                style = WatermarkStyle(sizeRatio = 0.513f),
                anchor = WatermarkAnchor.BAND,
            ),
            bandElement(
                content = WatermarkContent.Divider(thicknessRatio = 2f / 120f, heightRatio = 0.587f),
                centerX = 0.60f,
                centerY = 0.5f,
                sizeRatio = 0.015f,
                color = 0xFFE0E0E0.toInt(),
            ),
            bandElement(
                content = WatermarkContent.CameraParams(
                    fields = listOf(
                        ExifFieldKey.FOCAL_LENGTH,
                        ExifFieldKey.APERTURE,
                        ExifFieldKey.SHUTTER,
                        ExifFieldKey.ISO,
                    ),
                    separator = " ",
                ),
                centerX = 0.60f + 24f / 1080f,
                centerY = 0.34f,
                sizeRatio = 0.257f,
                bold = true,
                horizontalAlignment = WatermarkHorizontalAlignment.START,
                maxWidthRatio = 0.35f,
            ),
            bandElement(
                content = WatermarkContent.Coordinates(useDecimalFormat = false),
                centerX = 0.60f + 24f / 1080f,
                centerY = 0.63f,
                sizeRatio = 0.161f,
                color = 0xFF666666.toInt(),
                horizontalAlignment = WatermarkHorizontalAlignment.START,
                maxWidthRatio = 0.35f,
            ),
        ),
    )

    private fun bandElement(
        content: WatermarkContent,
        centerX: Float,
        centerY: Float,
        sizeRatio: Float,
        color: Int = 0xFF1A1A1A.toInt(),
        bold: Boolean = false,
        horizontalAlignment: WatermarkHorizontalAlignment = WatermarkHorizontalAlignment.CENTER,
        maxWidthRatio: Float = 0.9f,
    ) = WatermarkElement(
        content = content,
        transform = LayerTransform(centerX = centerX, centerY = centerY, alpha = 1f),
        style = WatermarkStyle(
            color = color,
            backgroundColor = 0,
            sizeRatio = sizeRatio,
            maxWidthRatio = maxWidthRatio,
            bold = bold,
        ),
        layout = WatermarkLayout(isRepeated = false),
        overlayMode = BlendingMode.SrcOver,
        anchor = WatermarkAnchor.BAND,
        horizontalAlignment = horizontalAlignment,
    )

    private fun infoElement(
        content: WatermarkContent,
        centerX: Float,
        centerY: Float,
    ) = defaultWatermarkElement(content).copy(
        transform = LayerTransform(centerX = centerX, centerY = centerY, alpha = 0.9f),
        style = WatermarkStyle(color = -1, backgroundColor = 0x66000000, sizeRatio = 0.042f),
        layout = WatermarkLayout(isRepeated = false),
    )

    private fun document(
        elements: List<WatermarkElement>,
        frame: WatermarkFrame = WatermarkFrame.InPlace,
        hidden: HiddenWatermarkParams = HiddenWatermarkParams(),
    ) = WatermarkDocument(
        frame = frame,
        elements = elements,
        hiddenWatermark = hidden,
    )
}
