package com.wanbaohe.markdown.edit.screen

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
import com.wanbaohe.markdown.edit.component.MarkdownEditorComponent
import com.wanbaohe.markdown.edit.component.MarkdownEditorUiState
import com.wanbaohe.markdown.edit.webview.WebViewMarkdownEditor
import com.wanbaohe.markdown.edit.webview.rememberWebViewMarkdownEditorState
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSaveAs

/**
 * 纯净的 Markdown 编辑器页面
 *
 * 功能：
 * - 基于 WebView 的所见即所得 Markdown 编辑
 * - 打开 MD 文件编辑
 * - 保存为 MD 文件
 * - 新建文件
 */
@Composable
fun MarkdownEditorScreen(
    component: MarkdownEditorComponent
) {
    val context = LocalComponentActivity.current
    val scope = rememberCoroutineScope()


    val uiState by component.uiState.collectAsState()
    val editorState = rememberWebViewMarkdownEditorState()

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // 文件保存器（用于新建文件或另存为）
    val fileSaver = rememberFileCreator(
        mimeType = MimeType.Markdown,
        onSuccess = { uri ->
            scope.launch {
                val content = editorState.getContent()
                component.saveToNewFile(
                    context = context,
                    uri = uri,
                    content = content,
                    onSuccess = {
                        editorState.clearDraft()
                        component.parseSaveResult(it)
                        // 执行待处理的操作（如退出）
                        pendingAction?.invoke()
                        pendingAction = null
                    },
                    onFailure = { msg: String ->
                        AppToastHost.showToast(msg)
                    }
                )
            }
        }
    )

    // 文件选择器
    val filePicker = rememberFilePicker(
        mimeType = MimeType.Markdown,
        onSuccess = { uri: Uri ->
            component.loadFile(context, uri)
        }
    )

    // 当 uiState.content 变化时，设置到编辑器
    // 不使用 uiState.fileName 作为 key：保存后 fileName 可能变化但 content 未更新，
    // 会导致 setContent 用旧内容覆盖编辑器中的用户编辑
    LaunchedEffect(uiState.content, uiState.isLoading) {
        if (!uiState.isLoading) {
            // 无论内容是否为空都要设置，确保新建文件时能清空编辑器
            editorState.setContent(uiState.content)
        }
    }

    // 处理带确认的操作
    fun handleActionWithConfirm(action: () -> Unit) {
        if (uiState.isDirty) {
            pendingAction = action
            showExitConfirmDialog = true
        } else {
            action()
        }
    }

    BaseScreen(
        title = resolveTitle(uiState),
        onGoBack = {
            handleActionWithConfirm { component.onGoBack() }
        },
        supportGlassEffect = false,
        actions = {
            // 保存按钮（独立显示，方便快捷操作）
            IconButton(
                onClick = {
                    scope.launch {
                        val content = editorState.getContent()
                        if (component.isEditMode()) {
                            component.saveEditResult(content) {
                                AppToastHost.showToast(context.getString(R.string.save_success))
                                component.onGoBack()
                            }
                        } else if (component.hasExistingFile()) {
                            // 覆盖保存到已有文件
                            component.saveToExistingFile(
                                context = context,
                                content = content,
                                onSuccess = {
                                    editorState.clearDraft()
                                    component.parseSaveResult(saveResult = it)
                                    AppToastHost.showToast(context.getString(R.string.save_success))
                                },
                                onFailure = { msg ->
                                    AppToastHost.showToast(msg)
                                }
                            )
                        } else {
                            // 新文件，弹出保存对话框
                            fileSaver.make(component.generateFileNameFromContent(content))
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.save_file),
                    tint = if (uiState.isDirty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // 更多菜单
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    if (!component.isEditMode()) {
                        // 新建
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
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
                                    contentDescription = null
                                )
                            }
                        )

                        // 打开文件
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.open_file)) },
                            onClick = {
                                showMenu = false
                                handleActionWithConfirm {
                                    filePicker.pickFile()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileOpen,
                                    contentDescription = null
                                )
                            }
                        )

                        // 另存为
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
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSaveAs,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    // 保存为PDF
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.save_as_pdf)) },
                        onClick = {
                            showMenu = false
                            scope.launch {
                                val content = editorState.getContent()

                                component.savePdf(
                                    editorState.webView,
                                    context,
                                    component.generateFileNameFromContent(content)
                                        .replace(".md", ".pdf")
                                ) { flag, msg ->
                                    if (flag) {
                                        AppToastHost.showToast(
                                            msg ?: context.getString(R.string.save_success)
                                        )
                                    } else {
                                        AppToastHost.showToast(
                                            msg ?: context.getString(R.string.save_failed)
                                        )
                                    }
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                                contentDescription = null
                            )
                        }
                    )

                    // 历史记录分隔线
                    if (uiState.historyList.isNotEmpty()) {
                        HorizontalDivider()

                        // 历史记录标题
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.recent_files),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { },
                            enabled = false
                        )

                        // 历史记录列表（最多显示5条）
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
                                        component.loadFile(context, entry.uri)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        // 清除历史
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
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        },
        isBackHandler = false,
        showNavigationBarsPadding = false
    ) {
        WebViewMarkdownEditor(
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            state = editorState,
            initialValue = uiState.content,
            placeholder = stringResource(R.string.note_placeholder),
            textStyle = EditorUiDefaults.contentTextStyle(),
            storageKey = "markdown_editor_screen",
            onContentChanged = {
                component.markAsDirty()
            }
        )
    }

    // 退出确认弹窗
    EnhancedAlertDialog(
        visible = showExitConfirmDialog,
        onDismissRequest = {
            showExitConfirmDialog = false
            pendingAction = null
        },
        title = { Text(stringResource(R.string.unsaved_changes)) },
        text = { Text(stringResource(R.string.unsaved_changes_message)) },
        confirmButton = {
            ConfirmButton(
                stringResource(R.string.save_file)
            ) {
                showExitConfirmDialog = false
                // 保存后执行待处理操作
                scope.launch {
                    val content = editorState.getContent()
                    if (component.isEditMode()) {
                        component.saveEditResult(content) {
                            pendingAction?.invoke()
                            pendingAction = null
                        }
                    } else if (component.hasExistingFile()) {
                        // 覆盖保存到已有文件
                        component.saveToExistingFile(
                            context = context,
                            content = content,
                            onSuccess = {
                                editorState.clearDraft()
                                component.parseSaveResult(saveResult = it)
                                pendingAction?.invoke()
                                pendingAction = null
                            },
                            onFailure = { msg ->
                                AppToastHost.showToast(msg)
                            }
                        )
                    } else {
                        // 新文件，弹出保存对话框
                        fileSaver.make(component.generateFileNameFromContent(content))
                    }
                }
            }
        },
        dismissButton = {
            CancelButton(
                stringResource(R.string.discard)
            ) {
                showExitConfirmDialog = false
                val action = pendingAction
                pendingAction = null
                // 有 pendingAction (从"新建/打开/历史"menu 触发) → 执行该动作
                // 没有 pendingAction (从返回键触发) → 退出页面
                if (action != null) action.invoke()
                else component.onGoBack()
            }
        }
    )

    // 返回键处理
    BackHandler {
        handleActionWithConfirm { component.onGoBack() }
    }
}

/**
 * 解析标题:
 * - 优先 editTitle (edit 模式)
 * - 然后 fileName (打开文件)
 * - 都没有时,返回空白标题 (新建状态)
 */
private fun resolveTitle(uiState: MarkdownEditorUiState): String {
    return when {
        !uiState.editTitle.isNullOrBlank() -> uiState.editTitle
        !uiState.fileName.isNullOrBlank() -> uiState.fileName
        else -> ""  // 新建状态:标题空白
    }
}
