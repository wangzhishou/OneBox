package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.t8rin.imagetoolbox.core.ui.widget.buttons.PanModeButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton

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
            if (color.isSpecified) {
                PickColorColorCard(
                    color = color,
                    isFavorite = isFavorite,
                    onCopy = onCopy,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        // 卡片浮在图片上：吃掉卡片范围内的按下事件，避免点卡片背景时取到卡片后面的颜色
                        .pointerInput(Unit) { detectTapGestures { } }
                )
            } else {
                PickColorHintCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .pointerInput(Unit) { detectTapGestures { } }
                )
            }
        }
    }
}
