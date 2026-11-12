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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.startsAtManualPageBreak
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.selection.CustomSizeDialog

enum class PagePanelSection {
    Canvas, Background
}

@Composable
fun PagesPanel(
    component: TextCardComponent,
    section: PagePanelSection,
    onSectionChange: (PagePanelSection) -> Unit,
) {
    GlassSegmentedButtonRow(
        options = PagePanelSection.entries,
        selectedOption = section,
        onOptionSelected = onSectionChange,
        label = { option ->
            Text(
                stringResource(
                    if (option == PagePanelSection.Canvas) {
                        R.string.textcard_tab_pages
                    } else {
                        R.string.textcard_tab_background
                    }
                )
            )
        },
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
    when (section) {
        PagePanelSection.Canvas -> CanvasPagesPanel(component)
        PagePanelSection.Background -> BackgroundPanel(component)
    }
}

@Composable
private fun CanvasPagesPanel(component: TextCardComponent) {
    var showCustomSizeDialog by remember { mutableStateOf(false) }
    var pageJumpText by remember { mutableStateOf("") }
    val layout = component.layout

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(CanvasSpec.builtIn, key = { it.ratioLabel }) { canvas ->
            CanvasCell(
                canvas = canvas,
                document = component.document,
                selected = component.document.canvas == canvas,
                onClick = { component.updateCanvas(canvas) }
            )
        }
        item(key = "custom") {
            val custom = component.document.canvas as? CanvasSpec.Custom
            val previewCanvas = custom ?: component.lastCustomCanvas ?: CanvasSpec.Portrait
            CanvasCell(
                canvas = previewCanvas,
                document = component.document,
                title = stringResource(R.string.textcard_canvas_custom),
                ratioLabel = custom?.ratioLabel
                    ?: component.lastCustomCanvas?.ratioLabel
                    ?: "+",
                selected = custom != null,
                onClick = { showCustomSizeDialog = true }
            )
        }
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
                text = stringResource(
                    when (mode) {
                        TextCardOutputMode.Pages -> R.string.textcard_output_pages
                        TextCardOutputMode.LongImage -> R.string.textcard_output_long_image
                    }
                ),
                textAlign = TextAlign.Center
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
    if (component.document.outputMode == TextCardOutputMode.Pages) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.textcard_page_number),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            GlassSwitch(
                checked = component.document.showPageNumber,
                onCheckedChange = component::updateShowPageNumber
            )
        }
    }
    if (component.document.outputMode == TextCardOutputMode.LongImage) {
        val requiredHeight = layout?.longImageRequiredHeight ?: 0
        val overBudget = requiredHeight > TextCardDocument.MAX_LONG_IMAGE_SIDE ||
            component.document.canvas.width.toLong() * requiredHeight >
            TextCardDocument.MAX_LONG_IMAGE_PIXELS
        if (layout?.longImage == null && (component.document.annotations.isEmpty() || overBudget)) {
            Text(
                text = stringResource(
                    R.string.textcard_long_image_too_large,
                    layout?.longImageRequiredHeight ?: 0
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (component.document.annotations.isNotEmpty()) {
            Text(
                text = stringResource(R.string.textcard_annotated_long_image_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Text(
            text = stringResource(R.string.textcard_long_image_page_break_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
    val pages = layout?.pages.orEmpty()
    val targetPage = pageJumpText.toIntOrNull()?.minus(1)?.takeIf { it in pages.indices }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.textcard_page_count, pages.size),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        if (pages.size > 1) {
            GlassOutlinedTextField(
                value = pageJumpText,
                onValueChange = { pageJumpText = it.filter(Char::isDigit).take(4) },
                placeholder = { Text(stringResource(R.string.textcard_page_number_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(88.dp)
            )
            TextButton(
                onClick = {
                    targetPage?.let(component::setCurrentPage)
                    pageJumpText = ""
                },
                enabled = targetPage != null
            ) {
                Text(stringResource(R.string.textcard_go_to_page))
            }
        }
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(pages, key = { index, _ -> index }) { index, page ->
            val selected = component.currentPage == index
            GlassCard(
                onClick = { component.setCurrentPage(index) },
                colors = if (selected) {
                    tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
                } else null,
                modifier = Modifier.width(92.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardCanvasPreview(
                        state = page,
                        cornerRadius = 6.dp,
                        interactive = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (page.startsAtManualPageBreak(component.document)) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.textcard_manual_page_break_short),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomSizeDialog) {
        CustomSizeDialog(
            initial = component.document.canvas as? CanvasSpec.Custom
                ?: component.lastCustomCanvas,
            onConfirm = { width, height ->
                component.updateCustomCanvas(width, height)
                showCustomSizeDialog = false
            },
            onDismiss = { showCustomSizeDialog = false }
        )
    }
}

@Composable
private fun CanvasCell(
    canvas: CanvasSpec,
    document: TextCardDocument,
    title: String = stringResource(canvas.titleRes),
    ratioLabel: String = canvas.ratioLabel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = if (selected) {
            tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
        } else null,
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimaryContainer)
        } else null,
        modifier = Modifier.width(116.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val fitHeight = maxWidth / maxHeight > canvas.aspectRatio
                val previewWidth = if (fitHeight) {
                    maxHeight * canvas.aspectRatio
                } else maxWidth
                val previewHeight = if (fitHeight) {
                    maxHeight
                } else maxWidth / canvas.aspectRatio
                CardCanvasPreview(
                    state = TextCardRenderState(
                        canvas = canvas,
                        background = document.background,
                        backgroundOpacity = document.backgroundOpacity
                    ),
                    cornerRadius = 6.dp,
                    interactive = false,
                    modifier = Modifier.size(previewWidth, previewHeight)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = ratioLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
