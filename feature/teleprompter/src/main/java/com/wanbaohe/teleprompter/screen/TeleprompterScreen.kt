package com.wanbaohe.teleprompter.screen

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wanbaohe.teleprompter.component.TeleprompterComponent

/**
 * 提词器主页面 — 根据 [TeleprompterComponent.currentPage] 切换子页面
 */
@Composable
fun TeleprompterScreen(
    component: TeleprompterComponent,
) {
    val page by component.currentPage.collectAsState()

    Crossfade(targetState = page, label = "teleprompter_page") { currentPage ->
        when (currentPage) {
            TeleprompterComponent.Page.LIST -> TeleprompterListScreen(component = component)
            TeleprompterComponent.Page.EDITOR -> TeleprompterEditorScreen(component = component)
            TeleprompterComponent.Page.PLAYER -> TeleprompterPlayerScreen(component = component)
        }
    }
}

