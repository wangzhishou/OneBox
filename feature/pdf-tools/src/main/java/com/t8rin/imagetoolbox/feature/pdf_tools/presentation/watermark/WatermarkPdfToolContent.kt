package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.watermark

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.rememberPdfTextStyle
import kotlin.math.abs
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.ImageUtils.rememberPdfPages
import com.wanbaohe.core.ui.widget.controls.page.PageSelectionItem
import com.wanbaohe.core.ui.widget.controls.selection.AlphaSelector
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.PdfPagePreview
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.watermark.screenLogic.WatermarkPdfToolComponent
import kotlin.math.roundToInt

@Composable
fun WatermarkPdfToolContent(
    component: WatermarkPdfToolComponent,
) {
    val params = component.params
    val pageCount by rememberPdfPages(component.uri)

    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            component.ensurePages(List(pageCount) { it })
        }
    }

    BasePdfToolContent(
        component = component,
        contentPicker = rememberFilePicker(
            mimeType = MimeType.Pdf,
            onSuccess = component::setUri,
        ),
        isPickedAlready = component.initialUri != null,
        canShowScreenData = component.uri != null,
        title = stringResource(R.string.watermark_pdf),
        canSave = params.text.isNotBlank(),
        imagePreview = {
            PdfPagePreview(
                uri = component.uri,
                pageCount = pageCount,
                activePages = params.pages,
            ) { page ->
                if (page in params.pages && params.text.isNotBlank()) {
                    WatermarkOverlay(
                        text = params.text,
                        color = Color(params.color),
                        fontSize = params.fontSize,
                        rotation = params.rotation,
                        opacity = params.opacity,
                    )
                }
            }
        },
        placeImagePreview = true,
        controls = {
            RoundedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = params.text,
                onValueChange = { component.updateParams(params.copy(text = it)) },
                label = stringResource(R.string.text),
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
            Spacer(Modifier.height(8.dp))
            PageSelectionItem(
                value = params.pages,
                onValueChange = { selected ->
                    val pages = selected.ifEmpty { List(pageCount) { it } }
                    if (pages.isNotEmpty()) {
                        component.updateParams(params.copy(pages = pages))
                    }
                },
                pageCount = pageCount,
            )
            Spacer(Modifier.height(8.dp))
            EnhancedSliderItem(
                value = params.rotation,
                title = stringResource(R.string.angle),
                valueRange = 0f..360f,
                internalStateTransformation = Float::roundToInt,
                onValueChange = { component.updateParams(params.copy(rotation = snapAngle(it))) },
                shape = ShapeDefaults.large,
            )
            Spacer(Modifier.height(8.dp))
            AlphaSelector(
                value = params.opacity,
                onValueChange = { component.updateParams(params.copy(opacity = it)) },
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeDefaults.large,
            )
            Spacer(Modifier.height(8.dp))
            EnhancedSliderItem(
                value = params.fontSize,
                title = stringResource(R.string.watermark_size),
                valueRange = 8f..100f,
                internalStateTransformation = Float::roundToInt,
                onValueChange = { component.updateParams(params.copy(fontSize = it)) },
                shape = ShapeDefaults.large,
            )
            Spacer(Modifier.height(8.dp))
            ColorRowSelector(
                value = Color(params.color),
                onValueChange = { component.updateParams(params.copy(color = it.toArgb())) },
                title = stringResource(R.string.text_color),
                allowAlpha = false,
                modifier = Modifier.container(shape = ShapeDefaults.large),
            )
        },
        onFilledPassword = { component.setUri(component.uri) },
    )
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.WatermarkOverlay(
    text: String,
    color: Color,
    fontSize: Float,
    rotation: Float,
    opacity: Float,
) {
    val pdfTextStyle = rememberPdfTextStyle()
    val lines = text.replace("\r\n", "\n").split('\n').filter { it.isNotBlank() }
    if (lines.isEmpty()) return
    BoxWithConstraints(Modifier.matchParentSize()) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val targetWidth = maxWidth * fontSize / 100f
        val scaledFontSize = run {
            val widestLine = lines.maxOf { line ->
                textMeasurer.measure(
                    text = line,
                    style = pdfTextStyle.copy(fontSize = 100.sp),
                    maxLines = 1,
                    softWrap = false,
                ).size.width
            }.coerceAtLeast(1)
            with(density) { 100.sp * (targetWidth.toPx() / widestLine) }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .requiredWidth(targetWidth)
                .graphicsLayer {
                    rotationZ = rotation
                    alpha = opacity
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            lines.forEach { line ->
                Text(
                    text = line,
                    color = color,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    style = pdfTextStyle.copy(fontSize = scaledFontSize),
                )
            }
        }
    }
}

private fun snapAngle(angle: Float): Float {
    val stops = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f)
    val nearest = stops.minBy { abs(it - angle) }
    return if (abs(nearest - angle) <= 4f) nearest else angle
}
