package com.shifenmiao.model.node

import androidx.compose.runtime.Immutable

@Immutable
object AstTableRoot: AstContainerBlockNodeType()

@Immutable
object AstTableBody: AstContainerBlockNodeType()

@Immutable
object AstTableHeader: AstContainerBlockNodeType()

@Immutable
object AstTableRow: AstContainerBlockNodeType()

@Immutable
data class AstTableCell(
  val header: Boolean,
  val alignment: AstTableCellAlignment
) : AstContainerBlockNodeType()

enum class AstTableCellAlignment {
  LEFT,
  CENTER,
  RIGHT
}
