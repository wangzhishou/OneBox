package com.shifenmiao.common.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxBottomActionBar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * OneBox 风格的图像处理页面底部操作栏
 *
 * 将 BottomButtonsBlock 的复杂行为桥接到 OneBoxBottomActionBar 的统一视觉语言。
 * 支持空数据状态（选择图片）、保存操作、额外操作等标准图像处理流程。
 *
 * @param isNoData 是否没有选择图片（空状态）
 * @param onPickImage 选择图片回调
 * @param onSave 保存回调
 * @param modifier Modifier
 * @param onPickImageLongClick 长按选择图片回调（可选，弹出更多选项）
 * @param onSaveLongClick 长按保存回调（可选，选择保存位置）
 * @param isSaveVisible 保存按钮是否可见
 * @param isSaveEnabled 保存按钮是否可用
 * @param pickImageText 选择图片按钮文字
 * @param saveText 保存按钮文字
 * @param pickImageIcon 选择图片按钮图标
 * @param saveIcon 保存按钮图标
 * @param extraActions 额外操作（放在左侧）
 */
@Composable
fun OneBoxImageScreenBottomBar(
    isNoData: Boolean,
    onPickImage: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onPickImageLongClick: (() -> Unit)? = null,
    onSaveLongClick: (() -> Unit)? = null,
    isSaveVisible: Boolean = true,
    isSaveEnabled: Boolean = true,
    pickImageText: String = stringResource(R.string.pick_image_alt),
    saveText: String = stringResource(R.string.save),
    pickImageIcon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.AddPhotoAlt,
    saveIcon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
    extraActions: (@Composable RowScope.() -> Unit)? = null,
) {
    // 空状态：只显示选择图片按钮
    if (isNoData) {
        OneBoxBottomActionBar(
            modifier = modifier,
            primaryText = pickImageText,
            onPrimaryClick = onPickImage,
            primaryEnabled = true,
            secondaryText = null,
            onSecondaryClick = null,
            extraActions = extraActions,
        )
    } else {
        // 有数据状态：显示选择图片（次要）+ 保存（主要）
        OneBoxBottomActionBar(
            modifier = modifier,
            primaryText = saveText,
            onPrimaryClick = onSave,
            primaryEnabled = isSaveEnabled,
            secondaryText = if (isSaveVisible) pickImageText else null,
            onSecondaryClick = if (isSaveVisible) onPickImage else null,
            extraActions = extraActions,
        )
    }
}

/**
 * 简化的 OneBox 底部保存栏（用于已有图片处理页面的直接替换）
 *
 * 保持与 BottomSaveCancelBar 类似的 API，但使用 OneBox 视觉语言。
 */
@Composable
fun OneBoxSaveCancelBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String? = null,
    cancelText: String = stringResource(R.string.cancel),
    saveEnabled: Boolean = true,
    cancelEnabled: Boolean = true,
    extraActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val resolvedSaveText = saveText ?: stringResource(R.string.save)

    OneBoxBottomActionBar(
        modifier = modifier,
        primaryText = resolvedSaveText,
        onPrimaryClick = onSave,
        primaryEnabled = saveEnabled,
        secondaryText = cancelText,
        onSecondaryClick = onCancel,
        secondaryEnabled = cancelEnabled,
        extraActions = extraActions,
    )
}
