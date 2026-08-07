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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.OneBoxImageScreenBottomBar
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.ui.theme.takeColorFromScheme
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.widget.buttons.PanModeButton
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.AutoFilePicker
import com.t8rin.imagetoolbox.core.ui.widget.utils.AutoContentBasedColors
import com.t8rin.imagetoolbox.feature.pick_color.presentation.components.PickColorFromImageContentImpl
import com.t8rin.imagetoolbox.feature.pick_color.presentation.components.PickColorFromImageTopAppBar
import com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn

@Composable
fun PickColorFromImageContent(
    component: PickColorFromImageComponent
) {
    val settingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()

    var panEnabled by rememberSaveable { mutableStateOf(false) }

    AutoContentBasedColors(component.bitmap)
    AutoContentBasedColors(component.color)

    val imagePicker = rememberImagePicker { uri: Uri ->
        component.setUri(
            uri = uri,
            onFailure = AppToastHost::showFailureToast
        )
    }

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = component.initialUri != null
    )

    val isPortrait by isPortraitOrientationAsState()

    val switch = @Composable {
        PanModeButton(
            selected = panEnabled,
            onClick = { panEnabled = !panEnabled }
        )
    }

    val magnifierButton = @Composable {
        val settingsInteractor = LocalSimpleSettingsInteractor.current
        EnhancedIconButton(
            containerColor = takeColorFromScheme {
                if (settingsState.magnifierEnabled) {
                    secondary
                } else surfaceContainer
            },
            contentColor = takeColorFromScheme {
                if (settingsState.magnifierEnabled) {
                    onSecondary
                } else onSurface
            },
            enableAutoShadowAndBorder = false,
            onClick = {
                scope.launch {
                    settingsInteractor.toggleMagnifierEnabled()
                }
            },
            modifier = Modifier.statusBarsPadding()
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZoomIn,
                contentDescription = stringResource(R.string.magnifier)
            )
        }
    }

    var showOneTimeImagePickingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            PickColorFromImageTopAppBar(
                bitmap = component.bitmap,
                onGoBack = component.onGoBack,
                isPortrait = isPortrait,
                magnifierButton = magnifierButton,
                color = component.color
            )
            PickColorFromImageContentImpl(
                bitmap = component.bitmap,
                isPortrait = isPortrait,
                panEnabled = panEnabled,
                onColorChange = component::updateColor,
                onPickImage = pickImage,
                onOneTimePickImage = { showOneTimeImagePickingDialog = true },
                magnifierButton = magnifierButton,
                switch = switch,
                color = component.color
            )
            // OneBox 风格底部操作栏
            OneBoxImageScreenBottomBar(
                isNoData = component.bitmap == null,
                onPickImage = pickImage,
                onSave = pickImage,
                onPickImageLongClick = { showOneTimeImagePickingDialog = true },
                extraActions = {
                    if (component.bitmap != null && isPortrait) {
                        switch()
                    }
                }
            )
        }

        // OneBox 风格：空状态使用底部栏统一处理，不再需要独立 FAB
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