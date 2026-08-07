package com.shifenmiao.base.provider

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.database.data_draft.DataDraftHelper

/**
 * Composition local for DataDraftHelper.
 * Used by composable call-sites that need to create drafts before navigating.
 *
 * Placed in core:base because it depends on both core:ui (Compose) and core:database (DataDraftHelper).
 * core:ui cannot directly depend on core:database due to circular dependency constraints.
 */
val LocalDataDraftHelper = compositionLocalOf<DataDraftHelper> {
    error("No DataDraftHelper provided")
}

