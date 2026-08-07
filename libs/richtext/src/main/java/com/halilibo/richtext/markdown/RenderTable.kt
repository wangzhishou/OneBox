package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstTableBody
import com.shifenmiao.model.node.AstTableCell
import com.shifenmiao.model.node.AstTableHeader
import com.shifenmiao.model.node.AstTableRow
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Table

@Composable
internal fun RichTextScope.RenderTable(node: com.shifenmiao.model.node.AstNode) {
  Table(
    headerRow = {
      node.filterChildrenType<com.shifenmiao.model.node.AstTableHeader>()
        .firstOrNull()
        ?.filterChildrenType<com.shifenmiao.model.node.AstTableRow>()
        ?.firstOrNull()
        ?.filterChildrenType<com.shifenmiao.model.node.AstTableCell>()
        ?.forEach { tableCell ->
          cell {
            MarkdownRichText(tableCell)
          }
        }
    }
  ) {
    node.filterChildrenType<com.shifenmiao.model.node.AstTableBody>()
      .firstOrNull()
      ?.filterChildrenType<com.shifenmiao.model.node.AstTableRow>()
      ?.forEach { tableRow ->
        row {
          tableRow.filterChildrenType<com.shifenmiao.model.node.AstTableCell>()
            .forEach { tableCell ->
              cell {
                MarkdownRichText(tableCell)
              }
            }
        }
      }
  }
}
