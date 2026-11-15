@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.richtext.editor.document

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.markdown.MarkdownDocumentBlock
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind
import com.wanbaohe.richtext.editor.markdown.RichTextMarkdownDocumentCodec
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
@Suppress("TooManyFunctions")
class MarkdownDocumentEditorState internal constructor(
    initialMarkdown: String,
    private val scheme: RichTextStyleScheme,
    private val scope: CoroutineScope,
    restored: Boolean = false,
) {
    private val codec = RichTextMarkdownDocumentCodec(scheme)
    private val history = MarkdownDocumentHistory(scope)
    private var externalMarkdown = initialMarkdown
    private var skipNextLoad = restored
    private var nextBlockId = 0
    private var decodeJob: Job? = null
    private var skipHistoryRecording = false

    internal val blocks: SnapshotStateList<MarkdownEditorBlock> = mutableStateListOf()
    val sourceState = TextFieldState(initialMarkdown)
    internal var activeBlockId by mutableStateOf<String?>(null)
        private set

    var mode by mutableStateOf(MarkdownEditorMode.Edit)
        private set
    var isDecoding by mutableStateOf(false)
        private set
    var contentVersion by mutableLongStateOf(0L)
        private set
    var userContentVersion by mutableLongStateOf(0L)
        private set

    init {
        decodeMarkdown(initialMarkdown, immediate = true, resetHistory = true)
    }

    fun markdown(): String = when (mode) {
        MarkdownEditorMode.Source -> sourceState.text.toString()
        else -> encodeBlocks()
    }

    suspend fun exportMarkdown(): String {
        val snapshot = withContext(Dispatchers.Main.immediate) {
            if (mode == MarkdownEditorMode.Source) null else captureBlocks(refresh = true)
        }
        return snapshot?.let { blocks ->
            withContext(Dispatchers.Default) { codec.encode(blocks) }
        } ?: withContext(Dispatchers.Main.immediate) { sourceState.text.toString() }
    }

    fun isUserChange(content: String): Boolean = content != externalMarkdown

    fun load(markdown: String) {
        if (skipNextLoad) {
            skipNextLoad = false
            externalMarkdown = markdown
            return
        }
        if (markdown == externalMarkdown) return
        externalMarkdown = markdown
        sourceState.edit {
            replace(0, length, markdown)
            selection = TextRange(markdown.length)
        }
        decodeMarkdown(markdown, immediate = true, resetHistory = true) {
            mode = MarkdownEditorMode.Edit
        }
    }

    fun selectMode(nextMode: MarkdownEditorMode) {
        if (nextMode == mode) return
        history.flush()
        when (nextMode) {
            MarkdownEditorMode.Source -> {
                val markdown = encodeBlocks()
                sourceState.edit {
                    replace(0, length, markdown)
                    selection = TextRange(markdown.length)
                }
                mode = nextMode
            }

            MarkdownEditorMode.Edit -> {
                if (mode == MarkdownEditorMode.Source) {
                    decodeMarkdown(sourceState.text.toString(), resetHistory = true) {
                        mode = nextMode
                    }
                } else {
                    mode = nextMode
                }
            }

            MarkdownEditorMode.Preview -> mode = nextMode
        }
    }

    fun notifyContentChanged(snapshot: RichTextSnapshot? = null) {
        contentVersion++
        if (skipHistoryRecording) return
        userContentVersion++
        history.record(
            if (mode == MarkdownEditorMode.Source) {
                MarkdownEditorSnapshot.Source(snapshot?.text ?: sourceState.text.toString())
            } else {
                MarkdownEditorSnapshot.Blocks(captureBlocks(snapshot))
            },
        )
    }

    fun activate(blockId: String) {
        activeBlockId = blockId
    }

    fun richTextState(): RichTextEditorState? {
        val active = blocks.filterIsInstance<MarkdownEditorBlock.RichText>()
        val activeId = activeBlockId ?: return active.lastOrNull()?.state
        return active.firstOrNull { it.id == activeId }?.state
    }

    fun insertTextBlock() {
        insertBlock(newRichTextBlock(newBlockId(), RichTextSnapshot.Empty))
    }

    fun insertRawBlock(kind: MarkdownRawBlockKind, markdown: String) {
        insertBlock(MarkdownEditorBlock.Raw(newBlockId(), kind, markdown))
    }

    fun removeBlock(blockId: String) {
        blocks.removeAll { it.id == blockId }
        if (blocks.isEmpty()) insertTextBlock()
        activeBlockId = blocks.filterIsInstance<MarkdownEditorBlock.RichText>().lastOrNull()?.id
        notifyContentChanged()
    }

    fun undo() {
        history.undo()?.let(::restoreHistory)
    }

    fun redo() {
        history.redo()?.let(::restoreHistory)
    }

    fun canUndo(): Boolean = history.canUndo

    fun canRedo(): Boolean = history.canRedo

    private fun restoreHistory(snapshot: MarkdownEditorSnapshot) {
        skipHistoryRecording = true
        when (snapshot) {
            is MarkdownEditorSnapshot.Blocks -> {
                mode = MarkdownEditorMode.Edit
                replaceBlocks(snapshot.value)
            }

            is MarkdownEditorSnapshot.Source -> {
                mode = MarkdownEditorMode.Source
                sourceState.edit {
                    replace(0, length, snapshot.markdown)
                    selection = TextRange(snapshot.markdown.length)
                }
                contentVersion++
            }
        }
        skipHistoryRecording = false
        userContentVersion++
    }

    private fun insertBlock(block: MarkdownEditorBlock) {
        val activeIndex = blocks.indexOfFirst { it.id == activeBlockId }
        blocks.add(if (activeIndex >= 0) activeIndex + 1 else blocks.size, block)
        activeBlockId = block.id
        notifyContentChanged()
    }

    private fun newBlockId(): String = "block-new-${nextBlockId++}"

    private fun decodeMarkdown(
        markdown: String,
        immediate: Boolean = false,
        resetHistory: Boolean = false,
        onComplete: () -> Unit = {},
    ) {
        decodeJob?.cancel()
        decodeJob = null
        isDecoding = false
        if (immediate && markdown.length <= SYNCHRONOUS_DECODE_LIMIT) {
            replaceBlocks(codec.decode(markdown).blocks, resetHistory)
            onComplete()
            return
        }
        isDecoding = true
        decodeJob = scope.launch {
            val decoded = withContext(Dispatchers.Default) { codec.decode(markdown) }
            replaceBlocks(decoded.blocks, resetHistory)
            isDecoding = false
            onComplete()
        }
    }

    private fun newRichTextBlock(
        id: String,
        snapshot: RichTextSnapshot,
    ): MarkdownEditorBlock.RichText = MarkdownEditorBlock.RichText(id, snapshot).also { block ->
        block.state.onContentChanged = { changedSnapshot ->
            notifyContentChanged(changedSnapshot)
        }
    }

    private fun replaceBlocks(
        decoded: List<MarkdownDocumentBlock>,
        resetHistory: Boolean = false,
    ) {
        blocks.clear()
        decoded.forEach { block ->
            when (block) {
                is MarkdownDocumentBlock.RichText -> {
                    blocks += newRichTextBlock(block.id, block.snapshot)
                }

                is MarkdownDocumentBlock.Raw -> {
                    blocks += MarkdownEditorBlock.Raw(block.id, block.kind, block.markdown)
                }
            }
        }
        if (blocks.isEmpty()) {
            blocks += newRichTextBlock("rich-text-empty", RichTextSnapshot.Empty)
        }
        activeBlockId = blocks.filterIsInstance<MarkdownEditorBlock.RichText>().lastOrNull()?.id
        nextBlockId = decoded.size
        if (resetHistory) history.reset(MarkdownEditorSnapshot.Blocks(captureBlocks()))
        contentVersion++
    }

    private fun encodeBlocks(): String = codec.encode(captureBlocks(refresh = true))

    private fun captureBlocks(
        changedSnapshot: RichTextSnapshot? = null,
        refresh: Boolean = false,
    ): List<MarkdownDocumentBlock> = blocks.map { block ->
        val isChangedBlock = changedSnapshot != null && block.id == activeBlockId
        when (block) {
            is MarkdownEditorBlock.RichText -> {
                if (isChangedBlock) {
                    block.snapshot = checkNotNull(changedSnapshot)
                } else if (refresh) {
                    block.snapshot = block.state.snapshot()
                }
                MarkdownDocumentBlock.RichText(block.id, block.snapshot)
            }

            is MarkdownEditorBlock.Raw -> {
                if (isChangedBlock) {
                    block.markdown = checkNotNull(changedSnapshot).text
                } else if (refresh) {
                    block.markdown = block.state.text.toString()
                }
                MarkdownDocumentBlock.Raw(block.id, block.markdown, block.kind)
            }
        }
    }
}

sealed interface MarkdownEditorBlock {
    val id: String

    class RichText(
        override val id: String,
        initialSnapshot: RichTextSnapshot,
    ) : MarkdownEditorBlock {
        val state = RichTextEditorState(initialSnapshot)
        internal var snapshot = initialSnapshot
    }

    class Raw(
        override val id: String,
        val kind: MarkdownRawBlockKind,
        initialMarkdown: String,
    ) : MarkdownEditorBlock {
        val state = TextFieldState(initialMarkdown)
        internal var markdown = initialMarkdown
    }
}

enum class MarkdownEditorMode {
    Edit,
    Preview,
    Source,
}

@Composable
fun rememberMarkdownDocumentEditorState(
    initialMarkdown: String,
    scheme: RichTextStyleScheme,
): MarkdownDocumentEditorState {
    val scope = rememberCoroutineScope()
    val saver = remember(scheme, scope) { markdownDocumentEditorStateSaver(scheme, scope) }
    return rememberSaveable(saver = saver) {
        MarkdownDocumentEditorState(initialMarkdown, scheme, scope)
    }
}

private fun markdownDocumentEditorStateSaver(
    scheme: RichTextStyleScheme,
    scope: CoroutineScope,
): Saver<MarkdownDocumentEditorState, String> = object : Saver<MarkdownDocumentEditorState, String> {
    override fun SaverScope.save(value: MarkdownDocumentEditorState): String = value.markdown()

    override fun restore(value: String): MarkdownDocumentEditorState =
        MarkdownDocumentEditorState(value, scheme, scope, restored = true)
}

private const val SYNCHRONOUS_DECODE_LIMIT = 50_000
