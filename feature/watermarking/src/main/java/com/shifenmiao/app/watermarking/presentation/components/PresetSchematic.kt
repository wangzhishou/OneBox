/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.Icons as CoreIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.model.sampleText
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId
import com.wanbaohe.watermark.template.R as TemplateR

/**
 * 预设缩略图：给引导页和编辑器「我的模板」共用，避免两处各画一套版式。
 * 徕卡白带与隐私打码走专用示意，其余按 document 里的元素位置画简化标签。
 */
@Composable
fun PresetSchematic(
    preset: WatermarkPreset,
    modifier: Modifier = Modifier,
    height: Dp = 82.dp,
) {
    if (preset.id == BuiltinTemplateId.LEICA_CLASSIC.id) {
        LeicaPresetSchematic(modifier, height)
        return
    }
    if (preset.id == BuiltinTemplateId.PRIVACY_REDACTION.id) {
        SchematicBackdrop(modifier, height) {
            Icon(
                imageVector = CoreIcons.Outlined.LineAutoAwesomeMosaic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center).size(48.dp),
            )
        }
        return
    }
    val elements = preset.document.elements.take(3)
    SchematicBackdrop(modifier, height) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        elements.forEach { element ->
            val spots = if (element.layout.isRepeated) {
                listOf(
                    -0.05f to 0.12f, 0.35f to 0.12f, 0.75f to 0.12f,
                    0.15f to 0.4f, 0.55f to 0.4f, 0.95f to 0.4f,
                    -0.05f to 0.68f, 0.35f to 0.68f, 0.75f to 0.68f,
                    0.15f to 0.96f, 0.55f to 0.96f, 0.95f to 0.96f,
                )
            } else {
                listOf(element.transform.centerX to element.transform.centerY)
            }
            spots.forEach { (x, y) ->
                SchematicLabel(
                    text = presetLabel(element),
                    color = Color(element.style.color).copy(
                        alpha = Color(element.style.color).alpha * element.transform.alpha,
                    ),
                    x = x,
                    y = y,
                    rotation = element.transform.rotation,
                    width = maxWidth,
                    height = maxHeight,
                )
            }
        }
    }
    }
}

@Composable
private fun LeicaPresetSchematic(modifier: Modifier, height: Dp) {
    SchematicBackdrop(modifier, height) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(30.dp)
                .background(Color.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.width(30.dp).height(3.dp).background(Color(0xFF202020)))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(23.dp).height(2.dp).background(Color(0xFF888888)))
            }
            Image(
                painter = painterResource(TemplateR.drawable.leica_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.width(1.dp).height(18.dp).background(Color(0xFFCCCCCC)))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(Modifier.width(30.dp).height(3.dp).background(Color(0xFF202020)))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(23.dp).height(2.dp).background(Color(0xFF888888)))
            }
        }
    }
}

/** 与滤镜缩略图同源的预览底图，盖一层很淡的遮罩保证白色水印文字仍能看清。 */
@Composable
private fun SchematicBackdrop(
    modifier: Modifier,
    height: Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.filter_preview_source),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(Color(0x33000000)))
        content()
    }
}

@Composable
private fun SchematicLabel(
    text: String,
    color: Color,
    x: Float,
    y: Float,
    rotation: Float,
    width: Dp,
    height: Dp,
) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .offset(x = width * x - 36.dp, y = height * y - 8.dp)
            .width(72.dp)
            .graphicsLayer { rotationZ = rotation },
    )
}

@Composable
private fun presetLabel(element: WatermarkElement): String {
    val content = element.content
    if (content is WatermarkContent.Image && content.imageData.isBlank() && content.builtinResId == 0) {
        return stringResource(R.string.watermark_pick_logo)
    }
    return content.sampleText()
}

@Composable
internal fun presetTypeNames(preset: WatermarkPreset): String = stringResource(
    com.wanbaohe.privacy.redaction.R.string.privacy_redaction_subtitle
).takeIf { preset.id == BuiltinTemplateId.PRIVACY_REDACTION.id }
    ?: preset.document.elements.map { it.content.kind.title() }.joinToString(" · ")
