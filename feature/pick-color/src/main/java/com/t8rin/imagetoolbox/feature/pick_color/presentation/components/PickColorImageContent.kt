/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.smarttoolfactory.colordetector.ImageColorDetector
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.wanbaohe.core.ui.widget.buttons.PanModeButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton

/**
 * 取色页：BaseScreen 提供标准顶栏，选中的图片作为最底层背景铺满整屏，
 * 颜色卡片浮在图片底部。
 *
 * 交互：
 * - 单指拖动：取色（不弹额外放大镜，直接靠双指放大看细节）
 * - 双指捏合 / 拖动：缩放与平移
 * - 顶栏手形按钮：切到拖动模式，单指平移、双指（或双击后上下拖）缩放；此模式下不取色，
 *   再点一次回到取色模式。
 */
@Composable
internal fun PickColorImageContent(
    bitmap: Bitmap,
    paletteSource: ImageBitmap?,
    color: Color,
    isFavorite: Boolean,
    bottomInset: Dp,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onPickImageLongClick: () -> Unit,
    onColorChange: (Color) -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panEnabled by rememberSaveable { mutableStateOf(false) }

    BaseScreen(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomInset),
        title = stringResource(R.string.pick_color_title),
        onGoBack = onBack,
        isBackHandler = false,
        showNavigationBarsPadding = false,
        actions = {
            PanModeButton(
                selected = panEnabled,
                onClick = { panEnabled = !panEnabled },
                contentDescription = stringResource(R.string.pick_color_pan_mode)
            )
            EnhancedIconButton(
                onClick = onPickImage,
                onLongClick = onPickImageLongClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlt,
                    contentDescription = stringResource(R.string.pick_image_alt)
                )
            }
        },
        background = {
            ImageColorDetector(
                panEnabled = panEnabled,
                imageBitmap = remember(bitmap) { bitmap.asImageBitmap() },
                color = color,
                modifier = Modifier.fillMaxSize(),
                isMagnifierEnabled = false,
                onColorChange = onColorChange
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    // 浮层浮在图片上：吃掉这块范围内的按下事件，避免点卡片时取到卡片后面的颜色
                    .pointerInput(Unit) { detectTapGestures { } },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (paletteSource != null) {
                    PickColorImagePalette(
                        paletteSource = paletteSource,
                        onColorSelected = onColorChange
                    )
                }
                if (color.isSpecified) {
                    PickColorColorCard(
                        color = color,
                        isFavorite = isFavorite,
                        onCopy = onCopy,
                        onToggleFavorite = onToggleFavorite
                    )
                } else {
                    PickColorHintCard()
                }
            }
        }
    }
}
