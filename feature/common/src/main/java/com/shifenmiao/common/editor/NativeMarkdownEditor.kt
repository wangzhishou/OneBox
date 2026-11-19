@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.di.VoiceRecognizerEntryPoint
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.shifenmiao.common.markdown.rememberInAppLinkHandler
import com.shifenmiao.common.voice.ui.VoiceInputSheet
import com.shifenmiao.common.voice.ui.rememberVoiceInputLauncher
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.markdown.OneBoxMarkdown
import com.wanbaohe.richtext.editor.clipboard.rememberRichTextClipboard
import com.wanbaohe.richtext.editor.command.clearInlineFormatting
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.document.MarkdownBlockEditor
import com.wanbaohe.richtext.editor.document.MarkdownBlockRegistry
import com.wanbaohe.richtext.editor.document.MarkdownDocumentEditor
import com.wanbaohe.richtext.editor.document.MarkdownDocumentEditorConfig
import com.wanbaohe.richtext.editor.document.MarkdownEditorBlock
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import com.wanbaohe.richtext.editor.ui.RichTextEditorBehaviorConfig
import com.wanbaohe.richtext.editor.ui.RichTextEditorVisualConfig
import com.wanbaohe.richtext.editor.ui.RichTextSelectionAction
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbar
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbarConfig
import com.wanbaohe.richtext.editor.ui.rememberRichTextSelectionAnchorTracker
import com.wanbaohe.richtext.editor.ui.richTextInlineSelectionActions
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Composable
fun NativeMarkdownEditor(
    state: NativeMarkdownEditorState,
    modifier: Modifier = Modifier,
    extraActions: List<NativeEditorExtraAction> = emptyList(),
    autoFocus: Boolean = false,
    onContentChanged: () -> Unit = {},
) {
    val scheme = rememberNativeMarkdownStyleScheme()
    val registry = rememberNativeMarkdownBlockRegistry()
    val selectionTracker = rememberRichTextSelectionAnchorTracker()
    val clipboard = rememberRichTextClipboard(scheme)
    var contextMenuVisible by remember { mutableStateOf(false) }
    var contextMenuAnchor by remember { mutableStateOf(Rect.Zero) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var insertKind by remember { mutableStateOf<NativeEditorInsertKind?>(null) }
    var pendingLocalKind by remember { mutableStateOf<NativeEditorInsertKind?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    val voiceRecognizer = remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            VoiceRecognizerEntryPoint::class.java,
        ).voiceRecognizer()
    }

    fun insertMarkdown(kind: NativeEditorInsertKind, url: String, name: String) {
        val markdown = when (kind) {
            NativeEditorInsertKind.Link -> "[${name.ifBlank { url }}]($url)"
            NativeEditorInsertKind.Image -> "![${name.ifBlank { "image" }}]($url)"
            NativeEditorInsertKind.Audio -> "!AUDIO[${name.ifBlank { "audio" }}]($url)"
            NativeEditorInsertKind.Video -> "!VIDEO[${name.ifBlank { "video" }}]($url)"
        }
        if (state.mode == NativeMarkdownEditorMode.Source && kind == NativeEditorInsertKind.Link) {
            state.insertSourceText(markdown)
            return
        }
        state.insertRawBlock(
            kind = when (kind) {
                NativeEditorInsertKind.Link -> MarkdownRawBlockKind.Link
                NativeEditorInsertKind.Image -> MarkdownRawBlockKind.Image
                NativeEditorInsertKind.Audio -> MarkdownRawBlockKind.Audio
                NativeEditorInsertKind.Video -> MarkdownRawBlockKind.Video
            },
            markdown = markdown,
        )
    }

    fun insertVoiceText(recognized: String) {
        val text = recognized.trim()
        if (text.isBlank()) return
        if (state.mode == NativeMarkdownEditorMode.Preview) {
            state.selectMode(NativeMarkdownEditorMode.Edit)
        }
        if (state.mode == NativeMarkdownEditorMode.Source) {
            val buffer = state.sourceState
            val start = buffer.selection.min
            val end = buffer.selection.max
            val source = buffer.text.toString()
            val prefix = if (start > 0 && !source[start - 1].isWhitespace()) " " else ""
            val insertion = prefix + text
            buffer.edit {
                replace(start, end, insertion)
                selection = TextRange(start + insertion.length)
            }
            state.notifyContentChanged()
            return
        }
        val editor = state.richTextState() ?: run {
            state.insertTextBlock()
            state.richTextState()
        } ?: return
        val start = editor.selection.min
        val end = editor.selection.max
        val source = editor.text
        val prefix = if (start > 0 && start <= source.length && !source[start - 1].isWhitespace()) {
            " "
        } else {
            ""
        }
        val insertion = prefix + text
        editor.edit {
            replace(start, end, insertion)
            selection = TextRange(start + insertion.length)
        }
    }

    val startVoiceInput = rememberVoiceInputLauncher(
        onResult = ::insertVoiceText,
        onShowIflytekSheet = { showVoiceSheet = true },
    )

    val localFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val kind = pendingLocalKind
        pendingLocalKind = null
        if (uri == null || kind == null) return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
            val media = EditorMediaStore.import(
                context = context,
                uri = uri,
                fallbackBaseName = kind.fallbackName,
            )
            isImporting = false
            if (media == null) {
                AppToastHost.showToast(
                    context.getString(R.string.native_editor_insert_copy_failed)
                )
                return@launch
            }
            insertMarkdown(
                kind = kind,
                url = EditorMediaStore.markdownUri(media.fileName),
                name = media.displayName.substringBeforeLast('.'),
            )
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.userContentVersion }.drop(1).collect { onContentChanged() }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    // 进入编辑页即写入（新建笔记）：把光标放进正文并拉起输入法，省掉「先点一下占位文字」这一步。
    // 只在 autoFocus 变 true 时触发一次；正文块要等布局完成才有可用的焦点节点，所以按帧重试。
    LaunchedEffect(autoFocus) {
        if (!autoFocus || state.mode != NativeMarkdownEditorMode.Edit) return@LaunchedEffect
        val target = state.richTextState() ?: return@LaunchedEffect
        if (!target.focusRequester.requestFocusWhenReady()) return@LaunchedEffect
        keyboardController?.show()
    }

    Column(modifier = modifier) {
        // 空白笔记里除了那一行占位文字之外全是死区：点一下画布空白处也应该能接着写，
        // 不能逼用户去精准命中 32dp 高的输入框（收起键盘后再想输入时尤其明显）。
        val canTapBlankAreaToFocus = state.mode == NativeMarkdownEditorMode.Edit && state.isEmpty
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (canTapBlankAreaToFocus) {
                        Modifier.pointerInput(state) {
                            detectTapGestures {
                                scope.launch {
                                    val target = state.richTextState() ?: return@launch
                                    if (target.focusRequester.requestFocusWhenReady()) {
                                        keyboardController?.show()
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            SuppressSystemTextSelectionMenu {
                MarkdownDocumentEditor(
                    state = state,
                    scheme = scheme,
                    config = MarkdownDocumentEditorConfig(
                        registry = registry,
                        richTextBehavior = RichTextEditorBehaviorConfig(
                            clipboard = clipboard,
                            onLongPress = { anchor ->
                                contextMenuAnchor = anchor
                                contextMenuVisible = true
                            },
                        ),
                        richTextVisual = RichTextEditorVisualConfig(
                            // 与预览使用同一套正文排版（字号/行高/字体），切模式时正文不跳。
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                            selectionAnchorTracker = selectionTracker,
                            // 只给空段落一个可点高度；非空块至少一行 32dp，不会被额外撑高。
                            minHeight = 32.dp,
                            placeholder = if (state.isEmpty) {
                                {
                                    Text(
                                        text = stringResource(R.string.native_editor_empty_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                null
                            },
                        ),
                    ),
                    previewContent = { markdown ->
                        // 预览态里的链接与 App 其它正文一致：http(s) 进内置浏览器。
                        val onLinkClick = rememberInAppLinkHandler()
                        OneBoxMarkdownContent(
                            message = markdown,
                            modifier = Modifier.fillMaxSize(),
                            // 外层统一 16dp 横向留白；预览自身不再额外缩进，避免切换时左右跳。
                            paddingValues = PaddingValues(0.dp),
                            onLinkClick = onLinkClick,
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        // 与预览完全同一套留白：只留 16dp 横向，顶部不再额外加 8dp。
                        .padding(horizontal = 16.dp),
                )
            }
        }

        if (state.mode == NativeMarkdownEditorMode.Source && state.sourceState.text.isEmpty()) {
            Text(
                text = stringResource(R.string.native_editor_source_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (isImporting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        NativeMarkdownEditorDock(
            state = state,
            extraActions = extraActions,
            onInsert = { insertKind = it },
            onVoiceInput = startVoiceInput,
        )
    }

    VoiceInputSheet(
        visible = showVoiceSheet,
        recognizer = voiceRecognizer,
        onResult = ::insertVoiceText,
        onDismiss = { showVoiceSheet = false },
    )

    NativeEditorInsertSheet(
        kind = insertKind,
        isImporting = isImporting,
        onDismiss = { insertKind = null },
        onInsertUrl = { kind, url, linkText ->
            insertMarkdown(
                kind = kind,
                url = url,
                name = if (kind == NativeEditorInsertKind.Link) linkText else "",
            )
            insertKind = null
        },
        onPickLocalFile = { kind ->
            insertKind = null
            pendingLocalKind = kind
            localFilePicker.launch(kind.pickerMimeTypes)
        },
    )

    NativeMarkdownEditorSelectionMenu(
        state = state,
        scheme = scheme,
        selectionTracker = selectionTracker,
        contextMenuVisible = contextMenuVisible,
        contextMenuAnchor = contextMenuAnchor,
        onDismissContextMenu = { contextMenuVisible = false },
    )
}

@Composable
private fun NativeMarkdownEditorSelectionMenu(
    state: NativeMarkdownEditorState,
    scheme: RichTextStyleScheme,
    selectionTracker: com.wanbaohe.richtext.editor.ui.RichTextSelectionAnchorTracker,
    contextMenuVisible: Boolean,
    contextMenuAnchor: Rect,
    onDismissContextMenu: () -> Unit,
) {
    if (state.mode == NativeMarkdownEditorMode.Preview) return
    val editor = state.richTextState() ?: return
    val hasSelection = !editor.selection.collapsed
    if (!hasSelection && !contextMenuVisible) return
    val anchor = if (hasSelection) selectionTracker.anchorBounds else contextMenuAnchor
    if (anchor == Rect.Zero) return
    val clipboard = rememberRichTextClipboard(scheme)
    val actions = buildList {
        if (state.canUndo()) {
            add(
                RichTextSelectionAction(
                    id = "undo",
                    label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.undo),
                    glyph = "↶",
                    onClick = {
                        onDismissContextMenu()
                        state.undo()
                    },
                )
            )
        }
        if (state.canRedo()) {
            add(
                RichTextSelectionAction(
                    id = "redo",
                    label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.redo),
                    glyph = "↷",
                    onClick = {
                        onDismissContextMenu()
                        state.redo()
                    },
                )
            )
        }
        if (hasSelection) {
            addAll(richTextInlineSelectionActions(editor, scheme))
            add(
                RichTextSelectionAction(
                    id = "copy",
                    label = stringResource(android.R.string.copy),
                    glyph = "⧉",
                    onClick = { clipboard.copySelection(editor) },
                )
            )
            add(
                RichTextSelectionAction(
                    id = "cut",
                    label = stringResource(android.R.string.cut),
                    glyph = "✂",
                    onClick = { clipboard.cutSelection(editor) },
                )
            )
            add(
                RichTextSelectionAction(
                    id = "paste",
                    label = stringResource(android.R.string.paste),
                    glyph = "▣",
                    onClick = { clipboard.paste(editor) },
                )
            )
            add(
                RichTextSelectionAction(
                    id = "clear",
                    label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_clear_formatting),
                    glyph = "⌫",
                    onClick = { editor.clearInlineFormatting(scheme) },
                )
            )
        } else {
            add(
                RichTextSelectionAction(
                    id = "paste",
                    label = stringResource(android.R.string.paste),
                    glyph = "▣",
                    onClick = {
                        onDismissContextMenu()
                        clipboard.paste(editor)
                    },
                )
            )
            add(
                RichTextSelectionAction(
                    id = "select_all",
                    label = stringResource(android.R.string.selectAll),
                    glyph = "全",
                    onClick = {
                        onDismissContextMenu()
                        editor.select(TextRange(0, editor.text.length))
                    },
                )
            )
        }
    }
    RichTextSelectionToolbar(
        anchorBounds = anchor,
        actions = actions,
        config = RichTextSelectionToolbarConfig(
            dismissOnClickOutside = !hasSelection && contextMenuVisible,
            onDismiss = onDismissContextMenu.takeIf { !hasSelection && contextMenuVisible },
        ),
    )
}

@Composable
private fun rememberNativeMarkdownBlockRegistry(): MarkdownBlockRegistry {
    val rawEditor = remember { NativeRawBlockEditor() }
    val editors = remember(rawEditor) {
        MarkdownRawBlockKind.entries.associateWith { kind ->
            when (kind) {
                MarkdownRawBlockKind.Table -> NativeTableBlockEditor
                MarkdownRawBlockKind.TaskList -> NativeTaskListBlockEditor
                else -> rawEditor
            }
        }
    }
    return remember(editors) { MarkdownBlockRegistry(editors) }
}

private class NativeRawBlockEditor : MarkdownBlockEditor {
    @Composable
    override fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (com.wanbaohe.richtext.editor.RichTextSnapshot) -> Unit,
    ) {
        val context = LocalContext.current
        var showPreview by remember(block.id) {
            mutableStateOf(
                block.kind in setOf(
                    MarkdownRawBlockKind.Link,
                    MarkdownRawBlockKind.Image,
                    MarkdownRawBlockKind.Audio,
                    MarkdownRawBlockKind.Video,
                )
            )
        }
        GlassSurface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            style = GlassStyle.Thin,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                NativeRawBlockHeader(
                    block = block,
                    showPreview = showPreview,
                    onTogglePreview = { showPreview = !showPreview },
                    onDelete = onDelete,
                )
                if (showPreview) {
                    val onLinkClick = rememberInAppLinkHandler()
                    OneBoxMarkdown(
                        markdown = EditorMediaStore.resolveForDisplay(
                            context = context,
                            markdown = block.state.text.toString(),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            // 预览内容自身不带内边距：不补就会贴着玻璃卡左右/下边缘画，
                            // 圆角处看起来像正文跑出了块外。留白与源码编辑态保持一致。
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        enableScroll = false,
                        // 局部预览里的链接同样进内置浏览器。
                        onLinkClick = onLinkClick,
                        // 和正文编辑器同一套排版，预览态不会比其它块的正文明显偏大。
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                } else {
                    BasicTextField(
                        state = block.state,
                        inputTransformation = androidx.compose.foundation.text.input.InputTransformation {
                            onContentChanged(
                                toRichTextSnapshot(),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .padding(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    )
                }
            }
        }
    }
}

private val MarkdownRawBlockKind.labelRes: Int
    get() = when (this) {
        MarkdownRawBlockKind.Code -> R.string.native_editor_kind_code
        MarkdownRawBlockKind.Diagram -> R.string.native_editor_kind_diagram
        MarkdownRawBlockKind.Image -> R.string.native_editor_kind_image
        MarkdownRawBlockKind.Video -> R.string.native_editor_kind_video
        MarkdownRawBlockKind.Audio -> R.string.native_editor_kind_audio
        MarkdownRawBlockKind.Link -> R.string.native_editor_insert_link
        MarkdownRawBlockKind.Math -> R.string.native_editor_kind_math
        MarkdownRawBlockKind.Html -> R.string.native_editor_kind_html
        else -> R.string.native_editor_kind_other
    }

@Composable
private fun NativeRawBlockHeader(
    block: MarkdownEditorBlock.Raw,
    showPreview: Boolean,
    onTogglePreview: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(block.kind.labelRes),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onTogglePreview) {
            Icon(
                imageVector = if (showPreview) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = stringResource(R.string.native_editor_show_preview),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.native_editor_delete_block),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * 等输入框的焦点节点挂载好再请求焦点。
 *
 * 组合结束不代表布局完成：刚进入页面时 BasicTextField 的焦点节点还没挂在窗口上，
 * 直接 requestFocus 会抛 not initialized。按帧重试几次，直到真正拿到焦点。
 */
private suspend fun FocusRequester.requestFocusWhenReady(): Boolean {
    repeat(FOCUS_REQUEST_ATTEMPTS) {
        withFrameNanos { }
        if (runCatching { requestFocus() }.isSuccess) return true
    }
    return false
}

private const val FOCUS_REQUEST_ATTEMPTS = 4
