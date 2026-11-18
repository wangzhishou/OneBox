/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportCapabilities
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportConfig
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportSummary
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.wanbaohe.core.ui.widget.export.ImageExportSection
import com.wanbaohe.core.ui.widget.export.ImageExportSheet
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardExportFormat
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

@Composable
fun TextCardExportSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    var currentPageOnly by rememberSaveable(component.document.outputMode) {
        mutableStateOf(false)
    }
    val pages = component.layout?.pages.orEmpty()
    val stitchedHeight = component.layout?.longImageRequiredHeight ?: 0
    val stitchedWithinBudget = component.document.canvas.width.toLong() * stitchedHeight <=
        TextCardDocument.MAX_LONG_IMAGE_PIXELS && stitchedHeight <= TextCardDocument.MAX_LONG_IMAGE_SIDE
    val canExport = component.document.hasContent && component.isLayoutCurrent && when (component.document.outputMode) {
        TextCardOutputMode.Pages -> pages.isNotEmpty()
        TextCardOutputMode.LongImage -> if (component.document.annotations.isNotEmpty()) {
            pages.isNotEmpty() && stitchedWithinBudget
        } else {
            component.layout?.longImage != null
        }
    }
    val layoutPending = component.isLayoutComputing || !component.isLayoutCurrent
    val outputWidth = if (component.document.outputMode == TextCardOutputMode.LongImage) {
        component.layout?.longImage?.canvas?.width ?: component.document.canvas.width
    } else {
        pages.firstOrNull()?.canvas?.width
    }
    val outputHeight = if (component.document.outputMode == TextCardOutputMode.LongImage) {
        component.layout?.longImage?.canvas?.height ?: stitchedHeight.takeIf { it > 0 }
    } else {
        pages.firstOrNull()?.canvas?.height
    }
    val exportItemCount = when {
        component.document.outputMode == TextCardOutputMode.LongImage -> 1
        currentPageOnly -> 1
        else -> pages.size
    }
    val config = ImageExportConfig(
        format = component.document.exportFormat.toImageFormat(),
        quality = component.document.exportQuality,
    )

    ImageExportSheet(
        visible = visible,
        title = stringResource(R.string.textcard_export_title),
        config = config,
        capabilities = ImageExportCapabilities(),
        summary = ImageExportSummary(
            itemCount = exportItemCount,
            width = outputWidth,
            height = outputHeight,
        ),
        onConfigChange = { updated ->
            component.updateExportSettings(
                format = updated.format.toTextCardExportFormat(),
                quality = updated.quality,
            )
        },
        onDismiss = onDismiss,
        onSave = {
            component.saveCard(currentPageOnly)
            onDismiss()
        },
        onShare = {
            component.shareCard(currentPageOnly)
            onDismiss()
        },
        saveEnabled = canExport && !component.isSaving,
        shareEnabled = canExport && !component.isSaving,
        blockingMessage = component.layoutError?.let {
            stringResource(R.string.textcard_layout_failed_short)
        },
        content = {
            if (layoutPending) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.textcard_export_preparing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ImageExportSection(title = stringResource(R.string.textcard_output_mode)) {
                GlassSegmentedButtonRow(
                    options = TextCardOutputMode.entries,
                    selectedOption = component.document.outputMode,
                    onOptionSelected = component::updateOutputMode,
                    label = { mode ->
                        Text(
                            stringResource(
                                if (mode == TextCardOutputMode.Pages) {
                                    R.string.textcard_output_pages
                                } else {
                                    R.string.textcard_output_long_image
                                }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (component.document.annotations.isNotEmpty() &&
                    component.document.outputMode == TextCardOutputMode.LongImage
                ) {
                    Text(
                        text = stringResource(R.string.textcard_annotated_long_image_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (component.document.outputMode == TextCardOutputMode.Pages && pages.size > 1) {
                ImageExportSection(title = stringResource(R.string.textcard_export_scope)) {
                    GlassSegmentedButtonRow(
                        options = listOf(false, true),
                        selectedOption = currentPageOnly,
                        onOptionSelected = { currentPageOnly = it },
                        label = { currentOnly ->
                            Text(
                                stringResource(
                                    if (currentOnly) {
                                        R.string.textcard_export_current_page
                                    } else {
                                        R.string.textcard_export_all_pages
                                    }
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    )
}

private fun TextCardExportFormat.toImageFormat(): ImageFormat = when (this) {
    TextCardExportFormat.Jpg -> ImageFormat.Jpg
    TextCardExportFormat.Png -> ImageFormat.Png.Lossless
    TextCardExportFormat.Webp -> ImageFormat.Webp.Lossy
}

private fun ImageFormat.toTextCardExportFormat(): TextCardExportFormat = when (this) {
    is ImageFormat.Png -> TextCardExportFormat.Png
    is ImageFormat.Webp -> TextCardExportFormat.Webp
    else -> TextCardExportFormat.Jpg
}
