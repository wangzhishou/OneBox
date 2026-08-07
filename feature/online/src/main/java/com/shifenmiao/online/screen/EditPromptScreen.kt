package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.ui.PromptCategorySelection
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorTitleField
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import com.wanbaohe.markdown.edit.webview.WebViewMarkdownEditor
import com.wanbaohe.markdown.edit.webview.rememberWebViewMarkdownEditorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
fun EditPromptScreen(
    editPromptComponent: EditPromptComponent,
    appComponent: AppComponent
) {
    val uiState by editPromptComponent.uiState.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val titlePlaceholder = if (uiState.isEditing) {
        stringResource(R.string.edit_prompt)
    } else {
        stringResource(R.string.new_prompt)
    }
    fun handleEditorScrollDelta(deltaY: Float) {
        val topBarState = scrollBehavior.state
        topBarState.contentOffset += deltaY
        topBarState.heightOffset = (topBarState.heightOffset - deltaY)
            .coerceIn(topBarState.heightOffsetLimit, 0f)
    }

    BaseScreen(
        title = {
            EditorTitleField(
                value = uiState.title,
                onValueChange = editPromptComponent::onTitleChange,
                placeholder = titlePlaceholder,
                modifier = Modifier.fillMaxWidth()
            )
        },
        scrollBehavior = scrollBehavior,
        type = EnhancedTopAppBarType.Medium,
        onGoBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        val markdown = editorState.getContent()
                        editPromptComponent.savePromptData(
                            markdown = markdown,
                            onSuccess = {
                                scope.launch(Dispatchers.Main) {
                                    editorState.clearDraft()
                                    AppToastHost.showToast(
                                        AppContext.getString(R.string.save_success)
                                    )
                                    appComponent.onGoBack()
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
        showNavigationBarsPadding = true,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Category chips
            PromptCategorySelection(
                modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingNormal, vertical = 4.dp),
                editPromptComponent = editPromptComponent,
                uiState = uiState
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Markdown editor
            WebViewMarkdownEditor(
                initialValue = uiState.data,
                state = editorState,
                placeholder = stringResource(R.string.note_placeholder),
                textStyle = EditorUiDefaults.contentTextStyle(),
                onVerticalScrollDelta = ::handleEditorScrollDelta,
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .weight(1f)
                    .navigationBarsPadding(),
                storageKey = "edit_prompt_item",
                onContentChanged = {
                    editPromptComponent.markAsDirty()
                }
            )
        }
    }

    BackHandler(onBack = onBack)

    ExitWithoutSavingDialog(
        onExit = {
            appComponent.onGoBack()
        },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value
    )
}
