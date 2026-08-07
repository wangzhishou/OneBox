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

package com.t8rin.imagetoolbox.feature.filters.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.t8rin.imagetoolbox.core.ui.theme.mixedContainer
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.ImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.shifenmiao.common.ui.OneBoxImageScreenBottomBar
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedFloatingActionButton
import com.t8rin.imagetoolbox.feature.filters.presentation.screenLogic.FiltersComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTexture

@Composable
internal fun FiltersContentActionButtons(
    component: FiltersComponent,
    actions: @Composable RowScope.() -> Unit,
    imagePicker: ImagePicker,
    pickSingleImagePicker: ImagePicker,
    selectionFilterPicker: ImagePicker,
) {
    val isPortrait by isPortraitOrientationAsState()


    val filterType = component.filterType

    val saveBitmaps: (oneTimeSaveLocationUri: String?) -> Unit = {
        when (filterType) {
            is Screen.Filter.Type.Basic -> {
                component.saveBitmaps(
                    oneTimeSaveLocationUri = it,
                    onResult = component::parseSaveResults
                )
            }

            is Screen.Filter.Type.Masking -> {
                component.saveMaskedBitmap(
                    oneTimeSaveLocationUri = it,
                    onComplete = component::parseSaveResult
                )
            }

            else -> Unit
        }
    }
    var showFolderSelectionDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showOneTimeImagePickingDialog by rememberSaveable {
        mutableStateOf(false)
    }
    OneBoxImageScreenBottomBar(
        isNoData = component.basicFilterState.uris.isNullOrEmpty() && component.maskingFilterState.uri == null,
        onPickImage = {
            when (filterType) {
                is Screen.Filter.Type.Basic -> imagePicker.pickImage()
                is Screen.Filter.Type.Masking -> pickSingleImagePicker.pickImage()
                null -> selectionFilterPicker.pickImage()
            }
        },
        onSave = {
            saveBitmaps(null)
        },
        onSaveLongClick = {
            showFolderSelectionDialog = true
        },
        isSaveVisible = component.canSave,
        extraActions = {
            if (isPortrait) actions()
            // 添加滤镜按钮作为额外操作
            if (component.canSave && filterType != null) {
                EnhancedFloatingActionButton(
                    onClick = component::showAddFiltersSheet,
                    containerColor = MaterialTheme.colorScheme.mixedContainer
                ) {
                    when (filterType) {
                        is Screen.Filter.Type.Basic -> {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                                contentDescription = null
                            )
                        }
                        is Screen.Filter.Type.Masking -> {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTexture,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    )
    OneTimeSaveLocationSelectionDialog(
        visible = showFolderSelectionDialog,
        onDismiss = { showFolderSelectionDialog = false },
        onSaveRequest = saveBitmaps,
        formatForFilenameSelection = component.getFormatForFilenameSelection()
    )
    OneTimeImagePickingDialog(
        onDismiss = { showOneTimeImagePickingDialog = false },
        picker = if (filterType !is Screen.Filter.Type.Masking) {
            Picker.Multiple
        } else {
            Picker.Single
        },
        imagePicker = selectionFilterPicker,
        visible = showOneTimeImagePickingDialog
    )
}