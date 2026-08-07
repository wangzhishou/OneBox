package com.halilibo.richtext.ui.a2ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.button.CopyCodeActionButton
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import kotlinx.coroutines.delay

/**
 * a2ui 代码块组件 —— 将 ```a2ui 代码块渲染为 A2UI 界面。
 *
 * - 流式输出中：显示骨架占位图（shimmer 动画）
 * - 输出完成后：通过 [LocalA2uiRenderer] 渲染实际界面
 * - 未提供渲染器时：回退到纯代码显示
 *
 * 操作按钮：
 * - 复制：输出完成后可见，避免流式期间复制到未完成/缓存中的 JSON
 */
@Composable
fun A2uiCodeBlock(
    codeBlock: AstFencedCodeBlock,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val a2uiRenderer = LocalA2uiRenderer.current
    val isStreaming = LocalIsMessageStreaming.current
    var renderReady by remember(codeBlock.literal) { mutableStateOf(false) }

    LaunchedEffect(codeBlock.literal, isStreaming) {
        renderReady = false
        if (!isStreaming) {
            // 防抖：流式解析偶发误判为非 streaming 时，半截 JSON 会频繁变化。
            // 只有内容稳定一小段时间后才进入 A2UI 解析，避免 error/loading 反复闪烁。
            delay(A2UI_RENDER_STABLE_DELAY_MS)
            renderReady = true
        }
    }

    val showSkeleton = isStreaming || !renderReady

    // 没有提供渲染器时，回退到纯代码显示
    if (a2uiRenderer == null) {
        A2uiCodeOnlyBlock(codeBlock, codeBlockClickListener)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .glassBackground(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(
                vertical = 8.dp,
                horizontal = 10.dp
            )
    ) {
        // ── 顶部栏：标签 + 操作按钮 ──
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "A2UI",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 右侧：复制按钮。流式期间隐藏，避免持续变化的 literal 触发无意义重组，
            // 也避免复制到未完成或 AST 复用缓存中的 JSON。
            codeBlockClickListener?.takeUnless { showSkeleton }?.let { listener ->
                CopyCodeActionButton {
                    listener.onCopyButtonClicked(code = codeBlock.literal)
                }
            }
        }

        // ── 内容区域 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp)
        ) {
            if (showSkeleton) {
                // 流式输出/内容尚未稳定：保持稳定骨架，不进入 A2UI 解析链路。
                A2uiSkeletonPlaceholder(modifier = Modifier.fillMaxWidth())
            } else {
                // 输出完成：渲染 A2UI。
                a2uiRenderer.RenderA2ui(
                    json = codeBlock.literal,
                    modifier = Modifier.fillMaxWidth(),
                    onSubmit = { formData ->
                        codeBlockClickListener?.onA2uiSubmit(formData)
                    }
                )
            }
        }
    }
}

private const val A2UI_RENDER_STABLE_DELAY_MS = 240L

/**
 * 渲染器未提供时的回退视图：仅显示代码 + 复制按钮
 */
@Composable
private fun A2uiCodeOnlyBlock(
    codeBlock: AstFencedCodeBlock,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .glassBackground(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "A2UI",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            codeBlockClickListener?.let { listener ->
                CopyCodeActionButton {
                    listener.onCopyButtonClicked(code = codeBlock.literal)
                }
            }
        }
        Text(
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            text = AnnotatedString(codeBlock.literal.trim()),
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        )
    }
}
