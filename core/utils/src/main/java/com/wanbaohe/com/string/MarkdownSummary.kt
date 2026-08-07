package com.wanbaohe.com.string

/**
 * Simple markdown -> displayable text summary helper.
 *
 * Contract:
 * - Extract a readable title + description from the *beginning* of markdown.
 * - Filters out markdown-only noise and common illegal characters not suitable for titles.
 */
object MarkdownSummary {

    data class Summary(
        val title: String,
        val description: String
    )

    // 匹配 h1-h3 标题的正则
    private val headingRegex = Regex("^(#{1,3})\\s+(.+)$")

    fun derive(
        markdown: String,
        titleMaxChars: Int = 50,
        descriptionMaxChars: Int = 160
    ): Summary {
        val lines = markdown
            .replace("\uFEFF", "") // BOM
            .lineSequence()
            .toList()

        var inFrontMatter = false
        var inCodeFence = false

        fun isFence(line: String): Boolean {
            val t = line.trimStart()
            return t.startsWith("```") || t.startsWith("~~~")
        }

        fun shouldSkipLine(raw: String): Boolean {
            val line = raw.trim()
            if (line.isEmpty()) return true
            if (line == "---" || line == "+++") return true
            if (line == ">") return true
            return false
        }

        // 存储找到的标题（按优先级：h1 > h2 > h3）
        var foundHeading: String? = null
        var foundHeadingLevel = Int.MAX_VALUE

        val meaningful = mutableListOf<String>()
        val firstParagraph = StringBuilder()

        for (raw in lines) {
            val t = raw.trim()

            // toggle front-matter
            if (!inCodeFence && (t == "---" || t == "+++")) {
                inFrontMatter = !inFrontMatter
                continue
            }
            if (inFrontMatter) continue

            // toggle code fence
            if (isFence(raw)) {
                inCodeFence = !inCodeFence
                continue
            }
            if (inCodeFence) continue

            if (shouldSkipLine(raw)) {
                if (firstParagraph.isNotEmpty()) {
                    // stop at first blank line after starting paragraph
                    break
                }
                continue
            }

            // 检查是否是 h1-h3 标题
            val headingMatch = headingRegex.find(t)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length // # 的数量
                val headingText = headingMatch.groupValues[2].trim()
                // 优先使用更高级别的标题（h1 > h2 > h3）
                if (level < foundHeadingLevel && headingText.isNotBlank()) {
                    foundHeading = headingText
                    foundHeadingLevel = level
                }
            }

            val cleaned = cleanMarkdownInline(raw)
            if (cleaned.isBlank()) continue

            meaningful += cleaned

            if (firstParagraph.isNotEmpty()) firstParagraph.append(' ')
            firstParagraph.append(cleaned)

            // collect a bit; derive from beginning only
            if (firstParagraph.length >= descriptionMaxChars * 2) break
        }

        // 标题优先使用 h1/h2/h3，其次使用第一段内容
        val candidateTitle = when {
            !foundHeading.isNullOrBlank() -> foundHeading
            meaningful.isEmpty() -> ""
            else -> meaningful.first()
        }

        val title = sanitizeForTitle(candidateTitle)
            .collapseWhitespace()
            .truncateSafely(titleMaxChars)
            .trim()

        val description = sanitizeForTitle(firstParagraph.toString())
            .collapseWhitespace()
            .truncateSafely(descriptionMaxChars)
            .trim()

        return Summary(title = title, description = description)
    }

    private fun cleanMarkdownInline(input: String): String {
        var s = input.trim()

        // headings / quotes / list markers
        s = s.replace(Regex("^#{1,6}\\s+"), "")
        s = s.replace(Regex("^>\\s*"), "")
        s = s.replace(Regex("^\\s*([-*+]\\s+|\\d+\\.\\s+)"), "")

        // images: ![alt](url) -> alt
        s = s.replace(Regex("!\\[([^]]*)]\\([^)]*\\)"), "$1")
        // links: [text](url) -> text
        s = s.replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")

        // inline code
        s = s.replace("`", "")

        // emphasis markers
        s = s.replace("**", "")
        s = s.replace("*", "")
        s = s.replace("__", "")
        s = s.replace("_", "")
        s = s.replace("~~", "")

        return s
    }

    /**
     * Removes characters that tend to cause UI issues or are illegal in file names.
     * We keep most unicode (including emoji) since Compose can render them,
     * but remove control chars and the common \ / : * ? " < > | set.
     */
    private fun sanitizeForTitle(input: String): String {
        val illegal = Regex("[\\\\/:*?\"<>|]")
        val control = Regex("[\\u0000-\\u001F\\u007F]")

        return input
            .replace(control, " ")
            .replace(illegal, " ")
            .replace(Regex("[\r\n\t]"), " ")
    }

    private fun String.collapseWhitespace(): String =
        trim().replace(Regex("\\s+"), " ")

    private fun String.truncateSafely(maxChars: Int): String {
        if (maxChars <= 0) return ""
        if (length <= maxChars) return this

        // avoid splitting surrogate pairs
        val endExclusive = maxChars.coerceAtMost(length)
        var end = endExclusive
        if (end > 0) {
            val last = this[end - 1]
            if (
                Character.isHighSurrogate(last) &&
                end < length &&
                Character.isLowSurrogate(this[end])
            ) {
                end -= 1
            }
        }

        return substring(0, end).trimEnd() + "…"
    }
}