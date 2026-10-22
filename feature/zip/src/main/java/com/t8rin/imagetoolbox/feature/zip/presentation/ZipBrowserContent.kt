/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.core.ui.utils.content.FileAccessGuidance
import com.wanbaohe.core.ui.utils.content.rememberFileAccessController
import com.wanbaohe.core.ui.utils.content_pickers.rememberFolderPicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.ContextUtils.takePersistablePermission
import com.t8rin.imagetoolbox.feature.zip.R
import com.t8rin.imagetoolbox.feature.zip.domain.ArchiveUnavailableException
import com.t8rin.imagetoolbox.feature.zip.domain.EncryptedArchiveException
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchiveEntry
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipArchiveErrorState
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipArchiveHeaderCard
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipBottomActionBar
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipEmptyArchiveState
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipEntryDetailsDialog
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipFileRow
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipFolderPathRow
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipFolderRow
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipLoadingState
import com.t8rin.imagetoolbox.feature.zip.presentation.components.ZipProgressDialog
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipBrowserComponent
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipBrowserItem
import com.wanbaohe.core.ui.review.ReviewPromptTrigger

@Composable
fun ZipBrowserContent(component: ZipBrowserComponent) {
    val resources = LocalResources.current
    val listState = rememberLazyListState()
    val navigationBarInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    var detailsEntry by remember { mutableStateOf<ZipArchiveEntry?>(null) }
    var pendingExtractEntry by remember { mutableStateOf<ZipArchiveEntry?>(null) }

    val onBack: () -> Unit = {
        if (!component.navigateUp()) component.onGoBack()
    }

    val folderPicker = rememberFolderPicker(
        onFailure = {
            // 用户没选成（尤其是被 "Can't use this folder" 挡住时）给一句能照做的提示，
            // 否则返回页面就只剩"什么都没发生"
            AppToastHost.showToast(resources.getString(R.string.zip_extract_destination_unavailable))
        },
        onSuccess = { destinationUri ->
            // 记住用户选的目标目录，避免每次解压都再弹一次"允许访问"
            destinationUri.takePersistablePermission()
            component.rememberExtractDestination(destinationUri)
            component.extractAll(destinationUri) { result ->
                result.onSuccess { extractedFolderUri ->
                    val count = component.fileCount
                    val message = resources.getQuantityString(
                        R.plurals.zip_extract_success,
                        count,
                        count
                    )
                    AppToastHost.showFileSuccessToast(
                        uri = extractedFolderUri,
                        message = message,
                        trigger = ReviewPromptTrigger.ARCHIVE_SUCCESS
                    )
                }.onFailure {
                    showZipFailure(resources, it)
                }
            }
        }
    )

    val extractEntryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { destinationUri ->
        val entry = pendingExtractEntry
        pendingExtractEntry = null
        if (destinationUri != null && entry != null) {
            component.extractEntry(entry, destinationUri) { result ->
                result.onSuccess { extractedUri ->
                    AppToastHost.showFileSuccessToast(
                        uri = extractedUri,
                        message = resources.getString(R.string.zip_file_saved),
                        trigger = ReviewPromptTrigger.ARCHIVE_SUCCESS
                    )
                }.onFailure {
                    showZipFailure(resources, it)
                }
            }
        }
    }

    LaunchedEffect(component.currentFolder) {
        listState.scrollToItem(0)
    }

    BaseScreen(
        title = component.archiveName.ifBlank { stringResource(R.string.zip_browse_title) },
        onGoBack = onBack,
        actions = {},
        isShowDefaultActions = false,
        showNavigationBarsPadding = false,
        isBackHandler = false
    ) {
        when (val state = component.state) {
            is ZipBrowserComponent.State.Loading -> {
                Box(modifier = Modifier.weight(1f)) {
                    ZipLoadingState(progress = component.loadProgress)
                }
            }

            is ZipBrowserComponent.State.Error -> {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.isAccessDenied) {
                        ZipAccessDeniedState(component)
                    } else {
                        ZipArchiveErrorState(
                            isUnsupportedFormat = state.isUnsupportedFormat,
                            isEncrypted = state.isEncrypted,
                            onRetry = { component.retry() }
                        )
                    }
                }
            }

            is ZipBrowserComponent.State.Ready -> {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            // 给底部通栏按钮留位，滚动到底时最后一行不被挡住
                            bottom = 88.dp + navigationBarInset
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item(key = "zip_archive_header") {
                            ZipArchiveHeaderCard(
                                name = component.archiveName,
                                fileCount = component.fileCount,
                                totalSize = component.totalSize,
                                compressionPercent = component.compressionPercent
                            )
                        }

                        component.currentFolder?.let { folder ->
                            item(key = "zip_breadcrumb") {
                                ZipFolderPathRow(path = folder)
                            }
                        }

                        if (component.visibleItems.isEmpty()) {
                            item(key = "zip_empty") {
                                ZipEmptyArchiveState(
                                    message = stringResource(
                                        if (component.currentFolder == null) {
                                            R.string.zip_empty_archive
                                        } else {
                                            R.string.zip_empty_folder
                                        }
                                    )
                                )
                            }
                        } else {
                            items(
                                items = component.visibleItems,
                                key = { item ->
                                    when (item) {
                                        is ZipBrowserItem.Folder -> "folder:${item.path}"
                                        is ZipBrowserItem.File -> "file:${item.path}"
                                    }
                                }
                            ) { item ->
                                when (item) {
                                    is ZipBrowserItem.Folder -> {
                                        ZipFolderRow(
                                            item = item,
                                            onClick = { component.openFolder(item.path) }
                                        )
                                    }

                                    is ZipBrowserItem.File -> {
                                        ZipFileRow(
                                            entry = item.entry,
                                            onExtract = {
                                                pendingExtractEntry = item.entry
                                                extractEntryLauncher.launch(item.entry.name)
                                            },
                                            onShowDetails = { detailsEntry = item.entry }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (component.fileCount > 0) {
                    ZipBottomActionBar(
                        text = stringResource(R.string.zip_extract_all),
                        enabled = !component.isExtracting,
                        // 从上次的目录（或 App 工作目录）开始，别再扔回存储根目录
                        onClick = { folderPicker.pickFolder(component.extractStartUri()) }
                    )
                }
            }
        }
    }

    BackHandler(onBack = onBack)

    detailsEntry?.let { entry ->
        ZipEntryDetailsDialog(
            entry = entry,
            onDismiss = { detailsEntry = null }
        )
    }

    ZipProgressDialog(
        visible = component.isExtracting,
        progress = component.extractProgress,
        onCancelLoading = component::cancelExtraction
    )
}

private fun showZipFailure(resources: Resources, throwable: Throwable) {
    when (throwable) {
        is EncryptedArchiveException -> AppToastHost.showFailureToast(
            resources.getString(R.string.zip_encrypted_unsupported)
        )

        is ArchiveUnavailableException -> AppToastHost.showFailureToast(
            resources.getString(R.string.zip_archive_closed)
        )

        else -> AppToastHost.showFailureToast(throwable)
    }
}

/**
 * file:// 无读权限:接入统一授权引导(zip 非媒体文件,只有 SAF 文件夹授权一条路)。
 * 授权成功后用恢复出的文档 Uri 重试加载。
 */
@Composable
private fun ZipAccessDeniedState(component: ZipBrowserComponent) {
    val accessController = rememberFileAccessController(
        uriString = component.sourceUri.toString(),
        isLoadFailed = true,
        mediaReadPermission = null
    )

    LaunchedEffect(accessController.recoveredUriString) {
        accessController.recoveredUriString?.let { component.retry(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 静默检查已持久化的授权,期间不闪错误卡
            accessController.isCheckingPersistedGrants -> {
                ZipLoadingState(progress = ZipProgress())
            }

            accessController.isGuidanceVisible -> {
                // 引导面板是深色设计(固定白字),浅色页面垫一层深色容器
                Surface(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = AccessDeniedGuidanceContainerColor
                ) {
                    FileAccessGuidance(accessController)
                }
            }

            else -> {
                ZipArchiveErrorState(
                    isUnsupportedFormat = false,
                    isEncrypted = false,
                    onRetry = { component.retry() }
                )
            }
        }
    }
}

/** FileAccessGuidance 固定白字,浅色页面下垫的深色容器色(不随主题)。 */
private val AccessDeniedGuidanceContainerColor = Color(0xFF1B1C20)
