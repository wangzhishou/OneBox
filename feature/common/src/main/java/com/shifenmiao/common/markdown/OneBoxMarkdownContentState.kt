/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.shifenmiao.common.markdown

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wanbaohe.core.ui.utils.helper.scrollToContentStart
import com.wanbaohe.markdown.OneBoxMarkdownState
import com.wanbaohe.markdown.rememberOneBoxMarkdownState

@Stable
class OneBoxMarkdownContentState internal constructor(
    internal val modernState: OneBoxMarkdownState,
    internal val legacyState: LazyListState,
) {
    internal var useLegacy by mutableStateOf(false)

    suspend fun scrollToContentStart() {
        if (useLegacy) legacyState.scrollToContentStart() else modernState.scrollToContentStart()
    }
}

@Composable
fun rememberOneBoxMarkdownContentState(): OneBoxMarkdownContentState {
    val modernState = rememberOneBoxMarkdownState()
    val legacyState = rememberLazyListState()
    return remember(modernState, legacyState) {
        OneBoxMarkdownContentState(modernState, legacyState)
    }
}
