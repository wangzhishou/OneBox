package com.shifenmiao.ai.a2ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.wanbaohe.a2ui.ui.A2uiDynamicContent
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import kotlinx.coroutines.delay

@Composable
internal fun A2uiMarkdownCard(
    source: String,
    renderProvider: A2uiRenderProvider?,
    isStreaming: Boolean,
    onCopy: (String) -> Unit,
    onSubmit: (String) -> Unit,
) {
    var renderReady by remember(source) { mutableStateOf(false) }
    LaunchedEffect(source, isStreaming) {
        renderReady = false
        if (!isStreaming) {
            delay(240)
            renderReady = true
        }
    }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "A2UI", style = MaterialTheme.typography.titleSmall)
                if (!isStreaming && source.isNotEmpty()) {
                    TextButton(onClick = { onCopy(source) }) {
                        Text(stringResource(R.string.copy))
                    }
                }
            }
            when {
                renderProvider == null -> SelectionContainer {
                    Text(text = source, fontFamily = FontFamily.Monospace)
                }

                isStreaming || !renderReady -> A2uiSkeletonPlaceholder(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 128.dp),
                )

                else -> A2uiDynamicContent(
                    json = source,
                    onSubmit = onSubmit,
                    renderProvider = renderProvider,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 128.dp, max = 480.dp),
                )
            }
        }
    }
}
