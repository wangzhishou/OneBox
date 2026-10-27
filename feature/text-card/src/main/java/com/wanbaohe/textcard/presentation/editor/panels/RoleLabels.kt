package com.wanbaohe.textcard.presentation.editor.panels

import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardRole

fun TextCardRole.labelRes(): Int = when (this) {
    TextCardRole.Title -> R.string.textcard_role_title
    TextCardRole.Body -> R.string.textcard_role_body
    TextCardRole.Author -> R.string.textcard_role_author
    TextCardRole.Source -> R.string.textcard_role_source
}
