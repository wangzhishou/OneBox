package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.rememberPdfTextStyle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.model.Position
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.ImageUtils.rememberPdfPages
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.controls.selection.PositionSelector
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.PdfPagePreview
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers.screenLogic.PageNumbersPdfToolComponent

@Composable
fun PageNumbersPdfToolContent(
    component: PageNumbersPdfToolComponent,
) {
    val pageCount by rememberPdfPages(component.uri)
    val params = component.params

    BasePdfToolContent(
        component = component,
        contentPicker = rememberFilePicker(
            mimeType = MimeType.Pdf,
            onSuccess = component::setUri,
        ),
        isPickedAlready = component.initialUri != null,
        canShowScreenData = component.uri != null,
        title = stringResource(R.string.page_numbers),
        canSave = params.labelFormat.isNotBlank(),
        imagePreview = {
            PdfPagePreview(
                uri = component.uri,
                pageCount = pageCount,
            ) { page ->
                PageNumberOverlay(
                    text = params.labelFormat
                        .replace("{n}", (page + 1).toString())
                        .replace("{total}", pageCount.toString()),
                    position = params.position,
                    color = Color(params.color),
                    fontSize = params.fontSize,
                    pageWidthPt = component.pageSizes.getOrNull(page)?.width ?: 0,
                    pageHeightPt = component.pageSizes.getOrNull(page)?.height ?: 0,
                )
            }
        },
        placeImagePreview = true,
        showImagePreviewAsStickyHeader = true,
        controls = {
            RoundedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = params.labelFormat,
                onValueChange = { component.updateParams(params.copy(labelFormat = it)) },
                label = { Text(stringResource(R.string.page_number_format)) },
                supportingText = { Text(stringResource(R.string.page_number_format_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("{n}", "{total}").forEach { token ->
                    EnhancedChip(
                        selected = false,
                        onClick = {
                            component.updateParams(
                                params.copy(labelFormat = params.labelFormat + token)
                            )
                        },
                        selectedColor = MaterialTheme.colorScheme.primaryContainer,
                        label = { Text(token) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            EnhancedSliderItem(
                value = params.fontSize,
                title = stringResource(R.string.page_number_size),
                valueRange = 8f..80f,
                internalStateTransformation = Float::roundToInt,
                onValueChange = { component.updateParams(params.copy(fontSize = it)) },
                shape = ShapeDefaults.large,
            )
            Spacer(Modifier.height(8.dp))
            PositionSelector(
                value = params.position,
                onValueChange = { component.updateParams(params.copy(position = it)) },
                color = Color.Unspecified,
            )
            Spacer(Modifier.height(8.dp))
            ColorRowSelector(
                value = Color(params.color),
                onValueChange = { component.updateParams(params.copy(color = it.toArgb())) },
                title = stringResource(R.string.text_color),
                modifier = Modifier.container(shape = ShapeDefaults.large),
            )
        },
        onFilledPassword = { component.setUri(component.uri) },
    )
}

@Composable
private fun BoxScope.PageNumberOverlay(
    text: String,
    position: Position,
    color: Color,
    fontSize: Float,
    pageWidthPt: Int,
    pageHeightPt: Int,
) {
    val alignment = when (position) {
        Position.TopLeft -> Alignment.TopStart
        Position.TopCenter -> Alignment.TopCenter
        Position.TopRight -> Alignment.TopEnd
        Position.CenterLeft -> Alignment.CenterStart
        Position.Center -> Alignment.Center
        Position.CenterRight -> Alignment.CenterEnd
        Position.BottomLeft -> Alignment.BottomStart
        Position.BottomCenter -> Alignment.BottomCenter
        Position.BottomRight -> Alignment.BottomEnd
    }
    val pdfTextStyle = rememberPdfTextStyle()
    BoxWithConstraints(Modifier.matchParentSize()) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val widthPt = pageWidthPt.takeIf { it > 0 } ?: 595
        val heightPt = pageHeightPt.takeIf { it > 0 } ?: 842
        val horizontalPadding = with(density) { (10f * maxWidth.toPx() / widthPt).toDp() }
        val verticalPadding = with(density) { (20f * maxHeight.toPx() / heightPt).toDp() }
        val targetWidth = maxWidth * fontSize / 100f
        val sample = text.ifBlank { " " }
        val scaledFontSize = run {
            val textWidth = textMeasurer.measure(
                text = sample,
                style = pdfTextStyle.copy(fontSize = 100.sp),
                maxLines = 1,
                softWrap = false,
            ).size.width.coerceAtLeast(1)
            with(density) { 100.sp * (targetWidth.toPx() / textWidth) }
        }
        val textVerticalOffsetPx = run {
            val textLayout = textMeasurer.measure(
                text = sample,
                style = pdfTextStyle.copy(fontSize = scaledFontSize),
                maxLines = 1,
                softWrap = false,
            )
            var top = Float.POSITIVE_INFINITY
            var bottom = Float.NEGATIVE_INFINITY
            sample.indices.forEach { index ->
                val bounds = textLayout.getBoundingBox(index)
                top = min(top, bounds.top)
                bottom = max(bottom, bounds.bottom)
            }
            if (!top.isFinite() || !bottom.isFinite()) {
                0f
            } else {
                when (position) {
                    Position.TopLeft,
                    Position.TopCenter,
                    Position.TopRight -> -top
                    Position.CenterLeft,
                    Position.Center,
                    Position.CenterRight -> textLayout.size.height / 2f - (top + bottom) / 2f
                    Position.BottomLeft,
                    Position.BottomCenter,
                    Position.BottomRight -> textLayout.size.height - bottom
                }
            }
        }
        Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            style = pdfTextStyle.copy(fontSize = scaledFontSize),
            modifier = Modifier
                .align(alignment)
                .requiredWidth(targetWidth + 8.dp)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .graphicsLayer { translationY = textVerticalOffsetPx },
        )
    }
}
