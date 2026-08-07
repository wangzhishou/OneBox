package com.halilibo.richtext.ui.mermaid

import android.webkit.WebView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.R
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.locator.PrismGrammarLocator
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import io.noties.markwon.prism4j.Prism4jThemeMaterial3
import io.noties.markwon.prism4j.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.utils.MarkdownStringUtils
import io.noties.prism4j.Prism4j
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * Mermaid 代码块组件 —— 带"图表 / 代码"Tab 切换的 Mermaid 渲染器。
 *
 * 图表 Tab：通过 [LocalMermaidRenderer] 提供的渲染器渲染 Mermaid 图表。
 * 代码 Tab：显示带语法高亮的 Mermaid 源码。
 *
 * 操作按钮：
 * - 复制：始终可见，复制 Mermaid 源码。
 * - 保存：图表 Tab 可见，截图并通过 [CodeBlockClickListener.onMermaidSaveClicked] 保存。
 * - 全屏：图表 Tab 可见，截图并通过 [CodeBlockClickListener.onMermaidFullscreenClicked] 打开图片浏览器。
 */
@Composable
fun MermaidCodeBlock(
    codeBlock: AstFencedCodeBlock,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val mermaidRenderer = LocalMermaidRenderer.current

    // 如果没有提供 MermaidRenderer，回退到纯代码显示
    if (mermaidRenderer == null) {
        MermaidCodeOnlyBlock(codeBlock, codeBlockClickListener)
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 图表, 1 = 代码
    var hasUserSwitchedTab by remember { mutableStateOf(false) } // 是否手动切换过 Tab
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 代码高亮
    var highlightedText by remember { mutableStateOf<AnnotatedString?>(null) }
    LaunchedEffect(codeBlock.literal) {
        highlightedText = withContext(Dispatchers.IO) {
            parseAndHighlightMermaidCode(codeBlock.literal)
        }
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
        // ── 顶部栏：Tab 按钮 + 操作按钮 ──
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            // 左侧：Tab 切换按钮
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .glassBackground(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(8.dp)
                    ).padding(2.dp)
            ) {
                MermaidTabButton(
                    text = stringResource(R.string.mermaid_tab_diagram),
                    isSelected = selectedTab == 0,
                    onClick = { hasUserSwitchedTab = true; selectedTab = 0 }
                )
                MermaidTabButton(
                    text = stringResource(R.string.mermaid_tab_code),
                    isSelected = selectedTab == 1,
                    onClick = { hasUserSwitchedTab = true; selectedTab = 1 }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 右侧：操作按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selectedTab == 0) {
                    // 图表 Tab：保存 + 全屏
                    codeBlockClickListener?.let { listener ->
                        MermaidActionButton(
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 2.dp)
                                        .size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = stringResource(R.string.save),
                            onClick = {
                                // 优先使用 SVG 文件（零转换），回退到 Bitmap
                                val svgFile = mermaidRenderer.getCachedSvgFile(codeBlock.literal)
                                if (svgFile != null) {
                                    listener.onMermaidSaveFile(codeBlock.literal, svgFile)
                                } else {
                                    coroutineScope.launch {
                                        val bitmap = mermaidRenderer.getCachedBitmap(codeBlock.literal)
                                        if (bitmap != null) {
                                            listener.onMermaidSaveClicked(codeBlock.literal, bitmap)
                                        }
                                    }
                                }
                            }
                        )
                        MermaidActionButton(
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 2.dp)
                                        .size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = stringResource(R.string.mermaid_fullscreen),
                            onClick = {
                                // 优先使用 SVG 文件（零转换），回退到 Bitmap
                                val svgFile = mermaidRenderer.getCachedSvgFile(codeBlock.literal)
                                if (svgFile != null) {
                                    listener.onMermaidFullscreenFile(codeBlock.literal, svgFile)
                                } else {
                                    coroutineScope.launch {
                                        val bitmap = mermaidRenderer.getCachedBitmap(codeBlock.literal)
                                        if (bitmap != null) {
                                            listener.onMermaidFullscreenClicked(
                                                codeBlock.literal,
                                                bitmap
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                } else {
                    // 代码 Tab：复制
                    codeBlockClickListener?.let { listener ->
                        MermaidActionButton(
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 2.dp)
                                        .size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = stringResource(R.string.copy),
                            onClick = {
                                listener.onCopyButtonClicked(codeBlock.literal)
                            }
                        )
                    }
                }
            }
        }

        // ── 内容区域 ──
        // 仅在用户手动切换 Tab 时启用 animateContentSize，
        // 避免初次进入/滚动回来时从 minHeight 展开到实际高度的动画
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hasUserSwitchedTab) {
                        Modifier.animateContentSize(animationSpec = tween(300))
                    } else {
                        Modifier
                    }
                )
        ) {
            if (selectedTab == 0) {
                // 图表视图 —— 通过 LocalMermaidRenderer 渲染
                mermaidRenderer.RenderDiagram(
                    code = codeBlock.literal,
                    modifier = Modifier.fillMaxWidth(),
                    onWebViewReady = { wv: WebView ->
                        webViewRef = wv
                    }
                )
            } else {
                // 代码视图
                Text(
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    text = highlightedText ?: AnnotatedString(codeBlock.literal.trim()),
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * MermaidRenderer 未提供时的回退视图：仅显示代码 + 复制按钮
 */
@Composable
private fun MermaidCodeOnlyBlock(
    codeBlock: AstFencedCodeBlock,
    codeBlockClickListener: CodeBlockClickListener?
) {
    val scrollState = rememberScrollState()
    var highlightedText by remember { mutableStateOf<AnnotatedString?>(null) }
    LaunchedEffect(codeBlock.literal) {
        highlightedText = withContext(Dispatchers.IO) {
            parseAndHighlightMermaidCode(codeBlock.literal)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
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
                text = "MERMAID",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            codeBlockClickListener?.let { listener ->
                MermaidActionButton(
                    icon = {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    text = stringResource(R.string.copy),
                    onClick = { listener.onCopyButtonClicked(codeBlock.literal) }
                )
            }
        }
        Text(
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            text = highlightedText ?: AnnotatedString(codeBlock.literal.trim()),
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  内部 UI 组件
// ══════════════════════════════════════════════════════════════

/**
 * Mermaid Tab 切换按钮
 */
@Composable
private fun MermaidTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(200),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(200),
        label = "tabText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )
    }
}

/**
 * Mermaid 操作按钮（图标 + 文字）
 */
@Composable
internal fun MermaidActionButton(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  语法高亮工具函数
// ══════════════════════════════════════════════════════════════

private suspend fun parseAndHighlightMermaidCode(code: String): AnnotatedString {
    return withContext(Dispatchers.IO) {
        try {
            val prism4j = Prism4j(PrismGrammarLocator())
            val theme = Prism4jThemeMaterial3.create()
            val highlight = Prism4jSyntaxHighlight(prism4j, theme, "java")
            val spanned = highlight.highlight("yaml", code)
            MarkdownStringUtils.spannedToAnnotatedString(
                SpannableBuilder(spanned).spannableStringBuilder()
            )
        } catch (_: Exception) {
            AnnotatedString(code)
        }
    }
}
