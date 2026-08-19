package com.wanbaohe.dsh.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.screen.node.ChatNodeItem
import com.wanbaohe.dsh.screen.node.rememberMarkdownParser
import com.wanbaohe.dsh.session.SubagentCatalogPhase
import com.wanbaohe.dsh.session.SubagentCatalogState
import com.wanbaohe.dsh.session.SubagentStore
import com.wanbaohe.dsh.session.extractNodes
import com.wanbaohe.dsh.wire.model.ActivityRunning
import com.wanbaohe.dsh.wire.model.SubagentListEntry
import com.wanbaohe.dsh.wire.model.SubagentModeContinuable
import com.wanbaohe.dsh.wire.model.entryTitle
import kotlinx.coroutines.launch

/**
 * subagent 目录弹层(对齐 Flutter subagent_catalog.dart,DSH-PROTOCOL §3 subagent 组):
 * - 目录树懒展开:hasChildren 行展开时才拉该 child 的目录(一次一请求)
 * - 行:label + activity 状态点;diagnostic 行可读不可操作
 * - 点击 child → 只读 transcript(分页 history + 更早补页 + mux 实时增量);
 *   续聊(subagent.prompt,仅 parentAvailable 且非运行中暴露);
 *   中断(subagent.interrupt,仅运行中暴露)
 * - 错误一律内联呈现 + 重试,不静默吞
 */

/** 转录页目标:父会话 + 目录行(续聊/中断的寻址对) */
private data class TranscriptTarget(
    val parentSessionId: String,
    val entry: SubagentListEntry.Child
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubagentSheet(
    sessionId: String,
    store: SubagentStore,
    onDismiss: () -> Unit
) {
    var target by remember { mutableStateOf<TranscriptTarget?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        val current = target
        if (current == null) {
            SubagentCatalogView(
                sessionId = sessionId,
                store = store,
                onOpen = { target = it }
            )
        } else {
            SubagentTranscriptView(
                rootSessionId = sessionId,
                target = current,
                store = store,
                onBack = { target = null }
            )
        }
    }
}

// ───────────────────────────── 目录树 ─────────────────────────────

/** 扁平化后的目录行(渲染单元):目录行 / 装载态占位 */
private sealed interface CatalogRow {
    val key: String

    data class Entry(
        val parentId: String,
        val entry: SubagentListEntry,
        val level: Int
    ) : CatalogRow {
        override val key: String
            get() = when (entry) {
                is SubagentListEntry.Child -> "child_${entry.id}"
                is SubagentListEntry.Diagnostic -> "diag_${entry.id}"
            }
    }

    data class Loading(val parentId: String, val level: Int) : CatalogRow {
        override val key: String get() = "loading_$parentId"
    }
}

@Composable
private fun SubagentCatalogView(
    sessionId: String,
    store: SubagentStore,
    onOpen: (TranscriptTarget) -> Unit
) {
    val scope = rememberCoroutineScope()
    val catalogs by store.catalogs.collectAsState()
    val expanded = remember { mutableStateOf(setOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }

    // 打开即拉根目录(缓存命中零往返)
    LaunchedEffect(sessionId) {
        try {
            store.listChildren(sessionId)
        } catch (e: Throwable) {
            error = e.message
        }
    }

    /** 懒展开:展开时才拉该 child 的目录(一次一请求) */
    fun toggle(child: SubagentListEntry.Child) {
        val now = expanded.value
        if (child.id in now) {
            expanded.value = now - child.id
        } else {
            expanded.value = now + child.id
            scope.launch {
                try {
                    store.listChildren(child.id)
                } catch (_: Throwable) {
                    // 错误进目录状态机(error 态行内重试)
                }
            }
        }
    }

    // 拍平:根目录 → (展开行递归)
    fun flatten(parentId: String, level: Int, out: MutableList<CatalogRow>) {
        val catalog = catalogs[parentId]
        if (catalog == null) {
            out.add(CatalogRow.Loading(parentId, level))
            return
        }
        for (entry in catalog.entries) {
            out.add(CatalogRow.Entry(parentId, entry, level))
            if (entry is SubagentListEntry.Child && entry.id in expanded.value) {
                flatten(entry.id, level + 1, out)
            }
        }
    }

    val rows = remember(catalogs, expanded.value) {
        ArrayList<CatalogRow>().also { flatten(sessionId, 0, it) }
    }
    val rootCatalog = catalogs[sessionId]

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dsh_subagent_title),
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = {
                scope.launch { store.invalidateChildren(sessionId) }
            }) {
                Text(stringResource(R.string.dsh_retry_action))
            }
        }
        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        when {
            rootCatalog?.phase == SubagentCatalogPhase.Error -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dsh_subagent_load_failed),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = {
                    scope.launch { store.invalidateChildren(sessionId) }
                }) {
                    Text(stringResource(R.string.dsh_retry_action))
                }
            }

            rows.isEmpty() -> Text(
                text = stringResource(R.string.dsh_subagent_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                items(rows, key = { it.key }) { row ->
                    when (row) {
                        is CatalogRow.Loading -> Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = (16 + row.level * 20).dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        is CatalogRow.Entry -> CatalogEntryRow(
                            row = row,
                            catalog = catalogs[row.parentId],
                            expanded = (row.entry as? SubagentListEntry.Child)?.id
                                ?.let { it in expanded.value } == true,
                            onToggle = ::toggle,
                            onOpen = onOpen
                        )
                    }
                }
            }
        }
    }
}

/** 单个目录行:展开箭头(hasChildren)+ 状态点 + 标题;diagnostic 行只读 */
@Composable
private fun CatalogEntryRow(
    row: CatalogRow.Entry,
    catalog: SubagentCatalogState?,
    expanded: Boolean,
    onToggle: (SubagentListEntry.Child) -> Unit,
    onOpen: (TranscriptTarget) -> Unit
) {
    when (val entry = row.entry) {
        is SubagentListEntry.Diagnostic -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (16 + row.level * 20).dp, top = 10.dp, bottom = 10.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dsh_subagent_diagnostic, entry.id),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        is SubagentListEntry.Child -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen(TranscriptTarget(row.parentId, entry)) }
                .padding(start = (16 + row.level * 20).dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.hasChildren) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },
                    contentDescription = stringResource(
                        if (expanded) R.string.dsh_node_collapse else R.string.dsh_node_expand
                    ),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) { onToggle(entry) },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.size(20.dp))
            }
            Spacer(Modifier.width(6.dp))
            // activity 状态点(行内翻转由 store 维护)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry.activity == ActivityRunning) {
                            AppTheme.colors.getPrimaryColor()
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.entryTitle(),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(
                            stringResource(
                                if (entry.activity == ActivityRunning) {
                                    R.string.dsh_subagent_running
                                } else {
                                    R.string.dsh_subagent_inactive
                                }
                            )
                        )
                        append(" · ")
                        append(entry.mode)
                        if (catalog?.parentAvailable == false) {
                            append(" · ")
                            append(stringResource(R.string.dsh_subagent_parent_unavailable))
                        }
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ───────────────────────────── 只读 transcript ─────────────────────────────

@Composable
private fun SubagentTranscriptView(
    rootSessionId: String,
    target: TranscriptTarget,
    store: SubagentStore,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val transcript = remember(target.entry.id) { store.transcriptFor(target.entry.id) }
    val events by transcript.events.collectAsState()
    val hasOlder by transcript.hasOlder.collectAsState()
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    // 目录行实时状态(activity/parentAvailable 行内翻转)
    val catalogs by store.catalogs.collectAsState()
    val liveEntry = catalogs[target.parentSessionId]?.entries
        ?.filterIsInstance<SubagentListEntry.Child>()
        ?.firstOrNull { it.id == target.entry.id } ?: target.entry
    val parentAvailable = catalogs[target.parentSessionId]?.parentAvailable != false

    // 打开即装载尾页(幂等,seq 去重)
    LaunchedEffect(target.entry.id) {
        try {
            store.readTranscript(
                target.parentSessionId,
                target.entry.id,
                mode = target.entry.mode
            )
        } catch (e: Throwable) {
            error = e.message
        }
    }

    val nodes = remember(events) { extractNodes(events) }
    val parser = rememberMarkdownParser()
    val canContinue = parentAvailable &&
        liveEntry.mode == SubagentModeContinuable &&
        liveEntry.activity != ActivityRunning
    val canInterrupt = parentAvailable && liveEntry.activity == ActivityRunning

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.dsh_subagent_back)
                )
            }
            Text(
                text = liveEntry.entryTitle(),
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 中断:仅运行中可继续子会话暴露
            if (canInterrupt) {
                TextButton(
                    onClick = {
                        if (busy) return@TextButton
                        busy = true
                        scope.launch {
                            try {
                                store.interruptChild(target.parentSessionId, target.entry.id)
                            } catch (e: Throwable) {
                                error = e.message
                            }
                            busy = false
                        }
                    },
                    enabled = !busy
                ) {
                    Text(
                        text = stringResource(R.string.dsh_subagent_interrupt),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            if (hasOlder) {
                item(key = "load_older") {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    store.loadOlderTranscript(
                                        target.parentSessionId,
                                        target.entry.id,
                                        mode = target.entry.mode
                                    )
                                } catch (e: Throwable) {
                                    error = e.message
                                }
                            }
                        }) {
                            Text(stringResource(R.string.dsh_load_older))
                        }
                    }
                }
            }
            if (nodes.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.dsh_subagent_transcript_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(nodes, key = { it.key }) { node ->
                ChatNodeItem(node, parser)
            }
        }
        // 续聊:仅 parentAvailable 且可继续、非运行中的子会话暴露
        if (canContinue) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.dsh_subagent_continue_hint)) },
                    maxLines = 3,
                    enabled = !busy
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isEmpty() || busy) return@IconButton
                        busy = true
                        scope.launch {
                            try {
                                store.promptChild(
                                    target.parentSessionId,
                                    target.entry.id,
                                    text
                                )
                                input = ""
                            } catch (e: Throwable) {
                                error = e.message
                            }
                            busy = false
                        }
                    },
                    enabled = input.isNotBlank() && !busy
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.dsh_send),
                        tint = if (input.isNotBlank() && !busy) {
                            AppTheme.colors.getPrimaryColor()
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
