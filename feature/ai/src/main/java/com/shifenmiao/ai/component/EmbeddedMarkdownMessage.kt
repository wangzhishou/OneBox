package com.shifenmiao.ai.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.TextSelectionMenuContainer
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.markdown.OneBoxMarkdown
import com.wanbaohe.markdown.OneBoxMarkdownComponents
import com.wanbaohe.markdown.OneBoxMarkdownPerformancePolicy
import com.wanbaohe.markdown.OneBoxMarkdownPlugin
import com.wanbaohe.markdown.rememberOneBoxMarkdownState

@Composable
internal fun EmbeddedMarkdownMessage(
    source: String,
    isStreaming: Boolean,
    components: OneBoxMarkdownComponents,
    modifier: Modifier = Modifier,
    plugins: List<OneBoxMarkdownPlugin> = emptyList(),
    textStyle: TextStyle? = null,
    contentColor: Color? = null,
    blockSpacing: Dp? = null,
    onLinkClick: ((String) -> Unit)? = null,
    policy: OneBoxMarkdownPerformancePolicy = OneBoxMarkdownPerformancePolicy.Default,
) {
    if (!policy.useEmbeddedViewport(source)) {
        OneBoxMarkdown(
            markdown = source,
            modifier = modifier,
            isStreaming = isStreaming,
            enableScroll = false,
            textStyle = textStyle,
            contentColor = contentColor,
            blockSpacing = blockSpacing,
            components = components,
            plugins = plugins,
            onLinkClick = onLinkClick,
        )
        return
    }

    val state = rememberOneBoxMarkdownState()
    var showFullContent by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OneBoxMarkdown(
            markdown = source,
            modifier = Modifier.fillMaxWidth(),
            state = state,
            isStreaming = isStreaming,
            enableScroll = false,
            textStyle = textStyle,
            contentColor = contentColor,
            blockSpacing = blockSpacing,
            components = components,
            plugins = plugins,
            onLinkClick = onLinkClick,
            embeddedViewportPolicy = policy,
        )
        TextButton(
            onClick = { showFullContent = true },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.LineExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.expand))
        }
    }

    FullMarkdownContentSheet(
        visible = showFullContent,
        source = source,
        isStreaming = isStreaming,
        components = components,
        plugins = plugins,
        textStyle = textStyle,
        contentColor = contentColor,
        blockSpacing = blockSpacing,
        onLinkClick = onLinkClick,
        onDismiss = { showFullContent = false },
    )
}

@Composable
private fun FullMarkdownContentSheet(
    visible: Boolean,
    source: String,
    isStreaming: Boolean,
    components: OneBoxMarkdownComponents,
    plugins: List<OneBoxMarkdownPlugin>,
    textStyle: TextStyle?,
    contentColor: Color?,
    blockSpacing: Dp?,
    onLinkClick: ((String) -> Unit)?,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.markdown),
    ) {
        TextSelectionMenuContainer(
            items = LocalAiMarkdownSelectionItems.current,
            modifier = Modifier.fillMaxSize(),
        ) {
            OneBoxMarkdown(
                markdown = source,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isStreaming = isStreaming,
                enableScroll = true,
                textStyle = textStyle,
                contentColor = contentColor,
                blockSpacing = blockSpacing,
                components = components,
                plugins = plugins,
                onLinkClick = onLinkClick,
            )
        }
    }
}
