package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
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
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.controls.ImageReorderCarousel
import com.t8rin.imagetoolbox.core.ui.widget.controls.ScaleSmallImagesToLargeToggle
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.PresetSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.QualitySelector
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitBackHandler
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedFloatingActionButton
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf.screenLogic.ImagesToPdfComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt

@Composable
fun ImagesToPdfScreen(component: ImagesToPdfComponent) {

    val showConfetti: () -> Unit = AppToastHost::showConfetti
    val isPortrait by isPortraitOrientationAsState()

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val savePdfLauncher = rememberFileCreator(
        mimeType = MimeType.Pdf,
        onSuccess = component::save,
    )

    val imagePicker = rememberImagePicker(onSuccess = component::setImages)
    val addImagesPicker = rememberImagePicker(onSuccess = component::addImages)

    BaseScreen(
        title = stringResource(R.string.images_to_pdf),
        onGoBack = onBack,
        actions = {
            if (component.canShare) {
                ShareButton(
                    onShare = {
                        component.share(
                            onSuccess = showConfetti,
                            onFailure = AppToastHost::showFailureToast,
                        )
                    },
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val images = component.imagesToPdfState
            if (images.isNullOrEmpty()) {
                EmptyPickArea(onPick = imagePicker::pickImage)
            } else {
                if (isPortrait) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .weight(1f)
                                .padding(16.dp),
                        ) {
                            ImagesToPdfControls(
                                component = component,
                                onAddImages = addImagesPicker::pickImage,
                                onClickPick = imagePicker::pickImage,
                                onClickSave = {
                                    savePdfLauncher.make(component.generatePdfFilename())
                                },
                            )
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        ) {
                            ImagesToPdfControls(
                                component = component,
                                onAddImages = addImagesPicker::pickImage,
                                onClickPick = imagePicker::pickImage,
                                onClickSave = {
                                    savePdfLauncher.make(component.generatePdfFilename())
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    LoadingDialog(
        visible = component.isSaving,
        onCancelLoading = component::cancelSaving,
    )

    ExitBackHandler(
        enabled = component.haveChanges,
        onBack = onBack,
    )

    ExitWithoutSavingDialog(
        onExit = component::clear,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
    )
}

@Composable
private fun ImagesToPdfControls(
    component: ImagesToPdfComponent,
    onAddImages: () -> Unit,
    onClickPick: () -> Unit,
    onClickSave: () -> Unit,
) {
    ImageReorderCarousel(
        images = component.imagesToPdfState,
        onReorder = component::reorder,
        onNeedToAddImage = onAddImages,
        onNeedToRemoveImageAt = component::removeAt,
        onNavigate = component.onNavigate,
    )
    Spacer(Modifier.height(8.dp))
    PresetSelector(
        value = component.presetSelected,
        includeTelegramOption = false,
        onValueChange = {
            if (it is Preset.Percentage) component.selectPreset(it)
        },
        showWarning = false,
    )
    Spacer(Modifier.height(8.dp))
    QualitySelector(
        imageFormat = ImageFormat.Jpg,
        quality = Quality.Base(component.quality),
        onQualityChange = {
            component.setQuality(it.qualityValue)
        }
    )
    Spacer(Modifier.height(8.dp))
    ScaleSmallImagesToLargeToggle(
        checked = component.scaleSmallImagesToLarge,
        onCheckedChange = { component.toggleScaleSmallImagesToLarge() },
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        EnhancedFloatingActionButton(onClick = onClickPick) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.AddPhotoAlt,
                contentDescription = stringResource(R.string.pick),
            )
        }
        if (!component.imagesToPdfState.isNullOrEmpty()) {
            Spacer(Modifier.width(8.dp))
            EnhancedFloatingActionButton(onClick = onClickSave) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.save),
                )
            }
        }
    }
}

@Composable
private fun EmptyPickArea(onPick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.images_to_pdf_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        EnhancedFloatingActionButton(
            onClick = onPick,
            modifier = Modifier.padding(16.dp),
            content = {
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.AddPhotoAlt,
                    contentDescription = stringResource(R.string.pick_image),
                )
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.pick_image))
                Spacer(Modifier.width(16.dp))
            },
        )
    }
}
