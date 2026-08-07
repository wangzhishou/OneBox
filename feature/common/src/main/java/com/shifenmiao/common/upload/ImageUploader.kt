package com.shifenmiao.common.upload

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.StrapiImage
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.resources.icons.Close


@Composable
fun rememberImageUploader(
    localActivity: ComponentActivity,
    ref: String? = null,
    refId: String? = null,
    field: String? = null,
    maxFiles: Int = 9,
    commonComponent: CommonComponent? = null,
    onPickerSuccess: ((List<Uri>) -> Unit)? = null,
    onUploadFailure: ((index: Int, String) -> Unit)? = null,
    onImageUploaded: ((index: Int, List<StrapiImage>) -> Unit)? = null,
    onProgressUpdate: ((index: Int, progress: Float) -> Unit)? = null
): FilePicker {
    return rememberFilePicker(
        type = FileType.Multiple,
        mimeType = MimeType.UploadImage,
        onFailure = remember(localActivity) {
            {
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    message = localActivity.getString(R.string.upload_cancel),
                )
            }
        }
    ) { files ->
        if (files.size > maxFiles) {
            AppToastHost.showToast(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                message = localActivity.getString(R.string.upload_max_files, maxFiles.toString()),
            )
            return@rememberFilePicker
        }
        onPickerSuccess?.let {
            it(files)
            return@let
        }
        commonComponent?.uploadImages(
            imageUris = files,
            ref = ref,
            refId = refId,
            field = field,
            onUploadFailure = { index, msg ->
                onUploadFailure?.let {
                    it(index, msg)
                    return@let
                }
            },
            onUploadSuccess = { index, images ->
                onImageUploaded?.let {
                    it(index, images)
                    return@let
                }
            },
            onProgressUpdate = onProgressUpdate
        )
    }
}