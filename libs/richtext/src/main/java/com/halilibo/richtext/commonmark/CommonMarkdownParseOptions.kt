package com.halilibo.richtext.commonmark

/**
 * Allows configuration of the Markdown parser
 *
 * @param autolink Detect plain text links and turn them into Markdown links.
 */
class CommonMarkdownParseOptions(
    val autolink: Boolean,
    val enableCodeHighlight: Boolean = true,
) {

    override fun toString(): String {
        return "CommonMarkdownParseOptions(autolink=$autolink)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommonMarkdownParseOptions) return false

        return autolink == other.autolink
    }

    override fun hashCode(): Int {
        return autolink.hashCode()
    }

    public fun copy(
        autolink: Boolean = this.autolink
    ): CommonMarkdownParseOptions = CommonMarkdownParseOptions(
        autolink = autolink
    )

    public companion object {
        public val Default: CommonMarkdownParseOptions = CommonMarkdownParseOptions(
            autolink = true
        )
    }
}
