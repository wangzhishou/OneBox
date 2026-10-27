package com.wanbaohe.textcard.presentation.screenLogic

import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSegment

internal data class CanvasTextEditSession(
    val id: Long,
    val role: TextCardRole,
    val pageIndex: Int,
    val sourceStart: Int,
    val initialCursor: Int,
    val block: TextBlock,
    val previousText: String,
) {
    val sourceEnd: Int get() = sourceStart + block.content.length

    val segment: TextCardSegment
        get() = TextCardSegment(
            block = block,
            role = role,
            sourceStart = sourceStart,
            sourceEnd = sourceEnd
        )
}
