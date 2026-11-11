package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardExportFormat
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlin.math.roundToInt

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
    EnhancedModalBottomSheet(
        visible = visible,
        title = stringResource(R.string.textcard_export_title),
        onDismiss = { onDismiss() },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ConfirmButton(
                    text = stringResource(R.string.textcard_share_images),
                    enabled = canExport,
                    onClick = {
                        component.shareCard(currentPageOnly)
                        onDismiss()
                    }
                )
                ConfirmButton(
                    text = stringResource(R.string.textcard_save_images),
                    enabled = canExport,
                    onClick = {
                        component.saveCard(currentPageOnly)
                        onDismiss()
                    }
                )
            }
        },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                val dimensions = if (component.document.outputMode == TextCardOutputMode.LongImage) {
                    component.layout?.longImage?.canvas?.let { "${it.width}×${it.height}" }
                        ?: component.layout?.longImageRequiredHeight?.let { height ->
                            "${component.document.canvas.width}×$height"
                        }
                } else {
                    pages.firstOrNull()?.canvas?.let { "${it.width}×${it.height}" }
                }
                Text(
                    text = stringResource(
                        if (component.document.outputMode == TextCardOutputMode.Pages) {
                            R.string.textcard_export_pages_info
                        } else R.string.textcard_export_long_info,
                        pages.size,
                        dimensions.orEmpty()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (component.document.annotations.isNotEmpty() &&
                    component.document.outputMode == TextCardOutputMode.LongImage
                ) {
                    Text(
                        text = stringResource(R.string.textcard_annotated_long_image_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!component.isLayoutCurrent) {
                    Text(
                        text = stringResource(R.string.textcard_layout_updating),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.textcard_output_mode),
                    style = MaterialTheme.typography.titleSmall
                )
                GlassSegmentedButtonRow(
                    options = TextCardOutputMode.entries,
                    selectedOption = component.document.outputMode,
                    onOptionSelected = component::updateOutputMode,
                    label = { mode ->
                        Text(
                            stringResource(
                                if (mode == TextCardOutputMode.Pages) {
                                    R.string.textcard_output_pages
                                } else R.string.textcard_output_long_image
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (component.document.outputMode == TextCardOutputMode.Pages && pages.size > 1) {
                    Text(
                        text = stringResource(R.string.textcard_export_scope),
                        style = MaterialTheme.typography.titleSmall
                    )
                    GlassSegmentedButtonRow(
                        options = listOf(false, true),
                        selectedOption = currentPageOnly,
                        onOptionSelected = { currentPageOnly = it },
                        label = { currentOnly ->
                            Text(
                                stringResource(
                                    if (currentOnly) {
                                        R.string.textcard_export_current_page
                                    } else R.string.textcard_export_all_pages
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = stringResource(R.string.textcard_export_format),
                    style = MaterialTheme.typography.titleSmall
                )
                GlassSegmentedButtonRow(
                    options = TextCardExportFormat.entries,
                    selectedOption = component.document.exportFormat,
                    onOptionSelected = { component.updateExportSettings(format = it) },
                    label = { format ->
                        Text(
                            text = when (format) {
                                TextCardExportFormat.Jpg -> "JPG"
                                TextCardExportFormat.Png -> "PNG"
                                TextCardExportFormat.Webp -> "WEBP"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (component.document.exportFormat != TextCardExportFormat.Png) {
                    EnhancedSliderItem(
                        value = component.document.exportQuality.toFloat(),
                        title = stringResource(R.string.textcard_export_quality),
                        valueRange = 10f..100f,
                        onValueChange = {
                            component.updateExportSettings(quality = it.roundToInt())
                        },
                        valueSuffix = "%"
                    )
                }
            }
        }
    )
}
