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

package com.t8rin.imagetoolbox.feature.limits_resize.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.common.ui.ImagePickerList
import com.shifenmiao.common.ui.ImageWorkspace
import com.shifenmiao.common.ui.ImageWorkspaceTab
import com.shifenmiao.common.ui.ZoomableImagePreview
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.common.ui.rememberImageWorkspaceState
import com.shifenmiao.common.ui.rememberSafeImageBitmap
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.controls.ResizeImageField
import com.t8rin.imagetoolbox.core.ui.widget.controls.SaveExifWidget
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageFormatSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.QualitySelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ScaleModeSelector
import com.t8rin.imagetoolbox.core.ui.widget.image.AutoFilePicker
import com.t8rin.imagetoolbox.core.ui.widget.modifier.detectSwipes
import com.t8rin.imagetoolbox.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import com.t8rin.imagetoolbox.core.ui.widget.utils.AutoContentBasedColors
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.components.AutoRotateLimitBoxToggle
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.components.LimitsResizeSelector
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.screenLogic.LimitsResizeComponent

@Composable
fun LimitsResizeContent(
    component: LimitsResizeComponent
) {

    val showConfetti: () -> Unit = AppToastHost::showConfetti

    AutoContentBasedColors(component.bitmap)

    val autoImagePicker = rememberImagePicker { uris: List<Uri> ->
        component.updateUris(uris = uris, onFailure = AppToastHost::showFailureToast)
    }

    AutoFilePicker(
        onAutoPick = autoImagePicker::pickImage,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    val workspaceState = rememberImageWorkspaceState()
    val immersiveModeState = rememberImmersiveModeState()
    val safeImageBitmap = rememberSafeImageBitmap(component.previewBitmap)

    val currentImageIndex = remember(component.uris, component.selectedUri) {
        component.uris?.indexOf(component.selectedUri)?.takeIf { it >= 0 } ?: 0
    }

    val imageListState = rememberLazyListState()

    LaunchedEffect(component.uris?.size) {
        if (!component.uris.isNullOrEmpty() && workspaceState.currentTabIndex == 0) {
            workspaceState.navigateToTab(1)
        }
        if (component.uris.isNullOrEmpty()) {
            workspaceState.scrollToTab(0)
        }
    }

    var editSheetData by remember { mutableStateOf(listOf<Uri>()) }
    ProcessImagesPreferenceSheet(
        uris = editSheetData,
        visible = editSheetData.isNotEmpty(),
        onDismiss = { editSheetData = emptyList() },
        onNavigate = component.onNavigate
    )

    val panelTabHeight = 220.dp

    ImageWorkspace(
        title = stringResource(R.string.limits_resize),
        onGoBack = component.onGoBack,
        hasSelectedImages = component.bitmap != null,
        haveChanges = component.haveChanges,
        isSaving = component.isSaving,
        isImageLoading = component.isImageLoading,
        done = component.done,
        left = component.uris?.size ?: 0,
        imageFormat = component.imageFormat,
        onPickImages = { uris ->
            component.updateUris(uris = uris, onFailure = AppToastHost::showFailureToast)
        },
        onSave = { uri ->
            component.saveBitmaps(
                oneTimeSaveLocationUri = uri,
                onResult = component::parseSaveResults
            )
        },
        onCancelSaving = component::cancelSaving,
        state = workspaceState,
        immersiveModeState = immersiveModeState,
        picker = Picker.Multiple,
        topBarActions = {
            if (component.bitmap != null) {
                ShareButton(
                    enabled = component.canSave,
                    onShare = {
                        component.shareBitmaps(showConfetti)
                    },
                    onCopy = {
                        component.cacheCurrentImage(Clipboard::copy)
                    },
                    onEdit = {
                        component.cacheImages {
                            editSheetData = it
                        }
                    }
                )
            }
        },
        canvas = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .detectSwipes(
                        onSwipeRight = component::selectLeftUri,
                        onSwipeLeft = component::selectRightUri
                    )
            ) {
                ZoomableImagePreview(
                    imageBitmap = safeImageBitmap,
                    isLoading = component.isImageLoading,
                    isImmersive = immersiveModeState.isImmersive,
                    contentPadding = contentPadding,
                    onTap = immersiveModeState::toggle,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        operationTabs = listOf(
            ImageWorkspaceTab(
                title = if ((component.uris?.size ?: 0) > 0)
                    stringResource(CommonR.string.common_selected_images_count, component.uris!!.size)
                else
                    stringResource(CommonR.string.common_selected_images)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ImagePickerList(
                        selectedUris = component.uris ?: emptyList(),
                        currentIndex = currentImageIndex,
                        onImageSelected = { index ->
                            component.uris?.getOrNull(index)?.let { uri ->
                                component.updateSelectedUri(
                                    uri = uri,
                                    onFailure = AppToastHost::showFailureToast
                                )
                            }
                        },
                        onAddClick = { autoImagePicker.pickImage() },
                        onAddLongClick = { autoImagePicker.pickImage() },
                        onRemoveClick = { uri ->
                            component.updateUrisSilently(removedUri = uri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        listState = imageListState,
                    )
                }
            },

            ImageWorkspaceTab(title = stringResource(R.string.limits_resize)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ResizeImageField(
                        imageInfo = component.imageInfo,
                        originalSize = component.originalSize,
                        onWidthChange = component::updateWidth,
                        onHeightChange = component::updateHeight
                    )
                    AutoRotateLimitBoxToggle(
                        value = component.resizeType.autoRotateLimitBox,
                        onClick = component::toggleAutoRotateLimitBox
                    )
                    LimitsResizeSelector(
                        enabled = component.bitmap != null,
                        value = component.resizeType,
                        onValueChange = component::setResizeType
                    )
                }
            },

            ImageWorkspaceTab(title = stringResource(R.string.image_format)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ImageFormatSelector(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.imageFormat,
                        onValueChange = component::setImageFormat,
                        title = {}
                    )
                    QualitySelector(
                        modifier = Modifier.fillMaxWidth(),
                        imageFormat = component.imageFormat,
                        quality = component.imageInfo.quality,
                        onQualityChange = component::setQuality
                    )
                }
            },

            ImageWorkspaceTab(title = stringResource(R.string.scale_mode)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ScaleModeSelector(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.imageInfo.imageScaleMode,
                        onValueChange = component::setImageScaleMode,
                        title = {}
                    )
                }
            },

            ImageWorkspaceTab(title = stringResource(R.string.settings)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    SaveExifWidget(
                        modifier = Modifier.fillMaxWidth(),
                        imageFormat = component.imageFormat,
                        checked = component.keepExif,
                        onCheckedChange = component::setKeepExif
                    )
                }
            }
        ),
        panelTabHeight = panelTabHeight,
    )
}
