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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("FunctionNaming", "LongMethod", "CyclomaticComplexMethod") // Compose 页面装配：多条独立流程集中接线

package com.wanbaohe.code.editor.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.wanbaohe.base.ui.ClearHistoryConfirmDialog
import com.wanbaohe.code.editor.component.CodeEditorComponent
import com.wanbaohe.code.editor.component.CodeEditorUiState
import com.wanbaohe.code.editor.ui.NativeCodeEditor
import com.wanbaohe.codeeditor.CodeLanguage
import com.wanbaohe.codeeditor.JsonCodeFormatter
import com.wanbaohe.codeeditor.JsonFormatResult
import com.wanbaohe.codeeditor.rememberCodeEditorState
import com.wanbaohe.core.ui.utils.content.FileAccessGuidance
import com.wanbaohe.core.ui.utils.content.rememberFileAccessController
import com.wanbaohe.core.ui.utils.content_pickers.rememberFileCreator
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.wanbaohe.code.editor.R as EditorR

/**
 * 原生代码编辑器页面（纯 Kotlin / Compose）。
 *
 * 与 Markdown 编辑器共用同一套页面骨架：
 * - 顶栏只有保存与更多菜单；
 * - 正文下方的工具坞承载撤销/重做、查找、缩进、注释、整行操作、软换行与剪贴板操作；
 * - 选中文字浮出自绘工具条，退出前对未保存内容做确认；
 * - 输入停顿后把未保存内容落成草稿，进程被杀后再进来能恢复。
 */
@Composable
fun CodeEditorScreen(
    component: CodeEditorComponent,
) {
    val context = LocalComponentActivity.current
    val scope = rememberCoroutineScope()
    val uiState by component.uiState.collectAsState()
    val editorState = rememberCodeEditorState(
        initialText = uiState.content,
        initialLanguage = uiState.language,
    )

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isFormatting by remember { mutableStateOf(false) }
    val showClearHistoryDialog = remember { mutableStateOf(false) }
    // 每个草稿桶只尝试恢复一次：换文件/新建才重新尝试，另存为不会触发
    var restoredDraftKey by remember { mutableStateOf<String?>(null) }

    // 外部内容变化时整体载入，并尝试恢复该文件的未保存草稿
    LaunchedEffect(uiState.content, uiState.isLoading, uiState.loadFailed) {
        if (!uiState.isLoading && !uiState.loadFailed) {
            editorState.load(uiState.content, uiState.language)
            val draftKey = component.draftKey()
            if (restoredDraftKey != draftKey) {
                restoredDraftKey = draftKey
                val draft = component.restoreDraft()
                if (draft != null) {
                    editorState.load(draft, uiState.language)
                    component.markAsDirty()
                    AppToastHost.showToast(
                        context.getString(
                            com.wanbaohe.code.editor.R.string.code_editor_draft_restored
                        )
                    )
                }
            }
        }
    }
    LaunchedEffect(uiState.language) {
        editorState.updateLanguage(uiState.language)
    }
    // 输入停顿后落一次草稿：进程被杀再进来能恢复最后一段未保存内容
    LaunchedEffect(editorState) {
        snapshotFlow { editorState.userContentVersion }
            .drop(1)
            .collectLatest {
                delay(DRAFT_SAVE_DEBOUNCE_MS)
                component.persistDraft(editorState.text)
            }
    }

    // file:// 直开无读权限时走统一授权引导（代码文本非媒体文件，无媒体权限路径）
    val accessController = rememberFileAccessController(
        uriString = uiState.fileUri?.toString().orEmpty(),
        isLoadFailed = uiState.loadFailed,
        mediaReadPermission = null,
    )
    LaunchedEffect(accessController.recoveredUriString) {
        val recovered = accessController.recoveredUriString ?: return@LaunchedEffect
        runCatching { Uri.parse(recovered) }.getOrNull()?.let { component.loadFile(context, it) }
    }

    fun handleActionWithConfirm(action: () -> Unit) {
        if (uiState.isDirty) {
            pendingAction = action
            showExitConfirmDialog = true
        } else {
            action()
        }
    }

    val fileSaver = rememberFileCreator(
        mimeType = MimeType.All,
        onSuccess = { uri ->
            scope.launch {
                // 另存为后 fileUri 会变，草稿桶也跟着换；旧桶要先清掉
                val draftKeyBeforeSave = component.draftKey()
                component.saveToNewFile(
                    context = context,
                    uri = uri,
                    content = editorState.text,
                    onSuccess = {
                        scope.launch { component.clearDraft(draftKeyBeforeSave) }
                        notifyFileSaved(context, uri)
                        pendingAction?.invoke()
                        pendingAction = null
                    },
                    onFailure = { msg -> AppToastHost.showToast(msg) },
                )
            }
        },
    )
    val filePicker = rememberFilePicker(
        mimeType = MimeType.All,
        onSuccess = { uri: Uri -> component.loadFile(context, uri) },
    )

    fun saveToExistingFile(afterSave: () -> Unit) {
        component.saveToExistingFile(
            context = context,
            content = editorState.text,
            onSuccess = {
                editorState.markSaved()
                scope.launch { component.clearDraft() }
                uiState.fileUri?.let { uri -> notifyFileSaved(context, uri) }
                afterSave()
            },
            onFailure = { msg -> AppToastHost.showToast(msg) },
        )
    }

    fun saveCurrent(afterEditModeSave: () -> Unit, afterFileSave: () -> Unit) {
        val content = editorState.text
        when {
            component.isEditMode() -> component.saveEditResult(content) {
                AppToastHost.showToast(context.getString(R.string.code_editor_save))
                afterEditModeSave()
            }

            uiState.fileUri != null -> saveToExistingFile(afterSave = afterFileSave)
            else -> fileSaver.make(component.generateFileNameFromContent(content))
        }
    }

    BackHandler {
        when {
            editorState.isFindVisible -> editorState.closeFind()
            uiState.isDirty -> showExitConfirmDialog = true
            else -> component.onGoBack()
        }
    }

    BaseScreen(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = resolveTitle(uiState).ifBlank { stringResource(R.string.code_editor_title) },
        onGoBack = { handleActionWithConfirm { component.onGoBack() } },
        supportGlassEffect = false,
        isBackHandler = false,
        showNavigationBarsPadding = false,
        actions = {
            CodeEditorSaveAction(isDirty = uiState.isDirty) {
                scope.launch {
                    saveCurrent(
                        afterEditModeSave = { component.onGoBack() },
                        afterFileSave = {},
                    )
                }
            }
            CodeEditorMoreMenu(
                uiState = uiState,
                isEditMode = component.isEditMode(),
                actions = CodeEditorMenuActions(
                    onNewFile = {
                        handleActionWithConfirm {
                            component.discardDraft()
                            component.newFile()
                            editorState.load("")
                        }
                    },
                    onOpenFile = { handleActionWithConfirm { filePicker.pickFile() } },
                    onSaveAs = {
                        scope.launch {
                            fileSaver.make(
                                component.generateFileNameFromContent(editorState.text)
                            )
                        }
                    },
                    onOpenRecent = { uriString ->
                        val uri = runCatching { Uri.parse(uriString) }.getOrNull()
                        if (uri != null) {
                            handleActionWithConfirm { component.loadFile(context, uri) }
                        }
                    },
                    onClearHistory = { showClearHistoryDialog.value = true },
                    onFormatJson = {
                        scope.launch {
                            isFormatting = true
                            try {
                                val source = editorState.text
                                val version = editorState.contentVersion
                                val tabWidth = editorState.tabWidth
                                val result = withContext(Dispatchers.Default) {
                                    JsonCodeFormatter.format(source, tabWidth)
                                }
                                if (version != editorState.contentVersion ||
                                    component.uiState.value.isLoading ||
                                    uiState.fileUri != component.uiState.value.fileUri
                                ) return@launch
                                when (result) {
                                    is JsonFormatResult.Formatted -> {
                                        editorState.updateLanguage(CodeLanguage.Json)
                                        component.updateLanguage(CodeLanguage.Json)
                                        editorState.replaceDocumentText(result.text)
                                        AppToastHost.showToast(context.getString(EditorR.string.code_editor_json_formatted))
                                    }
                                    is JsonFormatResult.Invalid -> {
                                        val line = source.take(result.offset).count { it == '\n' } + 1
                                        editorState.requestReveal(result.offset)
                                        AppToastHost.showToast(
                                            context.getString(EditorR.string.code_editor_json_invalid, line)
                                        )
                                    }
                                    JsonFormatResult.TooLarge -> AppToastHost.showToast(
                                        context.getString(EditorR.string.code_editor_json_too_large)
                                    )
                                }
                            } finally {
                                isFormatting = false
                            }
                        }
                    },
                    onExpandAll = editorState::expandAllBlocks,
                ),
                canFormatJson = !isFormatting && !uiState.isLoading && !uiState.loadFailed &&
                    editorState.text.isNotBlank(),
                hasFoldedBlocks = editorState.hasFoldedBlocks,
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NativeCodeEditor(
                state = editorState,
                modifier = Modifier.fillMaxSize(),
                onContentChanged = component::markAsDirty,
                onLanguageChange = component::updateLanguage,
            )
            if (accessController.isGuidanceVisible || accessController.isCheckingPersistedGrants) {
                FileAccessOverlay(accessController = accessController)
            }
        }
    }

    ClearHistoryConfirmDialog(
        showDialog = showClearHistoryDialog,
        onConfirm = component::clearHistory,
    )

    CodeEditorExitDialog(
        visible = showExitConfirmDialog,
        onDismiss = {
            showExitConfirmDialog = false
            pendingAction = null
        },
        onSaveAndExit = {
            showExitConfirmDialog = false
            scope.launch {
                val afterSave = {
                    pendingAction?.invoke()
                    pendingAction = null
                }
                saveCurrent(afterEditModeSave = afterSave, afterFileSave = afterSave)
            }
        },
        onDiscard = {
            showExitConfirmDialog = false
            component.discardDraft()
            val action = pendingAction
            pendingAction = null
            if (action != null) action() else component.onGoBack()
        },
    )
}

/** 授权引导覆盖层：面板是深色背景设计，页面浅色故垫深色容器。 */
@Composable
private fun FileAccessOverlay(
    accessController: com.wanbaohe.core.ui.utils.content.FileAccessController,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (accessController.isGuidanceVisible) {
            FileAccessGuidance(accessController)
        } else {
            // 静默检查已持久化授权期间只出 loading，不闪引导面板
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

/** 标准保存成功提示：uri 转展示用全路径，拆出文件名与目录后走统一管线。 */
private fun notifyFileSaved(
    context: android.content.Context,
    uri: Uri,
) {
    val fullPath = uri.toString().getPath(context)
    val fileName = fullPath.substringAfterLast('/')
    SaveSuccessNotifier.notifyFileSaved(
        fileUri = uri.toString(),
        fileName = fileName,
        savingPath = fullPath.removeSuffix("/$fileName"),
    )
}

/**
 * 解析标题：
 * - 优先 editTitle（edit 模式）
 * - 然后 fileName（打开文件）
 * - 都没有时返回空白标题（新建状态）
 */
private fun resolveTitle(uiState: CodeEditorUiState): String = when {
    !uiState.editTitle.isNullOrBlank() -> uiState.editTitle
    !uiState.fileName.isNullOrBlank() -> uiState.fileName
    else -> ""
}

/** 输入停顿多久落一次草稿。 */
private const val DRAFT_SAVE_DEBOUNCE_MS = 800L
