package com.wanbaohe.textcard.domain.render

import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRenderState

data class TextCardLayout(
    val pages: List<TextCardRenderState>,
    val longImage: TextCardRenderState?,
    val longImageRequiredHeight: Int? = null,
)

interface TextCardLayoutEngine {
    fun layout(document: TextCardDocument): TextCardLayout
}

sealed class TextCardLayoutException(message: String) : IllegalStateException(message) {
    class LongImageTooLarge(val requiredHeight: Int) : TextCardLayoutException(
        "Long image height $requiredHeight exceeds the supported budget"
    )
}
