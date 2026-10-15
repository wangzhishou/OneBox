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

package com.t8rin.imagetoolbox.core.ui.utils.helper

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import com.shifenmiao.model.event.PermissionRequest
import com.t8rin.documentscanner.DocumentScanner as DocumentScannerDelegate
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraAlt
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.utils.getString

private class DocumentScannerImpl(
    private val context: ComponentActivity,
    private val scanner: DocumentScannerDelegate,
    private val scannerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) : DocumentScanner {

    override fun scan() {
        ContextUtils.requestPermissionAndExecute(
            permissionRequest = PermissionRequest.CAMERA,
            context = context,
            permissions = arrayOf(android.Manifest.permission.CAMERA),
            onGranted = {
                scannerLauncher.launch(scanner.createDocumentScanIntent())
            },
            onDenied = {
                AppToastHost.showToast(
                    message = getString(R.string.grant_camera_permission_to_scan_document_scanner),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraAlt
                )
            }
        )
    }

}

@Composable
internal fun rememberDocumentScannerImpl(
    onSuccess: (ScanResult) -> Unit
): DocumentScanner {
    val context = LocalComponentActivity.current
    val colorScheme = MaterialTheme.colorScheme
    val cropperHandleColor = colorScheme.onPrimaryContainer
    val cropperFrameColor = colorScheme.onPrimaryContainer
    val cropperHandleOutlineColor = colorScheme.primaryContainer
    val buttonTintColor = colorScheme.onPrimaryContainer
    val buttonContainerColor = colorScheme.primaryContainer
    val doneButtonTintColor = colorScheme.primaryContainer
    val doneButtonContainerColor = colorScheme.onPrimaryContainer
    val cropperStrokeWidthDp = 1.2f

    val scanner = remember(
        context,
        cropperHandleColor,
        cropperFrameColor,
        cropperHandleOutlineColor,
        buttonTintColor,
        buttonContainerColor,
        cropperStrokeWidthDp,
        doneButtonTintColor,
        doneButtonContainerColor
    ) {
        DocumentScannerDelegate(
            activity = context,
            cropperHandleColor = cropperHandleColor.toArgb(),
            cropperHandleOutlineColor = cropperHandleOutlineColor.toArgb(),
            cropperFrameColor = cropperFrameColor.toArgb(),
            cropperStrokeWidthDp = cropperStrokeWidthDp,
            buttonTintColor = buttonTintColor.toArgb(),
            buttonContainerColor = buttonContainerColor.toArgb(),
            doneButtonTintColor = doneButtonTintColor.toArgb(),
            doneButtonContainerColor = doneButtonContainerColor.toArgb(),
            successHandler = { imageUris ->
                onSuccess(
                    ScanResult(imageUris.map { it.toUri() })
                )
            },
            errorHandler = {
                AppToastHost.showFailureToast(
                    throwable = Throwable(it)
                )
            }
        )
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scanner.handleDocumentScanIntentResult(result)
    }

    return remember(context, scanner, scannerLauncher) {
        DocumentScannerImpl(
            context = context,
            scanner = scanner,
            scannerLauncher = scannerLauncher
        )
    }
}
