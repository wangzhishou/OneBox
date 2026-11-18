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
    var revealBlockId by mutableStateOf<String?>(null)
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

    fun load(markdown: String, keepHistory: Boolean = false) {
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
        if (keepHistory) {
            reloadKeepingHistory(markdown)
        } else {
            decodeMarkdown(markdown, immediate = true, resetHistory = true) {
                mode = MarkdownEditorMode.Edit
            }
        }
    }

    /**
     * 外部整体换正文（AI 改写/扩写等）也走撤销：先把之前的编辑提交成撤销点，
     * 解码时不重置历史，再把新内容立刻提交，于是替换前的状态留在撤销栈里。
     */
    private fun reloadKeepingHistory(markdown: String) {
        history.flush()
        decodeMarkdown(markdown, immediate = true, resetHistory = false) {
            mode = MarkdownEditorMode.Edit
            notifyContentChanged()
            history.flush()
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

            MarkdownEditorMode.Edit, MarkdownEditorMode.Preview -> {
                if (mode == MarkdownEditorMode.Source) {
                    decodeMarkdown(sourceState.text.toString(), resetHistory = true) {
                        mode = nextMode
                    }
                } else {
                    mode = nextMode
                }
            }
        }
    }

    fun notifyContentChanged(
        snapshot: RichTextSnapshot? = null,
        blockId: String? = null,
    ) {
        contentVersion++
        if (skipHistoryRecording) return
        userContentVersion++
        history.record(
            if (mode == MarkdownEditorMode.Source) {
                MarkdownEditorSnapshot.Source(snapshot?.text ?: sourceState.text.toString())
            } else {
                MarkdownEditorSnapshot.Blocks(
                    captureBlocks(changedBlockId = blockId, changedSnapshot = snapshot),
                )
            },
        )
    }

    fun activate(blockId: String) {
        activeBlockId = blockId
    }

    internal fun consumeRevealBlockId(): String? {
        val id = revealBlockId
        revealBlockId = null
        return id
    }

    val isEmpty: Boolean
        get() = blocks.size == 1 &&
            (blocks.first() as? MarkdownEditorBlock.RichText)?.state?.text?.isEmpty() == true

    fun richTextState(): RichTextEditorState? {
        val active = blocks.filterIsInstance<MarkdownEditorBlock.RichText>()
        val activeId = activeBlockId ?: return active.lastOrNull()?.state
        return active.firstOrNull { it.id == activeId }?.state
    }

    fun insertTextBlock() {
        when (mode) {
            MarkdownEditorMode.Source -> insertSourceBlock("")
            MarkdownEditorMode.Preview -> {
                mode = MarkdownEditorMode.Edit
                insertBlock(newRichTextBlock(newBlockId(), RichTextSnapshot.Empty))
            }
            MarkdownEditorMode.Edit -> insertBlock(newRichTextBlock(newBlockId(), RichTextSnapshot.Empty))
        }
    }

    fun insertRawBlock(kind: MarkdownRawBlockKind, markdown: String) {
        when (mode) {
            MarkdownEditorMode.Source -> insertSourceBlock(markdown)
            MarkdownEditorMode.Preview -> {
                mode = MarkdownEditorMode.Edit
                insertBlock(MarkdownEditorBlock.Raw(newBlockId(), kind, markdown))
            }
            MarkdownEditorMode.Edit -> insertBlock(MarkdownEditorBlock.Raw(newBlockId(), kind, markdown))
        }
    }

    fun insertSourceText(markdown: String) {
        val source = sourceState.text.toString()
        val start = sourceState.selection.min.coerceIn(0, source.length)
        val end = sourceState.selection.max.coerceIn(start, source.length)
        sourceState.edit {
            replace(start, end, markdown)
            selection = TextRange(start + markdown.length)
        }
        notifyContentChanged()
    }

    private fun insertSourceBlock(markdown: String) {
        val source = sourceState.text.toString()
        val start = sourceState.selection.min.coerceIn(0, source.length)
        val end = sourceState.selection.max.coerceIn(start, source.length)
        val before = source.substring(0, start)
        val after = source.substring(end)
        val prefix = when {
            before.isEmpty() || before.endsWith("\n\n") -> ""
            before.endsWith('\n') -> "\n"
            else -> "\n\n"
        }
        val suffix = when {
            after.isEmpty() || after.startsWith("\n\n") -> ""
            after.startsWith('\n') -> "\n"
            else -> "\n\n"
        }
        val insertion = prefix + markdown + suffix
        sourceState.edit {
            replace(start, end, insertion)
            selection = TextRange(start + prefix.length + markdown.length)
        }
        notifyContentChanged()
    }

    fun removeBlock(blockId: String) {
        // 结构变更前先把未提交的打字固化成独立撤销点。否则删块与之前的输入共用
        // 同一个 500ms 防抖窗口，撤销会一步跳回上一次 flush 的旧状态（表现为回退过头）。
        history.flush()
        blocks.removeAll { it.id == blockId }
        if (blocks.isEmpty()) {
            // 不再复用 insertTextBlock()：那边会再 flush 一次、再 record 一次，
            // 同一次删除会多记一个撤销点；补块和记录统一在方法末尾做。
            blocks += newRichTextBlock(newBlockId(), RichTextSnapshot.Empty)
        }
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
        // 与 removeBlock 同理：插入块是结构变更，先固化之前的编辑，使其成为独立撤销点。
        history.flush()
        val activeIndex = blocks.indexOfFirst { it.id == activeBlockId }
        blocks.add(if (activeIndex >= 0) activeIndex + 1 else blocks.size, block)
        activeBlockId = block.id
        revealBlockId = block.id
        notifyContentChanged()
    }

    private fun newBlockId(): String = "$INSERTED_BLOCK_ID_PREFIX${nextBlockId++}"

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
        // 用块自己的 id 上报改动：captureBlocks 里若拿 activeBlockId 猜，插入新块后
        // 焦点可能还留在原块，猜错就会把这次输入记成「内容没变」而丢掉撤销点。
        block.state.onContentChanged = { changedSnapshot ->
            notifyContentChanged(changedSnapshot, block.id)
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
        // 只增不减：撤销/重新解码会把带 "block-new-N" 的旧块原样恢复回来，若按 decoded.size
        // 重置计数器，下一次插入就会生成重名 id（LazyColumn key 冲突，removeBlock 一次删俩）。
        nextBlockId = maxOf(nextBlockId, decoded.highestInsertedBlockIndex() + 1)
        if (resetHistory) history.reset(MarkdownEditorSnapshot.Blocks(captureBlocks()))
        contentVersion++
    }

    private fun encodeBlocks(): String = codec.encode(captureBlocks(refresh = true))

    private fun captureBlocks(
        changedBlockId: String? = null,
        changedSnapshot: RichTextSnapshot? = null,
        refresh: Boolean = false,
    ): List<MarkdownDocumentBlock> {
        // 只知道内容变了、不知道是哪个块时，宁可全量刷新也不能拿缓存冒充最新内容。
        val refreshAll = refresh || (changedSnapshot != null && changedBlockId == null)
        return blocks.map { block ->
            val isChangedBlock = changedSnapshot != null && block.id == changedBlockId
            when (block) {
                is MarkdownEditorBlock.RichText -> {
                    if (isChangedBlock) {
                        block.snapshot = checkNotNull(changedSnapshot)
                    } else if (refreshAll) {
                        block.snapshot = block.state.snapshot()
                    }
                    MarkdownDocumentBlock.RichText(block.id, block.snapshot)
                }

                is MarkdownEditorBlock.Raw -> {
                    // raw block 的 TextFieldState 是唯一事实来源：表格/待办等原生编辑器
                    // 直接改 state 后，历史快照与导出都必须拿到最新文本。
                    block.markdown = block.state.text.toString()
                    MarkdownDocumentBlock.Raw(block.id, block.markdown, block.kind)
                }
            }
        }
    }
}

/** [replaceBlocks] 恢复出来的块里最大的 `block-new-N` 序号；没有就返回 -1。 */
private fun List<MarkdownDocumentBlock>.highestInsertedBlockIndex(): Int = maxOfOrNull { block ->
    block.id.takeIf { it.startsWith(INSERTED_BLOCK_ID_PREFIX) }
        ?.removePrefix(INSERTED_BLOCK_ID_PREFIX)
        ?.toIntOrNull()
        ?: -1
} ?: -1

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

/** 运行时插入的块 id 前缀，与解码出来的内容哈希 id 区分开。 */
private const val INSERTED_BLOCK_ID_PREFIX = "block-new-"
