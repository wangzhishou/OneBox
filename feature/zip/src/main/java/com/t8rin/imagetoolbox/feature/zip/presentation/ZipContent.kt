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

package com.t8rin.imagetoolbox.feature.zip.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.domain.utils.timestamp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.image.AutoFilePicker
import com.t8rin.imagetoolbox.feature.zip.R
import com.t8rin.imagetoolbox.feature.zip.domain.ZipSourceFile
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipBottomActionBar
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipEmptyState
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipProgressDialog
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipSourceFileList
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipComponent
import com.wanbaohe.core.ui.review.ReviewPromptTrigger

@Composable
fun ZipContent(component: ZipComponent) {
    val resources = LocalResources.current

    val filePicker = rememberFilePicker(onSuccess = component::addUris)

    AutoFilePicker(
        onAutoPick = filePicker::pickFile,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    val createArchiveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destinationUri ->
        if (destinationUri != null) {
            component.saveArchive(destinationUri) { result ->
                result.onSuccess { savedUri ->
                    AppToastHost.showFileSuccessToast(
                        uri = savedUri,
                        message = resources.getString(R.string.zip_saved_message),
                        trigger = ReviewPromptTrigger.ARCHIVE_SUCCESS
                    )
                }.onFailure {
                    AppToastHost.showFailureToast(it)
                }
            }
        }
    }

    BaseScreen(
        title = stringResource(R.string.zip_create_title),
        onGoBack = component.onGoBack,
        isShowDefaultActions = false,
        showNavigationBarsPadding = false,
        actions = {
            IconButton(onClick = filePicker::pickFile) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.zip_add_files)
                )
            }
            if (component.savedArchiveUri != null) {
                IconButton(onClick = { component.shareSavedArchive(AppToastHost::showConfetti) }) {
                    Icon(
                        imageVector = Icons.Outlined.LineShare,
                        contentDescription = stringResource(R.string.zip_share)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (component.sourceFiles.isEmpty()) {
                ZipEmptyState(onAddFiles = filePicker::pickFile)
            } else {
                ZipSourceFileList(
                    files = component.sourceFiles,
                    totalSize = component.totalSize,
                    onRemove = { component.removeUri(it.uri.toUri()) }
                )
            }
        }

        ZipBottomActionBar(
            text = stringResource(R.string.zip_save_archive),
            enabled = component.uris.isNotEmpty(),
            onClick = {
                createArchiveLauncher.launch(
                    suggestedArchiveName(component.sourceFiles)
                )
            }
        )
    }

    ZipProgressDialog(
        visible = component.isSaving,
        progress = component.progress,
        onCancelLoading = component::cancelSaving
    )
}

private fun suggestedArchiveName(files: List<ZipSourceFile>): String {
    val singleName = files.singleOrNull()?.name
    if (!singleName.isNullOrBlank()) {
        val baseName = singleName.substringBeforeLast('.', singleName).ifBlank { "archive" }
        return "$baseName.zip"
    }
    return "ZIP_${timestamp("yyyyMMdd_HHmmss")}.zip"
}
