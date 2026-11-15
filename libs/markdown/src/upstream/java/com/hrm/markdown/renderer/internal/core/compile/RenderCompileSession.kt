package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.parser.ast.PublicationLineage
import com.hrm.markdown.renderer.internal.core.model.InternalRenderDocumentModel

internal data class RenderCompileStats(
    val visitedNodes: Int,
    val compiledRootBlocks: Int,
    val retainedSlots: Int,
    val coldRebuild: Boolean,
)

/**
 * Compiles trusted parser publications. Each successful update replaces one persistent root range;
 * stable IR and document metadata are shared. A skipped revision, changed global dependency or
 * compile environment starts a new full compilation. No historical AST is retained.
 */
internal class RenderCompileSession : RenderModelCompiler {
    private var lineage: PublicationLineage? = null
    private var revision: Long? = null
    private var environment: RenderCompileEnvironment? = null
    private var previous: InternalRenderDocumentModel? = null

    internal var lastUpdate = RenderCompileStats(0, 0, 0, false)
        private set

    override fun compile(document: Document, environment: RenderCompileEnvironment): InternalRenderDocumentModel {
        val publication = requireNotNull(document.publication) {
            "RenderCompileSession requires a parser publication; build custom syntax in a Flavour before publication"
        }
        require(publication.isCurrent) { "An expired document view cannot be compiled; use the parser's current document" }
        val oldModel = previous
        val sameLineage = lineage === publication.lineageId
        if (sameLineage && revision == publication.revision && this.environment == environment && oldModel != null) {
            lastUpdate = RenderCompileStats(0, 0, (oldModel.blocks as RenderBlockSlots).slotCount, false)
            return oldModel
        }
        val oldSlots = oldModel?.blocks as? RenderBlockSlots
        val canSplice = sameLineage && oldModel != null && oldSlots != null &&
            publication.baseRevision == revision && this.environment == environment &&
            !publication.globalDependenciesChanged &&
            publication.startIndex >= 0 && publication.removedCount >= 0 && publication.insertedCount >= 0 &&
            publication.startIndex <= oldSlots.slotCount - publication.removedCount &&
            oldSlots.slotCount - publication.removedCount + publication.insertedCount == document.children.size

        val model = if (canSplice) {
            val inserted = DefaultRenderModelCompiler.compileInsertedSlots(
                document, publication.startIndex, publication.insertedCount, environment,
            )
            val slots = oldSlots.splice(publication.startIndex, publication.removedCount, inserted.slots)
            lastUpdate = RenderCompileStats(
                inserted.visitedNodes, inserted.slots.count { it != null }, slots.slotCount, false,
            )
            InternalRenderDocumentModel(publishedDocumentIdentity(document), slots, oldModel.metadata)
        } else {
            val full = DefaultRenderModelCompiler.compileFullDocument(document, environment)
            lastUpdate = RenderCompileStats(
                full.visitedNodes, full.model.blocks.size, document.children.size, true,
            )
            full.model
        }
        lineage = publication.lineageId
        revision = publication.revision
        this.environment = environment
        previous = model
        return model
    }
}
