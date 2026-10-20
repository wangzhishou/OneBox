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

package com.t8rin.imagetoolbox.feature.pick_color.presentation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.stringResource
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePickColor
import com.t8rin.imagetoolbox.core.settings.presentation.model.PicturePickerMode
import com.wanbaohe.core.ui.utils.content_pickers.Picker
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.wanbaohe.core.ui.widget.image.AutoFilePicker
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.utils.AutoContentBasedColors
import com.t8rin.imagetoolbox.feature.pick_color.presentation.components.PickColorEmptyContent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.components.PickColorHistoryContent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.components.PickColorImageContent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.model.PickColorTab
import com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import kotlinx.coroutines.delay

@Composable
fun PickColorFromImageContent(
    component: PickColorFromImageComponent
) {
    AutoContentBasedColors(component.bitmap)
    AutoContentBasedColors(component.color)

    val imagePicker = rememberImagePicker { uri: Uri ->
        component.setUri(
            uri = uri,
            onFailure = AppToastHost::showFailureToast
        )
    }

    val pickImage = imagePicker::pickImage
    val takePhoto = {
        imagePicker.pickImageWithMode(
            picker = Picker.Single,
            picturePickerMode = PicturePickerMode.CameraCapture
        )
    }

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = component.initialUri != null
    )

    var showOneTimeImagePickingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val bottomInset = AppTheme.dimens.navigationHeight +
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    BackHandler {
        if (component.tab == PickColorTab.HISTORY) {
            component.selectTab(PickColorTab.PICK)
        } else {
            component.onGoBack()
        }
    }

    // 拖动过程中颜色高频变化，停稳后再落一条历史，避免把每一帧都写进存储
    LaunchedEffect(component.bitmap, component.color) {
        val color = component.color
        if (component.bitmap != null && color.isSpecified) {
            delay(800)
            component.recordColor(color)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (component.tab) {
            PickColorTab.PICK -> {
                val bitmap = component.bitmap
                if (bitmap == null) {
                    PickColorEmptyContent(
                        bottomInset = bottomInset,
                        onBack = component.onGoBack,
                        onPickImage = pickImage,
                        onTakePhoto = takePhoto
                    )
                } else {
                    PickColorImageContent(
                        bitmap = bitmap,
                        color = component.color,
                        isFavorite = component.isFavorite(component.color),
                        bottomInset = bottomInset,
                        onBack = component.onGoBack,
                        onPickImage = pickImage,
                        onPickImageLongClick = {
                            showOneTimeImagePickingDialog = true
                        },
                        onColorChange = component::updateColor,
                        onCopy = {
                            val color = component.color
                            if (color.isSpecified) {
                                component.recordColor(color)
                                Clipboard.copy(
                                    text = color.toHex(),
                                    message = R.string.color_copied
                                )
                            }
                        },
                        onToggleFavorite = {
                            component.toggleFavorite(component.color)
                        }
                    )
                }
            }

            PickColorTab.HISTORY -> PickColorHistoryContent(
                history = component.history,
                currentArgb = component.colorArgb,
                bottomInset = bottomInset,
                onBack = component.onGoBack,
                onSelect = component::applyHistoryColor,
                onToggleFavorite = { argb ->
                    component.toggleFavorite(Color(argb))
                },
                onRemove = component::removeHistoryEntry,
                onClear = component::clearHistory
            )
        }

        // BottomNavigationBar 内部把内容套在 AnimatedVisibility 里，传进去的 modifier
        // 只作用到内层 Box —— align 必须落在外层这层包裹 Box 上，否则栏会跑到屏幕顶部。
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            PickColorBottomNavigation(
                selectedTab = component.tab,
                onSelect = component::selectTab
            )
        }
    }

    OneTimeImagePickingDialog(
        onDismiss = { showOneTimeImagePickingDialog = false },
        picker = Picker.Single,
        imagePicker = imagePicker,
        visible = showOneTimeImagePickingDialog
    )

    LoadingDialog(
        visible = component.isImageLoading,
        canCancel = false
    )
}

@Composable
private fun PickColorBottomNavigation(
    selectedTab: PickColorTab,
    onSelect: (PickColorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            id = PickColorTab.PICK.name,
            label = stringResource(R.string.pick_color_tab_pick),
            icon = Icons.Outlined.LinePickColor
        ),
        BottomNavItem(
            id = PickColorTab.HISTORY.name,
            label = stringResource(R.string.pick_color_tab_history),
            icon = Icons.Outlined.LineHistory
        )
    )

    BottomNavigationBar(
        items = items,
        selectedItemId = selectedTab.name,
        onItemClick = { item ->
            PickColorTab.entries.firstOrNull { it.name == item.id }?.let(onSelect)
        },
        modifier = modifier
    )
}
