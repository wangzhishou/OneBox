package com.shifenmiao.model.node

import android.text.Spanned
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString

/**
 * Refer to https://spec.commonmark.org/0.30/#precedence
 *
 * Common mark specification defines 3 different types of AST nodes;
 *
 * - Container Block
 * - Leaf Block
 * - Inline Content
 *
 * Container blocks are the most generic nodes. They define a structure for their children but
 * do not impose any major restrictions, meaning that container blocks can contain any
 * type of child node.
 *
 * Leaf blocks are self-explanatory, they should not have any children. All the necessary content
 * to render a leaf block should already exist in its payload
 *
 * Inline Content is analogous to [RichTextString] and its styles. Most of the inline content
 * nodes are about styling(bold, italic, strikethrough, code). The rest contains links, images,
 * html content, and of course raw text.
 */
sealed class AstNodeType

//region AstBlockNodeType

sealed class AstBlockNodeType : AstNodeType()

//region AstContainerBlockNodeType
/**
 * Defines a subtype of Block Node that can contain other nodes.
 */
sealed class AstContainerBlockNodeType : AstBlockNodeType()

/**
 * Usually defines the root of a markdown document.
 */
@Immutable
data object AstDocument : AstContainerBlockNodeType()

/**
 * A block quote container that will indent its contents relative to its own indentation.
 */
@Immutable
data object AstBlockQuote : AstContainerBlockNodeType()

/**
 * Ordered or Unordered list item.
 */
@Immutable
data object AstListItem : AstContainerBlockNodeType()

/**
 * A list type that marks its items with bullets to signify a lack of order.
 */
@Immutable
data class AstUnorderedList(
    val bulletMarker: Char
) : AstContainerBlockNodeType()

/**
 * A list type that uses numbers to mark its items.
 */
@Immutable
data class AstOrderedList(
    val startNumber: Int,
    val delimiter: Char
) : AstContainerBlockNodeType()

//endregion

//region AstLeafBlockNodeType

/**
 * Defines a subtype of Block Node that can only contain plain text and full-length annotations.
 */
sealed class AstLeafBlockNodeType : AstBlockNodeType()

@Immutable
data object AstThematicBreak : AstLeafBlockNodeType()

@Immutable
data class AstHeading(
    val level: Int
) : AstLeafBlockNodeType()

@Immutable
data class AstIndentedCodeBlock(
    val literal: String
) : AstLeafBlockNodeType()

@Immutable
data class AstFencedCodeBlock(
    val fenceChar: Char,
    val fenceLength: Int,
    val fenceIndent: Int,
    val info: String,
    val literal: String,
    val annotatedString: AnnotatedString? = null,
) : AstLeafBlockNodeType()

@Immutable
data class AstHtmlBlock(
    val literal: String
) : AstLeafBlockNodeType()

@Immutable
data class AstLinkReferenceDefinition(
    val label: String,
    val destination: String,
    val title: String
) : AstLeafBlockNodeType()

@Immutable
data object AstParagraph : AstLeafBlockNodeType()

@Immutable
data class AstJLatexBlockMath(
    val spanned: Spanned? = null,
    val latex: String? = null
) : AstLeafBlockNodeType()

//endregion

//endregion

//region AstInlineNodeType

/**
 * Defines a node type that can only apply to inline content.
 */
sealed class AstInlineNodeType : AstNodeType()

@Immutable
data class AstCode(
    val literal: String
) : AstInlineNodeType()

@Immutable
data class AstEmphasis(
    private val delimiter: String
) : AstInlineNodeType()

@Immutable
data class AstStrongEmphasis(
    private val delimiter: String
) : AstInlineNodeType()

@Immutable
data class AstStrikethrough(
    val delimiter: String
) : AstInlineNodeType()

@Immutable
data class AstLink(
    val destination: String,
    val title: String
) : AstInlineNodeType()

@Immutable
data class AstImage(
    val title: String,
    val destination: String
) : AstInlineNodeType()

@Immutable
data class AstHtmlInline(
    val literal: String
) : AstInlineNodeType()

@Immutable
data object AstHardLineBreak : AstInlineNodeType()

@Immutable
data object AstSoftLineBreak : AstInlineNodeType()

@Immutable
data class AstText(
    val literal: String
) : AstInlineNodeType()

@Immutable
data class AstJLatexNodeMath(
    val spanned: Spanned? = null,
    val latex: String? = null
) : AstInlineNodeType()

//endregion