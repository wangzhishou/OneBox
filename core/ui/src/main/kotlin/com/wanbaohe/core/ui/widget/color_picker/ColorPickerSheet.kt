/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.wanbaohe.core.ui.widget.color_picker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smarttoolfactory.colordetector.ImageColorDetector
import com.t8rin.imagetoolbox.core.domain.model.toColorModel
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.text.AutoSizeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePickColor
import com.t8rin.imagetoolbox.core.resources.icons.Close

@Composable
fun ColorPickerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    color: Color?,
    onColorSelected: (Color) -> Unit,
    allowAlpha: Boolean,
    @Suppress("UNUSED_PARAMETER") placeAboveAll: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val simpleSettingsInteractor = LocalSimpleSettingsInteractor.current
    var tempColor by remember(visible) {
        mutableStateOf(color ?: Color.Black)
    }

    // 图片取色：系统相册选一张图 → 图内拖动取色 →「选用此颜色」回到面板。
    // 不跳独立的取色器页面，是为了不打断「正在配这个颜色」的上下文。
    var pickingFromImage by remember { mutableStateOf(false) }
    var imagePickLoading by remember { mutableStateOf(false) }
    var eyedropperBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 面板被外部关掉时，把取色弹窗/加载态一起收掉，避免下一屏突然弹出一个孤儿 Loading
    LaunchedEffect(visible) {
        if (!visible) {
            pickingFromImage = false
            eyedropperBitmap = null
            imagePickLoading = false
        }
    }

    val imagePicker = rememberImagePicker(
        onFailure = {},
        onSuccess = { uri: Uri ->
            scope.launch {
                imagePickLoading = true
                val bitmap = withContext(Dispatchers.IO) {
                    decodeDownsampledBitmap(context, uri)
                }
                imagePickLoading = false
                if (bitmap != null) {
                    eyedropperBitmap = bitmap
                    pickingFromImage = true
                } else {
                    AppToastHost.showFailureToast(R.string.image_load_no_permission)
                }
            }
        }
    )

    EnhancedModalBottomSheet(
        sheetContent = {
            Box {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState(), reverseScrolling = true)
                        .padding(24.dp)
                ) {
                    RecentColorsRow(
                        currentColor = tempColor,
                        onColorClick = { tempColor = it }
                    )

                    PaletteColorsCard(
                        currentColor = tempColor,
                        onColorClick = { tempColor = it }
                    )

                    ColorSelection(
                        value = tempColor,
                        onValueChange = { tempColor = it },
                        withAlpha = allowAlpha
                    )
                }
            }
        },
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        },
        title = stringResource(R.string.color),
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 取色器贴着 OK 放：OK 旁边就是「这个颜色从哪来」的另一个入口
                EnhancedIconButton(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    onClick = { imagePicker.pickImage() }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePickColor,
                        contentDescription = stringResource(R.string.pick_color)
                    )
                }
                EnhancedButton(
                    onClick = {
                        scope.launch {
                            simpleSettingsInteractor.toggleRecentColor(
                                tempColor.toArgb().toColorModel()
                            )
                        }
                        onColorSelected(tempColor)
                        onDismiss()
                    }
                ) {
                    AutoSizeText(stringResource(R.string.ok))
                }
            }
        }
    )

    val bitmap = eyedropperBitmap
    if (pickingFromImage && bitmap != null) {
        ImageEyedropperDialog(
            bitmap = bitmap,
            initialColor = tempColor,
            allowAlpha = allowAlpha,
            onDismiss = {
                pickingFromImage = false
                eyedropperBitmap = null
            },
            onColorSelected = { picked ->
                tempColor = picked
                pickingFromImage = false
                eyedropperBitmap = null
            }
        )
    }

    LoadingDialog(
        visible = imagePickLoading,
        canCancel = false
    )
}

/**
 * 图内取色弹窗：拖动时放大镜跟手，底部实时显示色值与「选用此颜色」。
 *
 * 这里不复用取色器页面（那是一个完整路由）：从选色面板跳走再回来，
 * 面板会关掉、临时色也丢了，用户要重新走一遍。
 */
@Composable
private fun ImageEyedropperDialog(
    bitmap: Bitmap,
    initialColor: Color,
    allowAlpha: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var pickedColor by remember(bitmap) { mutableStateOf(initialColor) }
    val magnifierEnabled = LocalSettingsState.current.magnifierEnabled

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.pick_color_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedIconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        // 图片取景框用中性底色，不要棋盘格：棋盘格会让上下留白
                        // 看起来像「图片没加载出来」；真正的透明色预览在下面色值行里。
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    ImageColorDetector(
                        modifier = Modifier.fillMaxSize(),
                        imageBitmap = remember(bitmap) { bitmap.asImageBitmap() },
                        color = pickedColor,
                        panEnabled = false,
                        isMagnifierEnabled = magnifierEnabled,
                        onColorChange = { pickedColor = it }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.large)
                            .then(
                                if (pickedColor.alpha < 1f) Modifier.transparencyChecker()
                                else Modifier
                            )
                            .background(pickedColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = pickedColor.toHex(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1f))
                    OneSecondaryButton(
                        text = stringResource(R.string.cancel),
                        onClick = onDismiss
                    )
                    OnePrimaryButton(
                        text = stringResource(R.string.use_this_color),
                        onClick = {
                            onColorSelected(
                                if (allowAlpha) pickedColor else pickedColor.copy(alpha = 1f)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 相册图可能非常大：先量尺寸，再按 2 的幂降采样到 [maxSide] 以内。
 *
 * 取色只需要屏幕级精度，2048px 已经足够看清像素；再大只是白白吃内存。
 */
private fun decodeDownsampledBitmap(
    context: Context,
    uri: Uri,
    maxSide: Int = 2048
): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    runCatching {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > maxSide) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }.getOrNull()
}
