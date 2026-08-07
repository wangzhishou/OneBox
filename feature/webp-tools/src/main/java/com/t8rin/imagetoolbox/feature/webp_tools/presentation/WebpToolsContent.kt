/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.webp_tools.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormatGroup
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Webp
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.isWebp
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.AdaptiveLayoutScreen
import com.t8rin.imagetoolbox.core.ui.widget.buttons.BottomButtonsBlock
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.controls.ImageReorderCarousel
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageFormatSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.QualitySelector
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.ImagesPreviewWithSelection
import com.t8rin.imagetoolbox.core.ui.widget.modifier.withModifier
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.core.ui.widget.preferences.TypeSelectionGrid
import com.t8rin.imagetoolbox.core.ui.widget.preferences.TypeSelectionItem
import com.t8rin.imagetoolbox.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import com.t8rin.imagetoolbox.core.ui.widget.text.TopAppBarTitle
import com.t8rin.imagetoolbox.feature.webp_tools.presentation.components.WebpParamsSelector
import com.t8rin.imagetoolbox.feature.webp_tools.presentation.screenLogic.WebpToolsComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.SelectAll

@Composable
fun WebpToolsContent(
    component: WebpToolsComponent
) {
    val context = LocalContext.current

    val showConfetti: () -> Unit = AppToastHost::showConfetti

    val imagePicker = rememberImagePicker(onSuccess = component::setImageUris)

    val pickSingleWebpLauncher = rememberFilePicker(
        mimeType = MimeType.Webp,
        onSuccess = { uri: Uri ->
            if (uri.isWebp(context)) {
                component.setWebpUri(uri)
            } else {
                AppToastHost.showToast(
                    message = getString(R.string.select_webp_image_to_start),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Webp
                )
            }
        }
    )

    val saveWebpLauncher = rememberFileCreator(
        mimeType = MimeType.Webp,
        onSuccess = { uri ->
            component.saveWebpTo(
                uri = uri,
                onResult = component::parseFileSaveResult
            )
        }
    )

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val isPortrait by isPortraitOrientationAsState()

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            TopAppBarTitle(
                title = when (val type = component.type) {
                    null -> stringResource(R.string.webp_tools)
                    else -> stringResource(type.title)
                },
                input = component.type,
                isLoading = component.isLoading,
                size = null
            )
        },
        onGoBack = onBack,
        topAppBarPersistentActions = {
            if (component.type == null) TopAppBarEmoji()
            val pagesSize by remember(component.imageFrames, component.convertedImageUris) {
                derivedStateOf {
                    component.imageFrames.getFramePositions(component.convertedImageUris.size).size
                }
            }
            val isWebpToImage = component.type is Screen.WebpTools.Type.WebpToImage
            AnimatedVisibility(
                visible = isWebpToImage && pagesSize != component.convertedImageUris.size,
                enter = fadeIn() + scaleIn() + expandHorizontally(),
                exit = fadeOut() + scaleOut() + shrinkHorizontally()
            ) {
                GlassTonalIconButton(
                    onClick = component::selectAllConvertedImages
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.SelectAll,
                        contentDescription = "Select All"
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .padding(8.dp),
                visible = isWebpToImage && pagesSize != 0
            ) {
                GlassSurface(
                    style = GlassStyle.Thin,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    borderWidth = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        pagesSize.takeIf { it != 0 }?.let {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = it.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        GlassTonalIconButton(
                            onClick = component::clearConvertedImagesSelection,
                            style = GlassStyle.Thin,
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                }
            }
        },
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = !component.isLoading && component.type != null,
                onShare = {
                    component.performSharing(showConfetti)
                },
                onEdit = {
                    component.cacheImages {
                        editSheetData = it
                    }
                }
            )
            ProcessImagesPreferenceSheet(
                uris = editSheetData,
                visible = editSheetData.isNotEmpty(),
                onDismiss = {
                    editSheetData = emptyList()
                },
                onNavigate = component.onNavigate
            )
        },
        imagePreview = {
            AnimatedContent(
                targetState = component.isLoading to component.type
            ) { (loading, type) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (loading) {
                        Modifier.padding(32.dp)
                    } else Modifier
                ) {
                    if (loading || type == null) {
                        EnhancedLoadingIndicator()
                    } else {
                        when (type) {
                            is Screen.WebpTools.Type.WebpToImage -> {
                                ImagesPreviewWithSelection(
                                    imageUris = component.convertedImageUris,
                                    imageFrames = component.imageFrames,
                                    onFrameSelectionChange = component::updateWebpFrames,
                                    isPortrait = isPortrait,
                                    isLoadingImages = component.isLoadingWebpImages
                                )
                            }

                            is Screen.WebpTools.Type.ImageToWebp -> Unit
                        }
                    }
                }
            }
        },
        placeImagePreview = component.type !is Screen.WebpTools.Type.ImageToWebp,
        showImagePreviewAsStickyHeader = false,
        autoClearFocus = false,
        controls = {
            when (val type = component.type) {
                is Screen.WebpTools.Type.WebpToImage -> {
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
                    ImageFormatSelector(
                        value = component.imageFormat,
                        onValueChange = component::setImageFormat,
                        entries = ImageFormatGroup.alphaContainedEntries
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
                    QualitySelector(
                        imageFormat = component.imageFormat,
                        quality = component.params.quality,
                        onQualityChange = component::setQuality
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
                }

                is Screen.WebpTools.Type.ImageToWebp -> {
                    val addImagesToPdfPicker =
                        rememberImagePicker(onSuccess = component::addImageToUris)

                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
                    ImageReorderCarousel(
                        images = type.imageUris,
                        onReorder = component::reorderImageUris,
                        onNeedToAddImage = addImagesToPdfPicker::pickImage,
                        onNeedToRemoveImageAt = component::removeImageAt,
                        onNavigate = component.onNavigate
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
                    WebpParamsSelector(
                        value = component.params,
                        onValueChange = component::updateParams
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
                }

                null -> Unit
            }
        },
        contentPadding = animateDpAsState(
            if (component.type == null) AppTheme.dimens.spaceSmall
            else AppTheme.dimens.spaceLarge
        ).value,
        buttons = { function ->
            val saveBitmaps: (oneTimeSaveLocationUri: String?) -> Unit = {
                component.saveBitmaps(
                    oneTimeSaveLocationUri = it,
                    onWebpSaveResult = saveWebpLauncher::make,
                    onResult = component::parseSaveResults
                )
            }
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                isNoData = component.type == null,
                onSecondaryButtonClick = {
                    when (component.type) {
                        is Screen.WebpTools.Type.WebpToImage -> pickSingleWebpLauncher.pickFile()
                        else -> imagePicker.pickImage()
                    }
                },
                isPrimaryButtonVisible = component.canSave,
                onPrimaryButtonClick = {
                    saveBitmaps(null)
                },
                onPrimaryButtonLongClick = {
                    showFolderSelectionDialog = true
                },
                actions = {
                    if (isPortrait) function()
                },
                showNullDataButtonAsContainer = true,
                onSecondaryButtonLongClick = if (component.type is Screen.WebpTools.Type.ImageToWebp || component.type == null) {
                    {
                        showOneTimeImagePickingDialog = true
                    }
                } else null
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = saveBitmaps
            )
            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Multiple,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        },
        insetsForNoData = WindowInsets(0),
        noDataControls = {
            val types = remember { Screen.WebpTools.Type.entries }
            TypeSelectionGrid(
                items = types.map { type ->
                    TypeSelectionItem(type.title, type.subtitle, type.icon)
                },
                onClick = { index ->
                    when (index) {
                        0 -> imagePicker.pickImage()
                        1 -> pickSingleWebpLauncher.pickFile()
                    }
                }
            )
        },
        canShowScreenData = component.type != null
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving
    )

    ExitWithoutSavingDialog(
        onExit = component::clearAll,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}