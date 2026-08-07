package com.shifenmiao.common.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog

/**
 * 创建标准的图片选择器（多选）
 */
@Composable
fun rememberStandardImagePicker(
    onImagesPicked: (List<Uri>) -> Unit
) = rememberImagePicker(
    picker = Picker.Multiple,
    onSuccess = onImagesPicked
)

// ==================== 添加图片选择器状态管理 ====================

/**
 * 添加图片选择器状态
 * 用于管理添加图片时的选择器和一次性图片来源选择对话框
 */
class AddImagePickerState(
    val showDialog: Boolean,
    val onShowDialog: () -> Unit,
    val onDismissDialog: () -> Unit,
    val pickImage: () -> Unit
)

/**
 * 创建添加图片选择器状态
 * 封装了添加图片选择器和一次性图片来源选择对话框的逻辑
 *
 * @param picker 选择器类型（单选/多选）
 * @param onImagesPicked 图片选择回调
 * @return AddImagePickerState 包含对话框状态和选择器操作
 *
 * 使用示例：
 * ```kotlin
 * val addImageState = rememberAddImagePickerState(
 *     picker = Picker.Multiple,
 *     onImagesPicked = { uris -> component.addUris(uris) }
 * )
 *
 * // 点击添加按钮
 * onAddClick = { addImageState.pickImage() }
 *
 * // 长按添加按钮
 * onAddLongClick = { addImageState.onShowDialog() }
 *
 * // 在 Composable 末尾添加对话框
 * AddImagePickingDialog(state = addImageState, picker = Picker.Multiple)
 * ```
 */
@Composable
fun rememberAddImagePickerState(
    picker: Picker = Picker.Multiple,
    onImagesPicked: (List<Uri>) -> Unit
): AddImagePickerState {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val imagePicker = rememberImagePicker(picker) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImagesPicked(uris)
        }
    }

    return remember(imagePicker, showDialog) {
        AddImagePickerState(
            showDialog = showDialog,
            onShowDialog = { showDialog = true },
            onDismissDialog = { showDialog = false },
            pickImage = { imagePicker.pickImage() }
        )
    }
}

/**
 * 添加图片选择对话框
 * 与 AddImagePickerState 配合使用，在已有图片时长按添加按钮弹出图片来源选择
 *
 * @param state AddImagePickerState 状态
 * @param picker 选择器类型
 */
@Composable
fun AddImagePickingDialog(
    state: AddImagePickerState,
    picker: Picker = Picker.Multiple
) {
    val imagePicker = rememberImagePicker(picker) { _ -> }

    OneTimeImagePickingDialog(
        visible = state.showDialog,
        onDismiss = state.onDismissDialog,
        picker = picker,
        imagePicker = imagePicker
    )
}

/**
 * 添加图片选择对话框（完整版）
 * 独立使用，不需要 AddImagePickerState
 *
 * @param visible 是否显示
 * @param onDismiss 关闭回调
 * @param picker 选择器类型
 * @param onImagesPicked 图片选择回调
 */
@Composable
fun AddImagePickingDialogWithPicker(
    visible: Boolean,
    onDismiss: () -> Unit,
    picker: Picker = Picker.Multiple,
    onImagesPicked: (List<Uri>) -> Unit
) {
    val imagePicker = rememberImagePicker(picker) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImagesPicked(uris)
        }
    }

    OneTimeImagePickingDialog(
        visible = visible,
        onDismiss = onDismiss,
        picker = picker,
        imagePicker = imagePicker
    )
}