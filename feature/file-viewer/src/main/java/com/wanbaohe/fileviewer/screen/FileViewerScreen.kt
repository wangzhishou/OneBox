package com.wanbaohe.fileviewer.screen

import android.text.format.Formatter
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.content.FileAccessGuidance
import com.wanbaohe.core.ui.utils.content.rememberFileAccessController
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.fileviewer.component.FileViewerComponent
import com.wanbaohe.fileviewer.component.FileViewerError
import com.wanbaohe.fileviewer.component.FileViewerUiState
import com.wanbaohe.fileviewer.component.SearchUiState

/**
 * 文件预览页。
 *
 * 顶栏沿用 App 的 [BaseScreen]（返回 + 文件名跑马灯），右侧按页面能力出现
 * 查找/夜间；正文是一整块 WebView，文档页自己负责滚动与缩放。
 * 页码做成底部小药丸，点击弹跳页框——只在页面真的回报页数后才出现。
 */
@Composable
fun FileViewerScreen(component: FileViewerComponent) {
    val state by component.uiState.collectAsState()
    var goToPageDialogVisible by remember { mutableStateOf(false) }

    /*
     * 夜间 = 全局日夜模式的生效值；顶栏由 BaseScreen 跟着主题走，这里不另画一套颜色。
     *
     * 现算而不是读 state.isNight：组件在配置变化时是 retained 的，里面那份 isNight
     * 只在设置流发新值时重算，系统深浅色在文档打开期间切换（黄昏、控制中心）它不知道。
     * UiSettingsState.isNightMode 是 toUiState 里算好的，那里读了 isSystemInDarkTheme，
     * 配置一变整条就重组，所以这里拿到的是此刻的值。
     */
    val isNight = LocalSettingsState.current.isNightMode
    LaunchedEffect(isNight) { component.applyNightMode(isNight) }

    /*
     * file:// 直开无权限时走统一授权引导：只挂 SAF 文件夹授权这条路（文档不是
     * 媒体文件，没有运行时媒体权限）；已持久化的授权会静默恢复成文档 Uri，
     * recoveredUriString 一非空就自动重试。
     */
    val accessController = rememberFileAccessController(
        uriString = component.uriString.orEmpty(),
        isLoadFailed = state.error == FileViewerError.CannotOpen,
        mediaReadPermission = null,
    )
    LaunchedEffect(accessController.recoveredUriString) {
        accessController.recoveredUriString?.let(component::retry)
    }

    BaseScreen(
        title = state.displayName.ifBlank { stringResource(R.string.file_viewer_title) },
        onGoBack = component.onGoBack,
        // 文档页不需要 BaseScreen 默认的 emoji 入口
        isShowDefaultActions = false,
        actions = {
            if (state.canSearch) {
                IconButton(
                    onClick = {
                        if (state.search.isOpen) component.closeSearch() else component.openSearch()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                    )
                }
            }
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        // 静默恢复检查期间也停在 loading：错误卡片闪一下再消失更像卡死
                        state.isLoading || accessController.isCheckingPersistedGrants ->
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                            )

                        accessController.isGuidanceVisible ->
                            // 引导面板是深色设计，本页底色可能很浅，垫一层深色容器
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = GuidanceBackground,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    FileAccessGuidance(accessController)
                                }
                            }

                        state.error != null -> ViewerErrorContent(
                            state = state,
                            onOpenWithOtherApp = component::openWithOtherApp,
                        )

                        state.canRender -> key(state.displayName, state.sizeBytes) {
                            DocumentWebView(component = component, nightMode = isNight)
                        }
                    }

                    // 查找条浮在文档上方，不改变 WebView 的高度，
                    // 否则键盘一弹页面重排，阅读位置会跳
                    if (state.search.isOpen) {
                        SearchBar(
                            state = state.search,
                            onQueryChange = component::updateSearchQuery,
                            onPrevious = component::findPrevious,
                            onNext = component::findNext,
                            onClose = component::closeSearch,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(),
                        )
                    }

                    if (state.canShowPages) {
                        PagePill(
                            page = state.page,
                            pageCount = state.pageCount,
                            onClick = { goToPageDialogVisible = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp),
                        )
                    }
                }
            }
        },
    )

    if (goToPageDialogVisible) {
        GoToPageDialog(
            currentPage = state.page,
            pageCount = state.pageCount,
            onDismiss = { goToPageDialogVisible = false },
            onConfirm = { page ->
                component.goToPage(page)
                goToPageDialogVisible = false
            },
        )
    }
}

@Composable
private fun DocumentWebView(component: FileViewerComponent, nightMode: Boolean) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // canRender 为真才会走到这里；真拿不到就退化成一个空 WebView，不在这儿崩
            component.createWebView(context, nightMode) ?: WebView(context)
        },
        onRelease = { webView ->
            // 先断开页面通道，再销毁 View；WebView 不 destroy 的话页面里的 JS 会一直活着
            component.release()
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        },
    )
}

@Composable
private fun SearchBar(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text(text = stringResource(R.string.file_viewer_search_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    keyboardType = KeyboardType.Text,
                ),
                keyboardActions = KeyboardActions(onSearch = { onNext() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = state.countLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.file_viewer_search_previous),
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.file_viewer_search_next),
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        }
    }
}

@Composable
private fun PagePill(
    page: Int,
    pageCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = "$page / $pageCount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoToPageDialog(
    currentPage: Int,
    pageCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var input by remember { mutableStateOf(currentPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.file_viewer_go_to_page)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { value -> input = value.filter(Char::isDigit).take(5) },
                label = { Text(text = stringResource(R.string.file_viewer_page_number)) },
                supportingText = { Text(text = "$currentPage / $pageCount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { input.toIntOrNull()?.let(onConfirm) },
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { input.toIntOrNull()?.let(onConfirm) },
                enabled = input.toIntOrNull() != null,
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

private fun SearchUiState.countLabel(): String = when {
    query.isBlank() -> ""
    !done -> "…"
    else -> "$at/$total"
}

/** 授权引导面板的衬底色：面板文案固定白色，跟主题无关，给一个恒深色底 */
private val GuidanceBackground = Color(0xFF16181C)

@Composable
private fun ViewerErrorContent(
    state: FileViewerUiState,
    onOpenWithOtherApp: () -> Unit,
) {
    val error = state.error ?: return
    val message = when (error) {
        FileViewerError.CannotOpen -> stringResource(R.string.file_viewer_error_cannot_open)
        is FileViewerError.TooLarge -> stringResource(
            R.string.file_viewer_error_too_large,
            Formatter.formatShortFileSize(LocalContext.current, error.sizeBytes),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onOpenWithOtherApp) {
                    Text(text = stringResource(R.string.file_viewer_open_with_other))
                }
            }
        }
    }
}
