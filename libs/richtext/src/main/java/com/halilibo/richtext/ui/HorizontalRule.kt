package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A simple horizontal line drawn with the current content color.
 *
 * 分割线只保留 4dp 自身呼吸位：外层容器（聊天里的 block 内边距、长文的 LazyColumn 间距）
 * 已经提供了块间距，这里再叠一整份 paragraphSpacing 会让线上下各出现 24dp 以上的空档。
 */
@Composable public fun RichTextScope.HorizontalRule() {
  val color = currentContentColor.copy(alpha = .2f)
  Box(
    Modifier
      .padding(vertical = 4.dp)
      .fillMaxWidth()
      .height(1.dp)
      .background(color)
  )
}
