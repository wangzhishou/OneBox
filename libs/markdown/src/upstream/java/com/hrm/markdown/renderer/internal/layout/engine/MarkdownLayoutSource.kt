package com.hrm.markdown.renderer.internal.layout.engine

import androidx.compose.ui.unit.LayoutDirection
import com.hrm.markdown.renderer.internal.childBlocks
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.model.CodeBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderDocumentModel
import com.hrm.markdown.renderer.internal.core.model.TableBlockModel
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutEpoch
import com.hrm.markdown.renderer.internal.layout.inline.ReferentialIdentity
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentMetadata
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentModel

/** Block-addressable input for the Compose painter. */
internal interface MarkdownLayoutSource {
    val identity: RenderIdentity
    val blockCount: Int
    val metadata: InternalLayoutDocumentMetadata

    fun stableIdAt(index: Int): Long

    fun blockAt(index: Int): InternalLayoutBlockModel

    fun prefetch(index: Int) {
        if (index in 0 until blockCount) blockAt(index)
    }
}

/** Existing eager layout path used by StaticColumn and compatibility tests. */
internal class EagerMarkdownLayoutSource(
    private val document: InternalLayoutDocumentModel,
) : MarkdownLayoutSource {
    override val identity: RenderIdentity get() = document.identity
    override val blockCount: Int get() = document.blocks.size
    override val metadata: InternalLayoutDocumentMetadata get() = document.metadata

    override fun stableIdAt(index: Int): Long = document.blocks[index].identity.stableId

    override fun blockAt(index: Int): InternalLayoutBlockModel = document.blocks[index]
}

/**
 * Lazy top-level layout session. A block is laid out at most once for this environment and only
 * when LazyColumn composes that item. Recreating the session invalidates the whole block cache.
 */
internal class MarkdownLayoutSession(
    private val document: InternalRenderDocumentModel,
    private val environment: LayoutEnvironment,
    private val engine: MarkdownLayoutEngine,
    private val sharedBlockCache: MarkdownBlockLayoutCache? = null,
) : MarkdownLayoutSource {
    private val blockCache = mutableMapOf<Int, InternalLayoutBlockModel>()

    override val identity: RenderIdentity get() = document.identity
    override val blockCount: Int get() = document.blocks.size
    override val metadata: InternalLayoutDocumentMetadata = engine.metadata(document)

    internal val laidOutBlockCount: Int get() = blockCache.size
    internal var prefetchRequestCount: Int = 0
        private set

    override fun stableIdAt(index: Int): Long = document.blocks[index].identity.stableId

    override fun blockAt(index: Int): InternalLayoutBlockModel {
        require(index in 0 until blockCount) { "Markdown block index out of bounds: $index" }
        return blockCache.getOrPut(index) {
            val block = document.blocks[index]
            sharedBlockCache?.getOrPut(block, environment) {
                engine.layoutBlock(block, environment)
            } ?: engine.layoutBlock(block, environment)
        }
    }

    override fun prefetch(index: Int) {
        if (index !in 0 until blockCount) return
        prefetchRequestCount++
        blockAt(index)
    }
}

/** Bounded cache shared by successive layout sessions owned by one renderer instance. */
internal class MarkdownBlockLayoutCache(
    private val maxEntries: Int = 2_048,
) {
    private data class Key(
        val blockIdentity: ReferentialIdentity,
        val stableId: Long,
        val contentRevision: Long,
        val layoutRevision: Long,
        val paintRevision: Long,
        val viewportWidthBits: Int,
        val blockSpacingBits: Int,
        val layoutDirection: LayoutDirection,
        val epoch: InlineLayoutEpoch,
        val codeBlockHeaderHeightBits: Int,
        val tableToolbarHeightBits: Int,
    )

    private val values = LinkedHashMap<Key, InternalLayoutBlockModel>()

    var hitCount: Long = 0
        private set
    var computationCount: Long = 0
        private set

    fun getOrPut(
        block: InternalRenderBlockModel,
        environment: LayoutEnvironment,
        compute: () -> InternalLayoutBlockModel,
    ): InternalLayoutBlockModel {
        val componentKinds = if (environment.codeBlockHeaderHeightPx == 0f && environment.tableToolbarHeightPx == 0f) 0 else block.componentKinds()
        val key = Key(
            // 不可变 IR 的引用同时覆盖目录、标题编号等全文派生语义，以及含这些语义的容器。
            // 增量拼接保留稳定块的同一引用；下面的版本字段继续承担哈希分布，避免扫描块内容。
            blockIdentity = ReferentialIdentity.of(block),
            stableId = block.identity.stableId,
            contentRevision = block.identity.contentRevision,
            layoutRevision = block.identity.layoutRevision,
            paintRevision = block.identity.paintRevision,
            viewportWidthBits = environment.viewportWidth.toBits(),
            blockSpacingBits = environment.blockSpacing.toBits(),
            layoutDirection = environment.layoutDirection,
            epoch = environment.inlineLayoutEpoch,
            codeBlockHeaderHeightBits = if (componentKinds and 1 != 0) environment.codeBlockHeaderHeightPx.toBits() else 0,
            tableToolbarHeightBits = if (componentKinds and 2 != 0) environment.tableToolbarHeightPx.toBits() else 0,
        )
        values[key]?.let {
            hitCount++
            return it
        }
        val value = compute()
        computationCount++
        values[key] = value
        while (values.size > maxEntries) {
            val eldest = values.keys.firstOrNull() ?: break
            values.remove(eldest)
        }
        return value
    }
}

/** Only affected blocks (or their containers) miss when slot dimensions change. */
private fun InternalRenderBlockModel.componentKinds(): Int = when (this) {
    is CodeBlockModel -> 1
    is TableBlockModel -> 2
    else -> childBlocks().fold(0) { kinds, child -> kinds or child.componentKinds() }
}
