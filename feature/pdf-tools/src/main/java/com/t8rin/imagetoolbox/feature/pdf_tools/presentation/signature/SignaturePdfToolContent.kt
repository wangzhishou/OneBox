package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolfactory.colordetector.util.ColorUtil.roundToTwoDigits
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDraw
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.ImageUtils.rememberPdfPages
import com.wanbaohe.core.ui.widget.controls.page.PageSelectionItem
import com.wanbaohe.core.ui.widget.controls.selection.AlphaSelector
import com.wanbaohe.core.ui.widget.controls.selection.ImageSelector
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSliderItem
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.PdfPagePreview
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature.screenLogic.SignaturePdfToolComponent

@Composable
fun SignaturePdfToolContent(
    component: SignaturePdfToolComponent,
) {
    val params = component.params
    val pageCount by rememberPdfPages(component.uri)
    val savedSignatures by component.savedSignatures.collectAsStateWithLifecycle()
    var showDrawDialog by rememberSaveable { mutableStateOf(false) }
    var signatureToDelete by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            component.ensurePages(listOf(pageCount - 1))
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
        title = stringResource(R.string.signature),
        canSave = component.hasSignature,
        imagePreview = {
            PdfPagePreview(
                uri = component.uri,
                pageCount = pageCount,
                activePages = params.pages,
            ) { page ->
                if (component.hasSignature && page in params.pages) {
                    BoxWithConstraints(Modifier.matchParentSize()) {
                        val bitmap = params.signatureImage as? android.graphics.Bitmap
                        var aspect by remember(bitmap) {
                            mutableFloatStateOf(
                                bitmap?.takeIf { it.height > 0 }?.let {
                                    it.width.toFloat() / it.height
                                } ?: 2f
                            )
                        }
                        val markWidth = maxWidth * params.size
                        val markHeight = markWidth / aspect.coerceAtLeast(0.2f)
                        val maxX = (maxWidth - markWidth).coerceAtLeast(0.dp)
                        val maxY = (maxHeight - markHeight).coerceAtLeast(0.dp)
                        // Drag deltas arrive in pixels of the page preview, while x/y are
                        // fractions of the page, so normalize by the page size (not by the
                        // signature size, which would make the mark move faster than the finger).
                        val pageWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                        val pageHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
                        Picture(
                            model = params.signatureImage,
                            contentScale = ContentScale.Fit,
                            showTransparencyChecker = false,
                            onSuccess = { state ->
                                val image = state.result.image
                                if (image.height > 0) {
                                    aspect = image.width.toFloat() / image.height.toFloat()
                                }
                            },
                            modifier = Modifier
                                .width(markWidth)
                                .height(markHeight)
                                .offset(
                                    x = (maxWidth * params.x - markWidth / 2).coerceIn(0.dp, maxX),
                                    y = (maxHeight * params.y - markHeight / 2).coerceIn(0.dp, maxY),
                                )
                                .graphicsLayer { alpha = params.opacity }
                                .pointerInput(page) {
                                    detectDragGestures { change, drag ->
                                        change.consume()
                                        val current = component.params
                                        component.updateParams(
                                            current.copy(
                                                x = (current.x + drag.x / pageWidthPx).coerceIn(0f, 1f),
                                                y = (current.y + drag.y / pageHeightPx).coerceIn(0f, 1f),
                                            )
                                        )
                                    }
                                },
                        )
                    }
                }
            }
        },
        placeImagePreview = true,
        controls = {
            PreferenceItem(
                title = stringResource(R.string.draw_signature),
                subtitle = stringResource(R.string.draw_signature_sub),
                startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDraw,
                onClick = { showDrawDialog = true },
                shape = ShapeDefaults.large,
            )
            Spacer(Modifier.height(8.dp))
            ImageSelector(
                value = params.signatureImage.takeIf { component.hasSignature },
                onValueChange = { component.updateSignature(it) },
                title = stringResource(R.string.signature_image),
                subtitle = stringResource(R.string.pick_image),
            )
            Spacer(Modifier.height(8.dp))
            SavedSignatureRow(
                signatures = savedSignatures,
                onSelect = { component.updateSignature(it) },
                onDeleteRequest = { signatureToDelete = it },
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
            Text(
                text = stringResource(R.string.signature_drag_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
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
                value = params.size,
                title = stringResource(R.string.just_size),
                valueRange = 0.05f..1f,
                internalStateTransformation = { it.roundToTwoDigits() },
                onValueChange = { component.updateParams(params.copy(size = it)) },
                shape = ShapeDefaults.large,
            )
        },
        onFilledPassword = { component.setUri(component.uri) },
    )

    SignatureDrawDialog(
        visible = showDrawDialog,
        onDismiss = { showDrawDialog = false },
        onConfirm = { component.updateSignature(it, save = true) },
    )

    EnhancedAlertDialog(
        visible = signatureToDelete != null,
        onDismissRequest = { signatureToDelete = null },
        title = { Text(stringResource(R.string.delete_signature)) },
        confirmButton = {
            EnhancedButton(
                onClick = {
                    signatureToDelete?.let(component::deleteSignature)
                    signatureToDelete = null
                },
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = { signatureToDelete = null },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
