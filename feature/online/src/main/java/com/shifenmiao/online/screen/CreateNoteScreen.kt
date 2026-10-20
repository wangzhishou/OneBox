package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.NOTE_EDITOR_TOOLBAR_EXTRAS
import com.shifenmiao.online.component.NOTE_TOOLBAR_ACTION_CATEGORY
import com.shifenmiao.online.component.NOTE_TOOLBAR_ACTION_TITLE
import com.shifenmiao.online.ui.NoteCategoryDialog
import com.shifenmiao.online.ui.NoteTitleDialog
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.text.EditorUiDefaults
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.markdown.edit.webview.WebViewMarkdownEditor
import com.wanbaohe.markdown.edit.webview.rememberWebViewMarkdownEditorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
fun CreateNoteScreen(
    createNoteComponent: CreateNoteComponent,
    appComponent: AppComponent
) {
    val uiState by createNoteComponent.uiState.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val showCategoryDialog = rememberSaveable { mutableStateOf(false) }
    val showTitleDialog = rememberSaveable { mutableStateOf(false) }
    var titleContentHint by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val editorState = rememberWebViewMarkdownEditorState()

    val onBack = {
        if (uiState.isDirty) {
            showExitDialog.value = true
        } else {
            appComponent.onGoBack()
        }
    }

    // 当数据加载完成时，设置初始内容
    LaunchedEffect(uiState.data) {
        if (uiState.data.isNotEmpty()) {
            editorState.setContent(uiState.data)
        }
    }
    val titlePlaceholder = if (uiState.isEditing) {
        stringResource(R.string.edit_note)
    } else {
        stringResource(R.string.new_note)
    }

    BaseScreen(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = {
            Text(
                text = titlePlaceholder,
                style = MaterialTheme.typography.titleLarge
            )
        },
        type = EnhancedTopAppBarType.Center,
        onGoBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        val markdown = editorState.getContent()
                        createNoteComponent.saveMarkDownData(
                            markdown = markdown,
                            onSuccess = {
                                scope.launch(Dispatchers.Main) {
                                    editorState.clearDraft()
                                    AppToastHost.showToast(
                                        AppContext.getString(R.string.save_success)
                                    )
                                    appComponent.onNavigateReplacingCurrent(
                                        Screen.NewApp(initialTab = HomeTabKey.TEXT)
                                    )
                                }
                            },
                            onFailure = { errorMsg ->
                                AppToastHost.showToast(errorMsg)
                            }
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.done_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportGlassEffect = false,
        showNavigationBarsPadding = true
    ) {
        WebViewMarkdownEditor(
            initialValue = uiState.data,
            state = editorState,
            placeholder = stringResource(R.string.note_placeholder),
            textStyle = EditorUiDefaults.contentTextStyle(),
            modifier = Modifier
                .weight(1f),
            storageKey = "create_note_item",
            onContentChanged = {
                createNoteComponent.markAsDirty()
            },
            toolbarExtras = NOTE_EDITOR_TOOLBAR_EXTRAS,
            onCustomToolbarAction = { action ->
                when (action) {
                    NOTE_TOOLBAR_ACTION_TITLE -> {
                        scope.launch {
                            titleContentHint = MarkdownSummary.derive(editorState.getContent()).title
                            showTitleDialog.value = true
                        }
                    }
                    NOTE_TOOLBAR_ACTION_CATEGORY -> showCategoryDialog.value = true
                }
            }
        )
    }

    BackHandler(onBack = onBack)

    ExitWithoutSavingDialog(
        onExit = {
            appComponent.onGoBack()
        },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value
    )

    // 分类选择/管理浮动弹窗
    NoteCategoryDialog(
        visible = showCategoryDialog.value,
        createNoteComponent = createNoteComponent,
        uiState = uiState,
        onDismiss = { showCategoryDialog.value = false }
    )

    // 自定义标题弹窗：无标题时以文章开头内容作占位提示
    NoteTitleDialog(
        visible = showTitleDialog.value,
        initialTitle = uiState.title,
        contentHint = titleContentHint,
        onConfirm = { newTitle ->
            createNoteComponent.onTitleChange(newTitle)
            showTitleDialog.value = false
        },
        onDismiss = { showTitleDialog.value = false }
    )
}
