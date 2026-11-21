/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.preset

import com.shifenmiao.model.editor.LayerBlendMode
import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.shifenmiao.app.watermarking.domain.model.ExifFieldKey
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkFrame
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkHorizontalAlignment
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkLayout
import com.shifenmiao.app.watermarking.domain.model.WatermarkStyle
import com.shifenmiao.app.watermarking.domain.model.WatermarkValueSource
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import org.json.JSONArray
import org.json.JSONObject
import com.shifenmiao.app.watermarking.domain.model.PhotoMount
import com.shifenmiao.app.watermarking.domain.model.PhotoMountRatio
import com.shifenmiao.app.watermarking.domain.model.PhotoMountStyle

object WatermarkPresetCodec {
    fun encode(preset: WatermarkPreset): String = JSONObject().apply {
        put("schemaVersion", preset.document.schemaVersion)
        put("id", preset.id)
        put("name", preset.name)
        put("updatedAt", preset.updatedAt)
        put("hiddenEnabled", preset.document.hiddenWatermark.enabled)
        put("hiddenLSB", preset.document.hiddenWatermark.isLSB)
        preset.document.mount?.let { mount ->
            put("mount", JSONObject().apply {
                put("style", mount.style.name)
                put("widthRatio", mount.widthRatio.toDouble())
                put("ratio", mount.ratio.name)
            })
        }
        put("frame", JSONObject().apply {
            when (val frame = preset.document.frame) {
                WatermarkFrame.InPlace -> put("type", "in_place")
                is WatermarkFrame.BottomBand -> {
                    put("type", "bottom_band")
                    put("heightRatio", frame.heightRatio.toDouble())
                    put("color", frame.color)
                }
            }
        })
        put("elements", JSONArray().apply {
            preset.document.elements.forEach { put(encodeElement(it)) }
        })
    }.toString()

    fun decode(source: String): WatermarkPreset {
        val json = JSONObject(source)
        val version = json.getInt("schemaVersion")
        require(version <= WatermarkDocument.CURRENT_SCHEMA_VERSION) {
            "Unsupported watermark preset schema: $version"
        }
        val elementsJson = json.getJSONArray("elements")
        val elements = buildList {
            for (index in 0 until elementsJson.length()) {
                add(decodeElement(elementsJson.getJSONObject(index)))
            }
        }
        // v1 文档没有 frame 字段，统一补 InPlace；v2 起按 type 还原。
        val frameJson = json.optJSONObject("frame")
        val frame = when (frameJson?.optString("type")) {
            "bottom_band" -> WatermarkFrame.BottomBand(
                heightRatio = frameJson.optDouble(
                    "heightRatio",
                    WatermarkFrame.BottomBand.DEFAULT_HEIGHT_RATIO.toDouble(),
                ).toFloat(),
                color = frameJson.optInt("color", -1),
            )
            else -> WatermarkFrame.InPlace
        }
        return WatermarkPreset(
            id = json.getString("id"),
            name = json.optString("name"),
            document = WatermarkDocument(
                // 旧文档补完当前版本默认字段后，下次保存写出最新结构。
                schemaVersion = WatermarkDocument.CURRENT_SCHEMA_VERSION,
                frame = frame,
                elements = if (version < 4) elements.ifEmpty { WatermarkDocument().elements } else elements,
                mount = json.optJSONObject("mount")?.let { data ->
                    PhotoMountStyle.entries.firstOrNull { it.name == data.optString("style") }?.let { style ->
                        PhotoMount(
                            style = style,
                            widthRatio = data.optDouble("widthRatio", style.defaultWidth.toDouble())
                                .toFloat().takeIf { it.isFinite() } ?: style.defaultWidth,
                            ratio = PhotoMountRatio.entries.firstOrNull { it.name == data.optString("ratio") }
                                ?: PhotoMountRatio.NATURAL,
                        )
                    }
                },
                hiddenWatermark = HiddenWatermarkParams(
                    enabled = json.optBoolean("hiddenEnabled"),
                    isLSB = json.optBoolean("hiddenLSB", true),
                ),
            ),
            updatedAt = json.optLong("updatedAt"),
        )
    }

    private fun encodeElement(element: WatermarkElement) = JSONObject().apply {
        put("id", element.id)
        put("content", encodeContent(element.content))
        put("transform", JSONObject().apply {
            put("centerX", element.transform.centerX.toDouble())
            put("centerY", element.transform.centerY.toDouble())
            put("scale", element.transform.scale.toDouble())
            put("rotation", element.transform.rotation.toDouble())
            put("alpha", element.transform.alpha.toDouble())
            put("visible", element.transform.visible)
            put("locked", element.transform.locked)
            put("blendMode", element.transform.blendMode.name)
            put("flipHorizontal", element.transform.flipHorizontal)
            put("flipVertical", element.transform.flipVertical)
        })
        put("style", JSONObject().apply {
            put("color", element.style.color)
            put("backgroundColor", element.style.backgroundColor)
            put("sizeRatio", element.style.sizeRatio.toDouble())
            put("maxWidthRatio", element.style.maxWidthRatio.toDouble())
            put("font", element.style.font?.let(::encodeFont) ?: JSONObject.NULL)
            put("bold", element.style.bold)
        })
        put("repeated", element.layout.isRepeated)
        put("repeatSpacing", element.layout.repeatSpacing.toDouble())
        put("overlayMode", element.overlayMode.value)
        put("anchor", element.anchor.name)
        put("horizontalAlignment", element.horizontalAlignment.name)
    }

    private fun decodeElement(json: JSONObject): WatermarkElement {
        val transform = json.getJSONObject("transform")
        val style = json.getJSONObject("style")
        val blendModeName = transform.optString("blendMode")
        return WatermarkElement(
            id = json.getString("id"),
            content = decodeContent(json.getJSONObject("content")),
            transform = LayerTransform(
                centerX = transform.optDouble("centerX", 0.5).toFloat(),
                centerY = transform.optDouble("centerY", 0.85).toFloat(),
                scale = transform.optDouble("scale", 1.0).toFloat(),
                rotation = transform.optDouble("rotation", 0.0).toFloat(),
                alpha = transform.optDouble("alpha", 1.0).toFloat(),
                visible = transform.optBoolean("visible", true),
                locked = transform.optBoolean("locked", false),
                blendMode = runCatching { LayerBlendMode.valueOf(blendModeName) }
                    .getOrDefault(LayerBlendMode.Normal),
                flipHorizontal = transform.optBoolean("flipHorizontal"),
                flipVertical = transform.optBoolean("flipVertical"),
            ),
            style = WatermarkStyle(
                color = style.optInt("color", -16777216),
                backgroundColor = style.optInt("backgroundColor", 0),
                sizeRatio = style.optDouble("sizeRatio", 0.08).toFloat(),
                maxWidthRatio = style.optDouble("maxWidthRatio", 0.9).toFloat(),
                font = style.optJSONObject("font")?.let(::decodeFont),
                bold = style.optBoolean("bold"),
            ),
            layout = WatermarkLayout(
                isRepeated = json.optBoolean("repeated"),
                repeatSpacing = json.optDouble("repeatSpacing", 1.8).toFloat(),
            ),
            overlayMode = BlendingMode.newEntries.firstOrNull {
                it.value == json.optInt("overlayMode")
            } ?: BlendingMode.SrcOver,
            anchor = runCatching { WatermarkAnchor.valueOf(json.optString("anchor")) }
                .getOrDefault(WatermarkAnchor.PHOTO),
            horizontalAlignment = runCatching {
                WatermarkHorizontalAlignment.valueOf(json.optString("horizontalAlignment"))
            }.getOrDefault(WatermarkHorizontalAlignment.CENTER),
        )
    }

    private fun encodeContent(content: WatermarkContent) = JSONObject().apply {
        when (content) {
            is WatermarkContent.Text -> {
                put("type", "text")
                put("text", content.text)
            }
            is WatermarkContent.Image -> {
                put("type", "image")
                put("imageData", content.imageData)
                put("size", content.size.toDouble())
                put("builtinResId", content.builtinResId)
            }
            is WatermarkContent.Stamp -> {
                put("type", "stamp")
                put("text", content.text)
            }
            is WatermarkContent.Timestamp -> {
                put("type", "timestamp")
                put("format", content.format)
                put("usePhotoTime", content.usePhotoTime)
                put("fallbackText", content.fallbackText)
            }
            is WatermarkContent.Altitude -> {
                put("type", "altitude")
                putSource(content.source, content.manualValue)
                put("useFeet", content.useFeet)
            }
            is WatermarkContent.Weather -> {
                put("type", "weather")
                putSource(content.source, content.manualValue)
                put("includeTemperature", content.includeTemperature)
                put("includeObservationTime", content.includeObservationTime)
            }
            is WatermarkContent.Coordinates -> {
                put("type", "coordinates")
                putSource(content.source, content.manualValue)
                put("useDecimalFormat", content.useDecimalFormat)
            }
            is WatermarkContent.CameraParams -> {
                put("type", "camera_params")
                put("fields", JSONArray().apply { content.fields.forEach { put(it.name) } })
                put("separator", content.separator)
                put("omitMissing", content.omitMissing)
                put("manualOverrides", JSONObject().apply {
                    content.manualOverrides.forEach { (key, value) -> put(key, value) }
                })
            }
            is WatermarkContent.Divider -> {
                put("type", "divider")
                put("thicknessRatio", content.thicknessRatio.toDouble())
                put("heightRatio", content.heightRatio.toDouble())
            }
        }
    }

    private fun decodeContent(json: JSONObject): WatermarkContent = when (json.getString("type")) {
        "image" -> WatermarkContent.Image(
            imageData = json.optString("imageData"),
            size = json.optDouble("size", 0.2).toFloat(),
            builtinResId = json.optInt("builtinResId"),
        )
        "camera_params" -> {
            val fieldsJson = json.optJSONArray("fields")
            val fields = buildList {
                if (fieldsJson != null) {
                    for (index in 0 until fieldsJson.length()) {
                        runCatching { ExifFieldKey.valueOf(fieldsJson.getString(index)) }
                            .getOrNull()?.let(::add)
                    }
                }
            }.ifEmpty { WatermarkContent.CameraParams().fields }
            val overridesJson = json.optJSONObject("manualOverrides")
            val overrides = buildMap {
                overridesJson?.keys()?.forEach { key -> put(key, overridesJson.optString(key)) }
            }
            WatermarkContent.CameraParams(
                fields = fields,
                separator = json.optString("separator", "  "),
                omitMissing = json.optBoolean("omitMissing", true),
                manualOverrides = overrides,
            )
        }
        "divider" -> WatermarkContent.Divider(
            thicknessRatio = json.optDouble("thicknessRatio", 0.015).toFloat(),
            heightRatio = json.optDouble("heightRatio", 0.8).toFloat(),
        )
        "stamp" -> WatermarkContent.Stamp(json.optString("text"))
        "timestamp" -> WatermarkContent.Timestamp(
            format = json.optString("format", "yyyy-MM-dd HH:mm"),
            usePhotoTime = json.optBoolean("usePhotoTime", true),
            fallbackText = json.optString("fallbackText"),
        )
        "altitude" -> WatermarkContent.Altitude(
            source = decodeSource(json),
            manualValue = json.optString("manualValue"),
            useFeet = json.optBoolean("useFeet"),
        )
        "weather" -> WatermarkContent.Weather(
            source = decodeSource(json),
            manualValue = json.optString("manualValue"),
            includeTemperature = json.optBoolean("includeTemperature", true),
            includeObservationTime = json.optBoolean("includeObservationTime"),
        )
        "coordinates" -> WatermarkContent.Coordinates(
            source = decodeSource(json),
            manualValue = json.optString("manualValue"),
            useDecimalFormat = json.optBoolean("useDecimalFormat", true),
        )
        else -> WatermarkContent.Text(json.optString("text"))
    }

    private fun JSONObject.putSource(source: WatermarkValueSource, manualValue: String) {
        put("source", source.name)
        put("manualValue", manualValue)
    }

    private fun decodeSource(json: JSONObject): WatermarkValueSource = runCatching {
        WatermarkValueSource.valueOf(json.optString("source"))
    }.getOrDefault(WatermarkValueSource.PHOTO)

    private fun encodeFont(font: FontType): JSONObject = when (font) {
        is FontType.Resource -> JSONObject().put("resource", font.resId)
        is FontType.File -> JSONObject().put("file", font.path)
    }

    private fun decodeFont(json: JSONObject): FontType? = when {
        json.has("resource") -> FontType.Resource(json.getInt("resource"))
        json.has("file") -> FontType.File(json.getString("file"))
        else -> null
    }
}
