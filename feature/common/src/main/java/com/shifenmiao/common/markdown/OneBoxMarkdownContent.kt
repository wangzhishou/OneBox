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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.components.CenterErrorBox
import com.shifenmiao.common.components.text.TextSelectionMenuContainer
import com.shifenmiao.common.components.text.TextSelectionMenuItem
import com.shifenmiao.core.R
import com.wanbaohe.base.ui.MarkdownLazyContent
import com.wanbaohe.base.ui.loading.EmptyBox
import com.wanbaohe.core.ui.loading.DotDanceLoading
import com.wanbaohe.markdown.OneBoxMarkdown
import com.wanbaohe.markdown.requiresLegacyMarkdown
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

@Composable
fun OneBoxMarkdownContent(
    message: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onGoBack: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    emptyText: String = stringResource(R.string.load_empty_toast),
    state: OneBoxMarkdownContentState = rememberOneBoxMarkdownContentState(),
    onLinkClick: ((String) -> Unit)? = null,
    selectionItems: List<TextSelectionMenuItem> = emptyList(),
    codeBlockClickListener: CodeBlockClickListener? = null,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
) {
    when {
        isLoading -> Box(modifier.fillMaxSize(), Alignment.Center) { DotDanceLoading() }
        errorMessage != null -> CenterErrorBox(
            errorMessage = errorMessage,
            onRetry = onRetry,
            onGoBack = onGoBack,
        )

        message.isEmpty() -> Box(modifier.fillMaxSize(), Alignment.Center) {
            EmptyBox(
                modifier = Modifier.fillMaxWidth(),
                text = emptyText,
            )
        }

        else -> {
            val useLegacy = remember(message) { requiresLegacyMarkdown(message) }
            SideEffect { state.useLegacy = useLegacy }
            if (useLegacy) {
                TextSelectionMenuContainer(items = selectionItems, modifier = modifier) {
                    MarkdownLazyContent(
                        message = message,
                        modifier = Modifier.fillMaxSize(),
                        paddingValues = paddingValues,
                        emptyText = emptyText,
                        lazyListState = state.legacyState,
                        codeBlockClickListener = codeBlockClickListener,
                        onLinkClick = onLinkClick,
                        headerContent = {
                            headerContent?.let { header -> item { header() } }
                        },
                        footerContent = {
                            footerContent?.let { footer -> item { footer() } }
                        },
                    )
                }
            } else {
                val components = oneBoxMarkdownComponents(
                    header = headerContent,
                    footer = footerContent,
                    selectionItems = selectionItems,
                    codeBlockClickListener = codeBlockClickListener,
                )
                OneBoxMarkdown(
                    markdown = message,
                    modifier = modifier.padding(paddingValues),
                    state = state.modernState,
                    onLinkClick = onLinkClick,
                    components = components,
                )
            }
        }
    }
}
