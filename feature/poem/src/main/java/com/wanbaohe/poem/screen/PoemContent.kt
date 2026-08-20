package com.wanbaohe.poem.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.model.node.AstNode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoStories
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.poem.R
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.model.parsePinyinLines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 诗词卡片:标题 / 朝代 · 作者 / 逐字拼音田字格 */
@Composable
internal fun PoemCard(
    poem: Poem,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = poem.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
            )
            val dynastyAuthor = listOf(poem.dynasty, poem.author)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            if (dynastyAuthor.isNotBlank()) {
                Text(
                    text = dynastyAuthor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            PoemVerseGrid(poem = poem)
        }
    }
}

/**
 * 逐字拼音网格:每句一行,行内每个字一个 cell(上拼音下汉字),标点附在前一字后。
 * AI 常按「句」(逗号/句号)分行返回拼音,与 API 的行(可能一行两句)不一致,
 * 因此按**全诗总字数**对齐:拼音 token 总数等于全诗汉字数时顺序分配,否则整首回退纯文本(容错)。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoemVerseGrid(poem: Poem) {
    val cellsPerLine = poem.content.map { splitVerseCells(it) }
    val tokens = parsePinyinLines(poem.pinyin).flatten()
    val usePinyin = tokens.isNotEmpty() && tokens.size == cellsPerLine.sumOf { it.size }
    // 视觉行按「句」切分(句号/叹号/问号/分号收尾),一行 API 文本可能含两句
    val segments: List<Pair<List<VerseCell>, List<String>>> = run {
        val result = mutableListOf<Pair<List<VerseCell>, List<String>>>()
        var tokenOffset = 0
        cellsPerLine.forEach { cells ->
            var segmentStart = 0
            cells.forEachIndexed { i, cell ->
                val isLineEnd = i == cells.lastIndex
                if (cell.trailing.any { it in STRONG_PUNCTUATION } || isLineEnd) {
                    val segment = cells.subList(segmentStart, i + 1)
                    val segmentTokens = if (usePinyin) {
                        tokens.subList(tokenOffset, tokenOffset + segment.size)
                    } else {
                        emptyList()
                    }
                    tokenOffset += segment.size
                    result += segment to segmentTokens
                    segmentStart = i + 1
                }
            }
        }
        result
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        segments.forEach { (segment, segmentTokens) ->
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                segment.forEachIndexed { cellIndex, cell ->
                    VerseCellView(
                        cell = cell,
                        pinyin = if (usePinyin) segmentTokens[cellIndex] else null,
                    )
                }
            }
        }
    }
}

/** 诗句拆分:汉字成独立 cell,标点并入前一个字的 cell(不占位) */
private data class VerseCell(val char: String, val trailing: String)

/** 句读级标点:视觉行在这些标点处切分(逗号/句号/叹号/问号/分号,全半角) */
private const val STRONG_PUNCTUATION = "\uFF0C\u3002\uFF01\uFF1F\uFF1B,;!?"

private fun splitVerseCells(line: String): List<VerseCell> {
    val cells = mutableListOf<VerseCell>()
    line.forEach { ch ->
        if (ch.isLetter()) {
            cells += VerseCell(char = ch.toString(), trailing = "")
        } else if (cells.isNotEmpty()) {
            val last = cells.last()
            cells[cells.lastIndex] = last.copy(trailing = last.trailing + ch)
        }
    }
    return cells
}

/** 单字格:上方拼音(可空)+ 固定尺寸田字格 + 格外标点 */
@Composable
private fun VerseCellView(cell: VerseCell, pinyin: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(horizontal = 5.dp),
    ) {
        if (pinyin != null) {
            Text(
                text = pinyin,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            TianZiGeText(text = cell.char)
            if (cell.trailing.isNotEmpty()) {
                Text(
                    text = cell.trailing,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 1.dp, bottom = 4.dp),
                )
            }
        }
    }
}

/** 田字格单字:固定 40dp 方框 + 横竖中线淡色背景,文字居中 */
@Composable
private fun TianZiGeText(text: String) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 1.dp.toPx()
            // 外框
            drawRect(color = gridColor, style = Stroke(width = stroke))
            // 横竖中线
            drawLine(
                color = gridColor,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = stroke,
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = stroke,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * AI 内容区(诗意解读 / 现代翻译共用):
 * 整张卡片包裹标题行(icon + 标题,有内容时右侧「重新生成」图标按钮)
 * 与正文(生成中 / 内容 / 空状态:提示 + 点击生成),配色由调用方给定以区分区块。
 * 注意:不用 GlassCard——玻璃管线对彩色容器的填充 alpha 封顶 0.30 并叠白色高光,
 * 夜间模式下混合底色会偏离 containerColor,破坏 container/onContainer 成对对比度,故用实心 Card。
 */
@Composable
internal fun PoemAiSection(
    title: String,
    icon: ImageVector,
    content: String?,
    isGenerating: Boolean,
    generatingText: String,
    error: String?,
    emptyHint: String,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(start = 8.dp),
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (!content.isNullOrBlank()) {
                        IconButton(
                            onClick = onGenerate,
                            enabled = !isGenerating,
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
                                contentDescription = stringResource(R.string.poem_regenerate),
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            when {
                isGenerating -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(24.dp),
                        )
                        Text(
                            text = generatingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                        )
                    }
                }

                !content.isNullOrBlank() -> {
                    PoemMarkdownText(content = content, color = contentColor)
                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoStories,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                        if (error != null) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }
                        GlassOutlinedButton(onClick = onGenerate) {
                            Text(text = stringResource(R.string.poem_generate_click))
                        }
                    }
                }
            }
        }
    }
}

/** Markdown 渲染(诗意解读/现代翻译):解析挂默认调度器,解析中/失败回落纯文本(绝不留白) */
@Composable
private fun PoemMarkdownText(content: String, color: Color) {
    val context = LocalContext.current
    val parser = remember { MarkdownAstNodeParser(context, CommonMarkdownParseOptions.Default) }
    val ast by produceState<AstNode?>(initialValue = null, parser, content) {
        value = withContext(Dispatchers.Default) {
            runCatching { parser.parse(content) }.getOrNull()
        }
    }
    val node = ast
    if (node != null) {
        RichText(
            contentColor = color,
            textStyle = MaterialTheme.typography.bodyMedium,
        ) {
            BasicMarkdown(astNode = node)
        }
    } else {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 24.sp,
            color = color,
        )
    }
}

/** AI 解读 + 收藏按钮行(历史页详情沿用) */
@Composable
internal fun PoemInsightFavoriteRow(
    poem: Poem,
    isGeneratingInsight: Boolean,
    onGenerateInsight: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassTonalButton(
            onClick = onGenerateInsight,
            enabled = !isGeneratingInsight,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp),
            )
            Text(text = stringResource(R.string.poem_ai_insight))
        }
        GlassTonalButton(
            onClick = onToggleFavorite,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite,
                contentDescription = null,
                tint = if (poem.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(end = 6.dp),
            )
            Text(
                text = stringResource(
                    if (poem.isFavorite) R.string.poem_favorited else R.string.poem_favorite
                )
            )
        }
    }
}

/** AI 解读内容区(历史页详情沿用) */
@Composable
internal fun PoemInsightSection(
    aiInsight: String?,
    isGenerating: Boolean,
    insightError: String?,
    modifier: Modifier = Modifier,
) {
    when {
        isGenerating -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                Text(
                    text = stringResource(R.string.poem_generating_insight),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        insightError != null -> {
            Text(
                text = insightError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }

        !aiInsight.isNullOrBlank() -> {
            GlassCard(modifier = modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.poem_insight_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = aiInsight,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
