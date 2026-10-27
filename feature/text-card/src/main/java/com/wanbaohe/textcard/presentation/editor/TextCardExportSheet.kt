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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.textcard.R
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
    EnhancedModalBottomSheet(
        visible = visible,
        title = stringResource(R.string.textcard_export_title),
        onDismiss = { onDismiss() },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CancelButton(onClick = onDismiss)
                ConfirmButton(
                    text = stringResource(R.string.textcard_export_action),
                    enabled = component.document.hasContent &&
                        (component.document.outputMode == TextCardOutputMode.Pages ||
                            component.layout?.longImage != null),
                    onClick = {
                        component.saveCard()
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
                val pages = component.layout?.pages.orEmpty()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.textcard_share_after_export),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    GlassSwitch(
                        checked = component.document.shareAfterSave,
                        onCheckedChange = { component.updateExportSettings(shareAfterSave = it) }
                    )
                }
            }
        }
    )
}
