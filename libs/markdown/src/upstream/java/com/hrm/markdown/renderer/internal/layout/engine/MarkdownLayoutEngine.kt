package com.hrm.markdown.renderer.internal.layout.engine

import com.hrm.markdown.renderer.internal.core.model.InternalRenderDocumentModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentMetadata

internal interface MarkdownLayoutEngine {
    fun layout(
        document: InternalRenderDocumentModel,
        environment: LayoutEnvironment,
    ): InternalLayoutDocumentModel

    fun layoutBlock(
        block: InternalRenderBlockModel,
        environment: LayoutEnvironment,
    ): InternalLayoutBlockModel

    fun metadata(document: InternalRenderDocumentModel): InternalLayoutDocumentMetadata
}
