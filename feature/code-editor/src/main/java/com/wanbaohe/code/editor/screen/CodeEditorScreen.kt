package com.wanbaohe.code.editor.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import com.wanbaohe.code.editor.component.CodeEditorComponent
import com.wanbaohe.code.editor.component.CodeEditorUiState
import com.wanbaohe.code.editor.webview.WebViewCodeEditor
import com.wanbaohe.code.editor.webview.rememberWebViewCodeEditorState
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSaveAs

/**
 * WebView 通用代码编辑器页面
 *
 * Phase 6：完整业务能力
 * - 文件 I/O（保存/另存为/打开）
 * - 历史记录（基于 [com.shifenmiao.common.recent.RecentAccessRepository]）
 * - 退出确认（isDirty → EnhancedAlertDialog）
 * - More 菜单（新建/打开/另存为/历史/清空）
 */
@Composable
fun CodeEditorScreen(
    component: CodeEditorComponent
) {
    val context = LocalComponentActivity.current
    val scope = rememberCoroutineScope()
    val uiState by component.uiState.collectAsState()
    val editorState = rememberWebViewCodeEditorState()

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // 同步外部加载的文件内容到编辑器
    // - loadFile 时:content 变化 → 同步
    // - newFile 时:content 清空 + fileUri=null → 仍要同步清空 WebView
    // - 用户编辑时:isDirty=true → 不再同步,保留用户输入
    LaunchedEffect(uiState.content, uiState.language, uiState.isLoading) {
        if (!uiState.isLoading && !uiState.isDirty) {
            editorState.setContent(uiState.content)
            editorState.setLanguage(uiState.language)
        }
    }

    // 退出确认处理
    fun handleActionWithConfirm(action: () -> Unit) {
        if (uiState.isDirty) {
            pendingAction = action
            showExitConfirmDialog = true
        } else {
            action()
        }
    }

    // 用 */* 打开保存对话框，避免 text/plain 时系统强制改成 .txt
    // 默认文件名扩展名由 generateFileNameFromContent 按语言生成
    val fileSaver = rememberFileCreator(
        mimeType = MimeType.All,
        onSuccess = { uri ->
            scope.launch {
                val content = editorState.getContent()
                component.saveToNewFile(
                    context = context,
                    uri = uri,
                    content = content,
                    onSuccess = {
                        editorState.clearDraft()
                        AppToastHost.showToast(context.getString(R.string.save_success))
                        pendingAction?.invoke()
                        pendingAction = null
                    },
                    onFailure = { msg -> AppToastHost.showToast(msg) }
                )
            }
        }
    )

    // 文件选择器（打开） - 用 */* 让系统文件选择器能选 .json/.kt/.html 等
    val filePicker = rememberFilePicker(
        mimeType = MimeType.All,
        onSuccess = { uri: Uri ->
            component.loadFile(context, uri)
        }
    )

    BackHandler {
        when {
            editorState.isPopupOpen -> editorState.closePopup()
            uiState.isDirty -> showExitConfirmDialog = true
            else -> component.onGoBack()
        }
    }

    BaseScreen(
        title = resolveTitle(uiState),
        onGoBack = { handleActionWithConfirm { component.onGoBack() } },
        supportGlassEffect = false,
        isBackHandler = false,
        showNavigationBarsPadding = false,
        actions = {
            // 保存按钮
            IconButton(
                onClick = {
                    scope.launch {
                        val content = editorState.getContent()
                        if (component.isEditMode()) {
                            component.saveEditResult(content) {
                                AppToastHost.showToast(context.getString(R.string.code_editor_save))
                                component.onGoBack()
                            }
                        } else if (uiState.fileUri != null) {
                            // 覆盖保存
                            component.saveToExistingFile(
                                context = context,
                                content = content,
                                onSuccess = {
                                    editorState.clearDraft()
                                    AppToastHost.showToast(context.getString(R.string.save_success))
                                },
                                onFailure = { msg -> AppToastHost.showToast(msg) }
                            )
                        } else {
                            // 新文件 → 弹保存对话框
                            fileSaver.make(component.generateFileNameFromContent(content))
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.save_file),
                    tint = if (uiState.isDirty) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            // More 菜单
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    if (!component.isEditMode()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_file)) },
                            onClick = {
                                showMenu = false
                                handleActionWithConfirm {
                                    component.newFile()
                                    editorState.clearDraft()
                                }
                            },
                            leadingIcon = {
                                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.open_file)) },
                            onClick = {
                                showMenu = false
                                handleActionWithConfirm { filePicker.pickFile() }
                            },
                            leadingIcon = {
                                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileOpen, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.save_as)) },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    val content = editorState.getContent()
                                    fileSaver.make(component.generateFileNameFromContent(content))
                                }
                            },
                            leadingIcon = {
                                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSaveAs, contentDescription = null)
                            }
                        )
                    }

                    if (uiState.historyList.isNotEmpty()) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.recent_files),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        uiState.historyList.take(5).forEach { entry ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = entry.displayName,
                                        maxLines = 1
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    handleActionWithConfirm {
                                        val uri = runCatching { Uri.parse(entry.uri) }.getOrNull()
                                        if (uri != null) component.loadFile(context, uri)
                                    }
                                },
                                leadingIcon = {
                                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.clear_history),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                component.clearHistory()
                            },
                            leadingIcon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    ) {
        // 每个文件/新建状态用独立的 storage key,避免分屏/多窗口实例间草稿互相覆盖
        val storageKey = remember(uiState.fileUri) {
            uiState.fileUri?.toString() ?: "code_editor_new_file"
        }
        WebViewCodeEditor(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            state = editorState,
            initialValue = uiState.content,
            language = uiState.language,
            placeholder = stringResource(com.wanbaohe.code.editor.R.string.code_editor_placeholder),
            storageKey = storageKey,
            textStyle = EditorUiDefaults.contentTextStyle(monospaced = true),
            onContentChanged = { component.markAsDirty() }
        )
    }

    // 退出确认弹窗
    if (showExitConfirmDialog) {
        EnhancedAlertDialog(
            visible = showExitConfirmDialog,
            onDismissRequest = {
                showExitConfirmDialog = false
                pendingAction = null
            },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                ConfirmButton(stringResource(R.string.save_file)) {
                    showExitConfirmDialog = false
                    scope.launch {
                        val content = editorState.getContent()
                        if (component.isEditMode()) {
                            component.saveEditResult(content) {
                                pendingAction?.invoke()
                                pendingAction = null
                            }
                        } else if (uiState.fileUri != null) {
                            component.saveToExistingFile(
                                context = context,
                                content = content,
                                onSuccess = {
                                    editorState.clearDraft()
                                    pendingAction?.invoke()
                                    pendingAction = null
                                },
                                onFailure = { msg -> AppToastHost.showToast(msg) }
                            )
                        } else {
                            fileSaver.make(component.generateFileNameFromContent(content))
                        }
                    }
                }
            },
            dismissButton = {
                CancelButton(stringResource(R.string.discard)) {
                    showExitConfirmDialog = false
                    val action = pendingAction
                    pendingAction = null
                    // 有 pendingAction (从"新建/打开/历史"菜单触发) → 执行该动作
                    // 没有 pendingAction (从返回键触发) → 退出页面
                    if (action != null) action.invoke()
                    else component.onGoBack()
                }
            }
        )
    }
}

/**
 * 解析标题:
 * - 优先 editTitle (edit 模式)
 * - 然后 fileName (打开文件)
 * - 都没有时,返回空白标题 (新建状态)
 */
private fun resolveTitle(uiState: CodeEditorUiState): String {
    return when {
        !uiState.editTitle.isNullOrBlank() -> uiState.editTitle
        !uiState.fileName.isNullOrBlank() -> uiState.fileName
        else -> ""  // 新建状态:标题空白
    }
}
