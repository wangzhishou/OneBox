package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.ui.MarkdownLazyContent
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberCodeBlockClickListener
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.online.ui.ReadOnlyNoteCategorySelection
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import kotlinx.coroutines.launch
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock

@Composable
fun NoteItemScreen(
    noteItemComponent: NoteItemComponent,
    appComponent: AppComponent
) {
    val noteUIState by noteItemComponent.noteUIState.collectAsState()
    val navigator = LocalUrlNavigator.current
    val uriHandler = LocalUriHandler.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()
    var isTextSelectionMode by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // 页面显示时刷新数据，确保显示最新的编辑内容
    LaunchedEffect(Unit) {
        noteItemComponent.refreshData()
    }

    val codeBlockClickListener = rememberCodeBlockClickListener(appComponent = appComponent)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    BaseScreen(
        title = {
            Text(
                text = if (isTextSelectionMode) {
                    stringResource(R.string.select_text)
                } else {
                    noteUIState.title
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = MaterialTheme.typography.titleLarge.fontSize * 1.2f
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        onGoBack = {
            if (isTextSelectionMode) {
                isTextSelectionMode = false
            } else {
                appComponent.onGoBack()
            }
        },
        type = EnhancedTopAppBarType.Medium,
        scrollBehavior = scrollBehavior,
        actions = {
            if (isTextSelectionMode) {
                IconButton(
                    onClick = { isTextSelectionMode = false }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(end = AppTheme.dimens.paddingNormal),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (!noteUIState.isLocked) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val draftId = dataDraftHelper.createDraft(
                                draftType = ListItemType.NOTE.id,
                                itemId = noteUIState.itemId
                            )
                            navigator.navigate(Screen.CreateNote(draftId = draftId))
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(end = AppTheme.dimens.paddingNormal),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        showNavigationBarsPadding = true
    ) {
        if (noteUIState.isLocked) {
            LockedNoteContent(
                onUnlock = noteItemComponent::unlock,
                modifier = Modifier.fillMaxSize()
            )
            return@BaseScreen
        }
        ProvideMermaidRenderer {
            val baseModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            val contentModifier = if (isTextSelectionMode) {
                baseModifier
            } else {
                baseModifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        enterNoteTextSelectionMode(
                            isTextSelectionMode = isTextSelectionMode,
                            onEnter = { isTextSelectionMode = true }
                        )
                    })
                }
            }

            val markdownContent = remember {
                movableContentOf<Modifier> { mod ->
                    MarkdownLazyContent(
                        modifier = mod,
                        lazyListState = lazyListState,
                        codeBlockClickListener = codeBlockClickListener,
                        message = noteUIState.data,
                        isLoading = noteUIState.isLoading,
                        errorMessage = noteUIState.errorMessage,
                        onRetry = { noteItemComponent.refreshData() },
                        onGoBack = { appComponent.onGoBack() },
                        onLinkClick = { url ->
                            if (!navigator.navigate(url)) {
                                uriHandler.openUri(url)
                            }
                        },
                        paddingValues = PaddingValues(
                            vertical = 0.dp,
                            horizontal = AppTheme.dimens.paddingNormal
                        ),
                        headerContent = {
                            if (noteUIState.selectedCategories.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
                                    ReadOnlyNoteCategorySelection(uiState = noteUIState)
                                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceExtraSmall))
                                }
                            }
                        }
                    )
                }
            }

            if (isTextSelectionMode) {
                SelectionContainer { markdownContent(contentModifier) }
            } else {
                markdownContent(contentModifier)
            }
        }

    }

    BackHandler {
        if (isTextSelectionMode) {
            isTextSelectionMode = false
        } else {
            appComponent.onGoBack()
        }
    }

}

/**
 * 加锁笔记的占位内容:不渲染正文,点击后拉起全局授权码,解锁成功自动加载。
 */
@Composable
private fun LockedNoteContent(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(AppTheme.dimens.paddingNormal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.item_locked_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.item_locked_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            EnhancedButton(
                onClick = onUnlock,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = AppTheme.dimens.spaceNormal)
            ) {
                Text(stringResource(R.string.item_locked_unlock))
            }
        }
    }
}

private fun enterNoteTextSelectionMode(
    isTextSelectionMode: Boolean,
    onEnter: () -> Unit
) {
    if (isTextSelectionMode) return

    onEnter()
    AppToastHost.showToast(
        message = R.string.ai_chat_text_selection_mode_hint,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo
    )
}