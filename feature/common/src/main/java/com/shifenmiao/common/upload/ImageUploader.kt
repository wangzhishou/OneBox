/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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
import com.wanbaohe.core.ui.utils.content_pickers.FilePicker
import com.wanbaohe.core.ui.utils.content_pickers.FileType
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
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