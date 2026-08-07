@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.halilibo.richtext.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

/**
 * 绘制富文本，compose-richtext库的入口点
 */
@Composable
fun BasicRichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  with(RichTextScope) {
    RestartListLevel {
      WithStyle(style) {
        val resolvedStyle = currentRichTextStyle.resolveDefaults()
        val blockSpacing = with(LocalDensity.current) {
          resolvedStyle.paragraphSpacing!!.toDp()
        }

        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = spacedBy(blockSpacing)) {
          children()
        }
      }
    }
  }
}
