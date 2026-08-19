package com.wanbaohe.dsh.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.SessionStore
import com.wanbaohe.dsh.session.TrajectoryExtractor
import com.wanbaohe.dsh.session.TrajectoryItem
import com.wanbaohe.dsh.session.TrajectoryRole
import com.wanbaohe.dsh.session.TrajectoryRow
import com.wanbaohe.dsh.session.TrajectoryTurn
import com.wanbaohe.dsh.wire.model.SessionEvent
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * 轨迹视图(对齐 Flutter trajectory_page.dart;DSH-PROTOCOL §9:零新 RPC,
 * 数据 = SessionLog 事件快照,「加载更早」= session.history 分页)。
 *
 * - 轮次分组 ledger:轮头(序号/耗时/行数,点击折叠)+ 行(seq/角色色 type/摘要/耗时)
 * - 行点选 → 底部 sheet 检查器(完整摘要 + 原始 JSON,等宽横向滚动)
 * - 轮外事件(compaction/start|end、无主 turn/end)归 Between-turns 区段
 * 裁剪:web 的搜索过滤/全部折叠/回到尾部 FAB 后置(协议语义不涉及,纯 UI 增强)。
 */

/** 扁平条目:轮次头 / 轮外区段头 / 行 */
private sealed interface TEntry {
    val key: String

    data class TurnHeader(val turn: TrajectoryTurn, val index: Int) : TEntry {
        override val key: String get() = "turn_${turn.startSeq}"
    }

    data class BetweenHeader(val count: Int, val firstSeq: Int) : TEntry {
        override val key: String get() = "between_$firstSeq"
    }

    data class RowItem(val row: TrajectoryRow) : TEntry {
        override val key: String get() = "row_${row.seq}_${row.type}"
    }
}

@Composable
fun TrajectoryPage(
    sessionId: String,
    sessionStore: SessionStore,
    onLoadOlder: () -> Unit,
    onClose: () -> Unit
) {
    val log = remember(sessionId, sessionStore) { sessionStore.logFor(sessionId) }
    val events by log.events.collectAsState()
    val hasOlder by log.hasOlder.collectAsState()
    var collapsed by remember { mutableStateOf(setOf<Int>()) }
    var inspectorRow by remember { mutableStateOf<TrajectoryRow?>(null) }

    // 提取按事件快照身份缓存(折叠等 setState 不重复 O(n) 提取)
    val view = remember(events) { TrajectoryExtractor.extract(events) }
    val turnNumbers = remember(view) {
        view.turns.mapIndexed { index, turn -> turn.startSeq to index + 1 }.toMap()
    }
    val entries = remember(view, collapsed) {
        val out = ArrayList<TEntry>()
        for (item in view.items) {
            when (item) {
                is TrajectoryItem.TurnItem -> {
                    out.add(TEntry.TurnHeader(item.turn, turnNumbers[item.turn.startSeq] ?: 0))
                    if (item.turn.startSeq !in collapsed) {
                        for (row in item.turn.rows) out.add(TEntry.RowItem(row))
                    }
                }

                is TrajectoryItem.BetweenItem -> {
                    out.add(
                        TEntry.BetweenHeader(
                            count = item.between.rows.size,
                            firstSeq = item.between.rows.firstOrNull()?.seq ?: 0
                        )
                    )
                    for (row in item.between.rows) out.add(TEntry.RowItem(row))
                }
            }
        }
        out
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏:返回 + 标题 + 加载更早
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.dsh_trajectory_back)
                    )
                }
                Text(
                    text = stringResource(R.string.dsh_trajectory_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (hasOlder) {
                    TextButton(onClick = onLoadOlder) {
                        Text(stringResource(R.string.dsh_load_older))
                    }
                }
            }
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dsh_trajectory_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(entries.size, key = { entries[it].key }) { i ->
                        when (val entry = entries[i]) {
                            is TEntry.TurnHeader -> TurnHeaderRow(
                                turn = entry.turn,
                                index = entry.index,
                                collapsed = entry.turn.startSeq in collapsed,
                                onToggle = {
                                    collapsed = if (entry.turn.startSeq in collapsed) {
                                        collapsed - entry.turn.startSeq
                                    } else {
                                        collapsed + entry.turn.startSeq
                                    }
                                }
                            )

                            is TEntry.BetweenHeader -> BetweenHeaderRow(entry.count)
                            is TEntry.RowItem -> TrajectoryLedgerRow(
                                row = entry.row,
                                onClick = { inspectorRow = entry.row }
                            )
                        }
                    }
                }
            }
        }
    }

    inspectorRow?.let { row ->
        TrajectoryInspector(row = row, onDismiss = { inspectorRow = null })
    }
}

/** 轮次头:序号 + 耗时 + 行数 + 折叠箭头(进行中轮带标记) */
@Composable
private fun TurnHeaderRow(
    turn: TrajectoryTurn,
    index: Int,
    collapsed: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (collapsed) {
                Icons.Default.KeyboardArrowRight
            } else {
                Icons.Default.KeyboardArrowDown
            },
            contentDescription = stringResource(
                if (collapsed) R.string.dsh_node_expand else R.string.dsh_node_collapse
            ),
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildString {
                append(stringResource(R.string.dsh_trajectory_turn, index))
                turn.durationMs?.let {
                    append(" · ")
                    append(formatTrajectoryMs(it))
                }
                append(" · ")
                append(stringResource(R.string.dsh_trajectory_rows, turn.rows.size))
                if (turn.inProgress) {
                    append(" · ")
                    append(stringResource(R.string.dsh_trajectory_in_progress))
                }
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 轮外区段头 */
@Composable
private fun BetweenHeaderRow(count: Int) {
    Text(
        text = stringResource(R.string.dsh_trajectory_between, count),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

/** ledger 行:角色色 type + 摘要 + 耗时;点击开检查器 */
@Composable
private fun TrajectoryLedgerRow(row: TrajectoryRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.type,
            modifier = Modifier.width(110.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = trajectoryRoleColor(row.role),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = row.summary,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        row.durationMs?.let {
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatTrajectoryMs(it),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 行检查器:完整摘要 + 原始 JSON(等宽横向滚动;截断防巨型文本卡布局) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrajectoryInspector(row: TrajectoryRow, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "${row.type} · seq ${row.seq}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            val full = TrajectoryExtractor.fullSummary(row.event)
            if (full.isNotEmpty()) {
                Text(
                    text = full,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    fontSize = 13.sp
                )
            }
            Text(
                text = stringResource(R.string.dsh_trajectory_raw_json),
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val raw = remember(row.event) {
                val encoded = runCatching {
                    TrajectoryJson.encodeToString(SessionEvent.serializer(), row.event)
                }.getOrDefault(row.event.data.toString())
                if (encoded.length > InspectorJsonCap) {
                    encoded.take(InspectorJsonCap) + "…"
                } else {
                    encoded
                }
            }
            Text(
                text = raw,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 检查器原始 JSON 展示截断阈值(完整内容属导出面,UI 防卡顿) */
private const val InspectorJsonCap = 20_000

private val TrajectoryJson = Json { prettyPrint = true }

/** 角色标记色 */
@Composable
private fun trajectoryRoleColor(role: TrajectoryRole): androidx.compose.ui.graphics.Color =
    when (role) {
        TrajectoryRole.User -> AppTheme.colors.getPrimaryColor()
        TrajectoryRole.Assistant -> MaterialTheme.colorScheme.onSurface
        TrajectoryRole.Tool -> MaterialTheme.colorScheme.tertiary
        TrajectoryRole.Compaction -> MaterialTheme.colorScheme.onSurfaceVariant
        TrajectoryRole.Retry -> MaterialTheme.colorScheme.tertiary
        TrajectoryRole.Error -> MaterialTheme.colorScheme.error
        TrajectoryRole.Other -> MaterialTheme.colorScheme.onSurfaceVariant
    }

/** 毫秒 → 短时长(与轮末统计行同款:≥1s 一位小数秒,<1s 毫秒) */
private fun formatTrajectoryMs(ms: Double): String =
    if (ms >= 1000) String.format(Locale.US, "%.1fs", ms / 1000.0)
    else String.format(Locale.US, "%dms", ms.toLong())
