package com.hrm.markdown.renderer.internal.compose

import androidx.compose.runtime.compositionLocalOf

/** Lists merge their own item text; avoid creating independently focusable fragments inside them. */
internal val LocalMergeInlineSemantics = compositionLocalOf { true }
