package com.wanbaohe.dsh.screen.node

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.screen.DeliverablesRow
import com.wanbaohe.dsh.session.ChatNode
import com.wanbaohe.dsh.session.FailureTurnFailed
import com.wanbaohe.dsh.session.ImageAttachmentRef
import com.wanbaohe.dsh.session.NoticeMaxTokens
import com.wanbaohe.dsh.session.NoticeSessionInterrupted
import com.wanbaohe.dsh.session.NoticeSessionInterruptedDetail
import com.wanbaohe.dsh.session.NoticeTurnStopped
import com.wanbaohe.dsh.session.ToolStatus
import com.wanbaohe.dsh.session.formatNodeValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * 会话流节点渲染(对齐 Flutter node_widgets.dart):纯展示无业务逻辑。
 *
 * - assistant 文本走 markdown(libs/richtext 的 BasicMarkdown;解析共享单个
 *   [MarkdownAstNodeParser] 实例,挂到默认调度器,不卡组合线程)
 * - think 折叠行**始终默认收起**(直播中也不抢占滚动);展开态 rememberSaveable
 *   按节点 key 挂在 LazyColumn item 上,加载更早历史不张冠李戴
 * - 工具卡:状态色点 + 名称 + 摘要行;点击展开输入/输出详情(等宽横向滚动)
 * - todo / 检查点 / 统计行 / 提示行:轻量分隔样式,与气泡同色系
 */

/** 共享 markdown 解析器(Markwon 构建昂贵,全列表一个实例) */
@Composable
fun rememberMarkdownParser(): MarkdownAstNodeParser {
    val context = LocalContext.current
    return remember { MarkdownAstNodeParser(context, CommonMarkdownParseOptions.Default) }
}

/** 单节点分发(LazyColumn item 内调用,key = node.key 由调用方保证) */
@Composable
fun ChatNodeItem(node: ChatNode, parser: MarkdownAstNodeParser) {
    when (node) {
        is ChatNode.UserMessage -> UserBubble(node)
        is ChatNode.AssistantMessage -> AssistantBubble(node, parser)
        is ChatNode.Think -> ThinkRow(node)
        is ChatNode.Tool -> ToolCard(node)
        is ChatNode.Todo -> TodoDock(node)
        is ChatNode.Compaction -> CompactionRow(node)
        is ChatNode.Stats -> StatsRow(node)
        is ChatNode.Deliverables -> DeliverablesRow(
            paths = node.paths,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        is ChatNode.Retry -> RetryRow(node)
        is ChatNode.Error -> ErrorRow(node)
        is ChatNode.Notice -> NoticeRow(node)
        is ChatNode.Unknown -> UnknownRow(node)
    }
}

// ───────────────────────────── 文本气泡 ─────────────────────────────

/** 用户气泡:右对齐主色,纯文本;图片引用以占位缩略条呈现(防御式,本部署无视觉模型) */
@Composable
private fun UserBubble(node: ChatNode.UserMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 300.dp),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (node.text.isNotEmpty()) {
                        Text(text = node.text, fontSize = 15.sp)
                    }
                    if (node.images.isNotEmpty()) {
                        if (node.text.isNotEmpty()) Spacer(Modifier.height(6.dp))
                        ImageRefRow(node.images)
                    }
                }
            }
        }
    }
}

/** 助手气泡:左对齐中性色,markdown 渲染;streaming 时头部带「生成中」小字 */
@Composable
private fun AssistantBubble(node: ChatNode.AssistantMessage, parser: MarkdownAstNodeParser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (node.streaming) {
                    Text(
                        text = stringResource(R.string.dsh_node_streaming),
                        fontSize = 11.sp,
                        color = AppTheme.colors.getPrimaryColor()
                    )
                    Spacer(Modifier.height(2.dp))
                }
                if (node.text.isNotEmpty()) {
                    MarkdownContent(parser, node.text)
                }
                if (node.images.isNotEmpty()) {
                    if (node.text.isNotEmpty()) Spacer(Modifier.height(6.dp))
                    ImageRefRow(node.images)
                }
            }
        }
    }
}

/** markdown 内容:解析挂默认调度器;解析中/失败回落纯文本(绝不留白) */
@Composable
private fun MarkdownContent(parser: MarkdownAstNodeParser, content: String) {
    val ast by produceState<AstNode?>(initialValue = null, parser, content) {
        value = withContext(Dispatchers.Default) {
            runCatching { parser.parse(content) }.getOrNull()
        }
    }
    val node = ast
    if (node != null) {
        RichText { BasicMarkdown(astNode = node) }
    } else {
        Text(text = content, fontSize = 15.sp)
    }
}

/** 图片引用占位缩略条(无视觉模型环境无法活体采样,按 ImageAttachmentRef fixture 形状呈现) */
@Composable
private fun ImageRefRow(images: List<ImageAttachmentRef>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (image in images) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.dsh_image_attachment),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = image.name?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.dsh_image_default_name),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            R.string.dsh_image_meta,
                            image.width,
                            image.height,
                            Formatter.formatShortFileSize(context, image.bytes)
                        ),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ───────────────────────────── think 折叠 ─────────────────────────────

/** think 折叠行:始终默认收起;streaming 时标题行显示思考正文最后一行(直播中不自动展开) */
@Composable
private fun ThinkRow(node: ChatNode.Think) {
    var expanded by rememberSaveable(node.key) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { expanded = !expanded }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.dsh_node_thinking),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (node.streaming) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.dsh_node_streaming),
                    fontSize = 11.sp,
                    color = AppTheme.colors.getPrimaryColor()
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = stringResource(
                    if (expanded) R.string.dsh_node_collapse else R.string.dsh_node_expand
                ),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // 直播中且未展开:标题下方单行滚动显示最后一行(用户看得到正在生成的内容)
        if (!expanded && node.streaming) {
            val lastLine = node.text.lineSequence().lastOrNull { it.isNotBlank() }.orEmpty()
            if (lastLine.isNotEmpty()) {
                Text(
                    text = lastLine.trim(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (expanded) {
            Text(
                text = node.text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// ───────────────────────────── 工具卡 ─────────────────────────────

/** 工具卡:状态色点 + 名称 + 摘要行;点击展开输入/输出详情(等宽横向滚动) */
@Composable
private fun ToolCard(node: ChatNode.Tool) {
    var expanded by rememberSaveable(node.key) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { expanded = !expanded }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ToolStatusDot(node.status)
            Spacer(Modifier.width(6.dp))
            Text(
                text = node.toolName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = toolStatusText(node.status),
                fontSize = 11.sp,
                color = toolStatusColor(node.status)
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = stringResource(
                    if (expanded) R.string.dsh_node_collapse else R.string.dsh_node_expand
                ),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        node.summary?.takeIf { it.isNotBlank() }?.let { summary ->
            Text(
                text = summary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (expanded) {
            node.input?.let { input ->
                DetailBlock(
                    label = stringResource(R.string.dsh_tool_input),
                    content = formatNodeValue(input)
                )
            }
            node.output?.let { output ->
                DetailBlock(
                    label = stringResource(R.string.dsh_tool_output),
                    content = formatNodeValue(output)
                )
            }
            node.error?.takeIf { it.isNotBlank() }?.let { error ->
                DetailBlock(
                    label = stringResource(R.string.dsh_tool_error),
                    content = error,
                    error = true
                )
            }
            if (node.producedPaths.isNotEmpty()) {
                Text(
                    text = node.producedPaths.joinToString("\n"),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolStatusDot(status: ToolStatus) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(toolStatusColor(status))
    )
}

@Composable
private fun toolStatusColor(status: ToolStatus): androidx.compose.ui.graphics.Color =
    when (status) {
        ToolStatus.Running -> AppTheme.colors.getPrimaryColor()
        ToolStatus.Success -> AppTheme.colors.getPrimaryColor()
        ToolStatus.Failed -> MaterialTheme.colorScheme.error
        ToolStatus.Interrupted -> MaterialTheme.colorScheme.tertiary
    }

@Composable
private fun toolStatusText(status: ToolStatus): String = stringResource(
    when (status) {
        ToolStatus.Running -> R.string.dsh_tool_running
        ToolStatus.Success -> R.string.dsh_tool_success
        ToolStatus.Failed -> R.string.dsh_tool_failed
        ToolStatus.Interrupted -> R.string.dsh_tool_interrupted
    }
)

/** 工具详情块:标签 + 等宽横向滚动内容(展示截断,巨型输出不卡布局) */
@Composable
private fun DetailBlock(label: String, content: String, error: Boolean = false) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (error) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (content.length > InlineDetailCap) {
                content.take(InlineDetailCap) + "…"
            } else {
                content
            },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (error) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 2.dp)
        )
    }
}

/** 行内展示截断阈值(字符);巨型工具输出全量塞进单个 Text 会同步布局卡帧 */
private const val InlineDetailCap = 4000

// ───────────────────────────── todo / 检查点 / 统计行 ─────────────────────────────

/** todo 计划快照:紧凑状态计数卡(标题 + done/total + 逐项勾选) */
@Composable
private fun TodoDock(node: ChatNode.Todo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.dsh_todo_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.dsh_todo_progress, node.done, node.items.size),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        for (item in node.items) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.done) {
                                AppTheme.colors.getPrimaryColor()
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    color = if (item.done) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 压缩检查点:轻量分隔行(图标化短句,可带摘要/消息数) */
@Composable
private fun CompactionRow(node: ChatNode.Compaction) {
    val label = stringResource(
        when (node.kind) {
            "start" -> R.string.dsh_compaction_start
            "end" -> R.string.dsh_compaction_end
            "summary" -> R.string.dsh_compaction_summary
            "prune" -> R.string.dsh_compaction_prune
            else -> R.string.dsh_compaction_generic
        }
    )
    LightDividerRow {
        Text(
            text = buildString {
                append(label)
                node.messages?.let { append(" · ").append(stringResource(R.string.dsh_compaction_messages, it)) }
                node.summary?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
            },
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 轮末统计行:本轮耗时 · TTFT · tok/s(轻量分隔样式,仅完成轮出现) */
@Composable
private fun StatsRow(node: ChatNode.Stats) {
    val parts = ArrayList<String>(3)
    parts.add(stringResource(R.string.dsh_stats_run, formatDurationMs(node.runMs)))
    node.ttftMs?.let { parts.add(stringResource(R.string.dsh_stats_ttft, formatDurationMs(it))) }
    node.tokensPerSecond?.let {
        parts.add(stringResource(R.string.dsh_stats_tps, String.format(Locale.US, "%.1f", it)))
    }
    LightDividerRow {
        Text(
            text = parts.joinToString(" · "),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 毫秒 → 短时长(1234ms → 1.2s;<1s 保留毫秒) */
private fun formatDurationMs(ms: Double): String =
    if (ms >= 1000) String.format(Locale.US, "%.1fs", ms / 1000.0)
    else String.format(Locale.US, "%dms", ms.toLong())

// ───────────────────────────── 轻量提示行 ─────────────────────────────

/** llm/retry 重试行(内联细行) */
@Composable
private fun RetryRow(node: ChatNode.Retry) {
    val text = buildString {
        append(stringResource(R.string.dsh_node_retry))
        if (node.attempt != null) {
            append(" (")
            append(node.attempt)
            node.maxRetries?.let { append("/").append(it) }
            append(")")
        }
        node.reason?.takeIf { it.isNotBlank() }?.let { append(": ").append(it) }
    }
    LightDividerRow {
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 错误行(turn/error 或轮末 reason=error) */
@Composable
private fun ErrorRow(node: ChatNode.Error) {
    LightDividerRow {
        Text(
            text = if (node.message == FailureTurnFailed) {
                stringResource(R.string.dsh_turn_failed)
            } else {
                node.message
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/** 系统短提示(title 为提取器语义键,这里映射本地化文案) */
@Composable
private fun NoticeRow(node: ChatNode.Notice) {
    val title = when (node.title) {
        NoticeTurnStopped -> stringResource(R.string.dsh_turn_stopped)
        NoticeMaxTokens -> stringResource(R.string.dsh_max_tokens)
        NoticeSessionInterrupted -> stringResource(R.string.dsh_session_interrupted)
        else -> node.title
    }
    val detail = when {
        node.detail == NoticeSessionInterruptedDetail ->
            stringResource(R.string.dsh_session_interrupted_detail)
        node.detail == "user" -> stringResource(R.string.dsh_stop_cause_user)
        node.detail == "parent" -> stringResource(R.string.dsh_stop_cause_parent)
        node.detail == "hook" -> stringResource(R.string.dsh_stop_cause_hook)
        node.detail?.startsWith("hook:") == true ->
            stringResource(R.string.dsh_stop_cause_hook_reason, node.detail.orEmpty().removePrefix("hook:"))
        node.detail == "disposed" -> stringResource(R.string.dsh_stop_cause_disposed)
        else -> node.detail
    }
    LightDividerRow {
        Text(
            text = if (detail != null) "$title · $detail" else title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 未知类型兜底:类型名 + 原始 data 折叠展示(默认收起) */
@Composable
private fun UnknownRow(node: ChatNode.Unknown) {
    var expanded by rememberSaveable(node.key) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { expanded = !expanded }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.dsh_unknown_event, node.type),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = stringResource(
                    if (expanded) R.string.dsh_node_collapse else R.string.dsh_node_expand
                ),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            Text(
                text = formatNodeValue(node.data).let {
                    if (it.length > InlineDetailCap) it.take(InlineDetailCap) + "…" else it
                },
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            )
        }
    }
}

/** 轻量分隔行容器:左右细线 + 中间内容(检查点/统计/提示共用) */
@Composable
private fun LightDividerRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
        Spacer(Modifier.width(8.dp))
        content()
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
    }
}
