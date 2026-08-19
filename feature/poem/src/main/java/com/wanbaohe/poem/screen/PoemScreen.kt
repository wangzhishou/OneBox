package com.wanbaohe.poem.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoStories
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.poem.R
import com.wanbaohe.poem.component.PoemComponent
import com.wanbaohe.poem.model.Poem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PoemScreen(component: PoemComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.poem_page_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = component::navigateToSearch,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearch,
                    contentDescription = stringResource(R.string.poem_search),
                )
            }
        },
    ) {
        val poem = uiState.poem
        when {
            poem == null && uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            poem == null && uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.poem_load_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    GlassTonalButton(
                        onClick = component::refresh,
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        Text(text = stringResource(R.string.poem_retry))
                    }
                }
            }

            poem == null -> {
                PoemEmptyState(onGenerate = component::refresh)
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PoemCard(poem = poem)

                    // 随机生成一首:居中弱化
                    TextButton(
                        onClick = component::refresh,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.poem_generate_random),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    PoemAiSection(
                        title = stringResource(R.string.poem_insight_title),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook,
                        content = poem.aiInsight,
                        isGenerating = uiState.isGeneratingInsight,
                        generatingText = stringResource(R.string.poem_generating_insight),
                        error = uiState.insightError,
                        emptyHint = stringResource(R.string.poem_insight_empty_hint),
                        onGenerate = component::generateInsight,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    PoemAiSection(
                        title = stringResource(R.string.poem_translation_title),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoStories,
                        content = poem.translation,
                        isGenerating = uiState.isGeneratingTranslation,
                        generatingText = stringResource(R.string.poem_generating_translation),
                        error = uiState.translationError,
                        emptyHint = stringResource(R.string.poem_translation_empty_hint),
                        onGenerate = component::generateTranslation,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )

                    if (uiState.recentHistory.isNotEmpty()) {
                        PoemHistorySection(
                            history = uiState.recentHistory,
                            onSelect = component::selectPoem,
                            onClear = component::clearHistory,
                        )
                    }

                    // 拼音生成中:底部灰色状态提示
                    if (uiState.isGeneratingPinyin) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.poem_generating_pinyin),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Box(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/** 历史记录区:标题行 + 最近 5 条 + 清空(确认弹窗) */
@Composable
private fun PoemHistorySection(
    history: List<Poem>,
    onSelect: (Long) -> Unit,
    onClear: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.poem_history_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClear,
                        contentDescription = stringResource(R.string.poem_clear_history),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        history.forEach { poem ->
            PoemHistoryRow(
                poem = poem,
                onClick = { onSelect(poem.id) },
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            text = {
                Text(text = stringResource(R.string.poem_clear_history_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClear()
                    },
                ) {
                    Text(text = stringResource(R.string.poem_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(text = stringResource(R.string.poem_cancel))
                }
            },
        )
    }
}

/** 历史行:首句摘录 | 作者 | 朝代 | 日期,点击回填到卡片 */
@Composable
private fun PoemHistoryRow(
    poem: Poem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = poem.content.firstOrNull().orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        )
        Text(
            text = listOf(poem.author, poem.dynasty)
                .filter { it.isNotBlank() }
                .joinToString(" | "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Text(
            text = formatPoemDate(poem.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 「M月d日」(中文)/「MMM d」(其他语言) */
private fun formatPoemDate(timestamp: Long): String {
    val locale = Locale.getDefault()
    val pattern = if (locale.language == Locale.CHINESE.language) "M月d日" else "MMM d"
    return SimpleDateFormat(pattern, locale).format(Date(timestamp))
}

/** 空状态:从未生成过诗词 */
@Composable
private fun PoemEmptyState(onGenerate: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(88.dp),
                )
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 32.dp),
                )
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp, start = 28.dp),
                )
            }
            Text(
                text = stringResource(R.string.poem_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(R.string.poem_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onGenerate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddCircleOutline,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp),
            )
            Text(
                text = stringResource(R.string.poem_generate),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
