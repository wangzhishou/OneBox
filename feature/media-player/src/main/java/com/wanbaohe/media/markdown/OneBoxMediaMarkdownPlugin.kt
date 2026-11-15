package com.wanbaohe.media.markdown

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.wanbaohe.markdown.OneBoxMarkdownBlockRenderer
import com.wanbaohe.markdown.OneBoxMarkdownBlockScope
import com.wanbaohe.markdown.OneBoxMarkdownInputTransformer
import com.wanbaohe.markdown.OneBoxMarkdownPlugin
import com.wanbaohe.media.R
import com.wanbaohe.media.ui.InlineMediaPlayerScope
import com.wanbaohe.media.ui.MarkdownAudioPlayer
import com.wanbaohe.media.ui.MarkdownVideoPlayer

object OneBoxMediaMarkdownPlugin : OneBoxMarkdownPlugin {
    override val id: String = "onebox-media"
    override val inputTransformers: List<OneBoxMarkdownInputTransformer> = listOf(MediaSyntaxTransformer)
    override val blockRenderers: Map<String, OneBoxMarkdownBlockRenderer> = mapOf(
        "audio" to { scope -> MediaDirectiveBlock(scope) },
        "video" to { scope -> MediaDirectiveBlock(scope) },
    )

    @Composable
    override fun WrapDocument(content: @Composable () -> Unit) {
        InlineMediaPlayerScope(content = content)
    }
}

@Composable
private fun MediaDirectiveBlock(scope: OneBoxMarkdownBlockScope) {
    Column {
        val url = scope.directive.args["url"].orEmpty()
        if (url.isSupportedMediaUrl()) {
            when (scope.directive.tagName) {
                "audio" -> MarkdownAudioPlayer(
                    url = url,
                    title = scope.directive.args["title"].orEmpty(),
                )

                "video" -> {
                    val navigator = LocalUrlNavigator.current
                    MarkdownVideoPlayer(
                        url = url,
                        title = scope.directive.args["title"].orEmpty(),
                        posterUrl = scope.directive.args["poster"],
                        onOpenFullScreen = {
                            navigator.navigate("onebox://screen/VideoPlayer?url=${Uri.encode(url)}")
                        },
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.media_inline_invalid_url),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        scope.content?.invoke()
    }
}

private object MediaSyntaxTransformer : OneBoxMarkdownInputTransformer {
    override val id: String = "onebox-media-syntax"

    override fun transform(input: String): String {
        var activeFence: Char? = null
        return input.split('\n').joinToString("\n") { line ->
            val trimmed = line.trim()
            val fence = FenceRegex.find(trimmed)?.value?.firstOrNull()
            if (fence != null) {
                if (activeFence == null) activeFence = fence
                else if (activeFence == fence) activeFence = null
                return@joinToString line
            }
            if (activeFence != null || line.startsWith("    ") || line.startsWith('\t')) {
                return@joinToString line
            }
            val transformedLine = transformMediaLine(trimmed)
            if (transformedLine == trimmed) line else transformedLine
        }
    }
}

private fun transformMediaLine(line: String): String {
    VideoSyntaxRegex.matchEntire(line)?.let { match ->
        val title = match.groupValues[1].escapeDirectiveValue()
        val url = match.groupValues[2].escapeDirectiveValue()
        val poster = match.groupValues[3].takeIf { it.isNotBlank() }
            ?.let { " poster=\"${it.escapeDirectiveValue()}\"" }
            .orEmpty()
        return "{% video title=\"$title\" url=\"$url\"$poster %}\n{% endvideo %}"
    }
    AudioSyntaxRegex.matchEntire(line)?.let { match ->
        val title = match.groupValues[1].escapeDirectiveValue()
        val url = match.groupValues[2].escapeDirectiveValue()
        return "{% audio title=\"$title\" url=\"$url\" %}\n{% endaudio %}"
    }
    return line
}

private fun String.escapeDirectiveValue(): String = buildString {
    this@escapeDirectiveValue.forEach { char ->
        when (char) {
            '\\', '"' -> append('\\').append(char)
            '\n', '\r' -> append(' ')
            else -> append(char)
        }
    }
}

private fun String.isSupportedMediaUrl(): Boolean {
    val scheme = substringBefore(':', "").lowercase()
    return scheme == "https" || scheme == "http"
}

private val FenceRegex = Regex("""^(`{3,}|~{3,})""")
private val VideoSyntaxRegex = Regex(
    """!VIDEO\[([^\]]*)\]\((\S+?)\)(?:\{poster=(\S+?)\})?"""
)
private val AudioSyntaxRegex = Regex("""!AUDIO\[([^\]]*)\]\((\S+?)\)""")
