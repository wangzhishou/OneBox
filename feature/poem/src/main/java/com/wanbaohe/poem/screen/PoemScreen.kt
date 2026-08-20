package com.wanbaohe.poem.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoStories
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePoem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.poem.R
import com.wanbaohe.poem.component.PoemComponent
import com.wanbaohe.poem.component.PoemUiState
import com.wanbaohe.poem.model.Poem

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
                val history = uiState.history
                var showHistory by remember { mutableStateOf(false) }

                // 整页横向翻页:每页 = 诗词卡片 + 操作行 + 诗意解读 + 现代翻译
                PoemPager(
                    poem = poem,
                    history = history,
                    uiState = uiState,
                    onPageSelected = component::selectPoem,
                    onRefresh = component::refresh,
                    onShowHistory = { showHistory = true },
                    onGenerateInsight = component::generateInsight,
                    onGenerateTranslation = component::generateTranslation,
                    onGeneratePinyin = component::generatePinyin,
                )

                PoemHistorySheet(
                    visible = showHistory,
                    history = history,
                    currentPoemId = poem.id,
                    onSelect = { id ->
                        component.selectPoem(id)
                        showHistory = false
                    },
                    onClear = component::clearHistory,
                    onDismiss = { showHistory = false },
                )
            }
        }
    }
}

/**
 * 整页横向翻页:左右滑动切换上一首/下一首(按历史倒序),
 * 诗意解读/现代翻译/拼音状态跟随每页自己的诗词数据。
 * 双向同步:用户滑动翻页 → selectPoem;随机生成/历史点选/deeplink → pager 跟随滚动。
 * 当前诗词不在历史中(理论上不会发生)时退化为单页。
 */
@Composable
private fun PoemPager(
    poem: Poem,
    history: List<Poem>,
    uiState: PoemUiState,
    onPageSelected: (Long) -> Unit,
    onRefresh: () -> Unit,
    onShowHistory: () -> Unit,
    onGenerateInsight: () -> Unit,
    onGenerateTranslation: () -> Unit,
    onGeneratePinyin: () -> Unit,
) {
    val currentIndex = history.indexOfFirst { it.id == poem.id }
    if (currentIndex < 0) {
        PoemDetailPage(
            poem = poem,
            isCurrent = true,
            uiState = uiState,
            onRefresh = onRefresh,
            onShowHistory = onShowHistory,
            onGenerateInsight = onGenerateInsight,
            onGenerateTranslation = onGenerateTranslation,
            onGeneratePinyin = onGeneratePinyin,
        )
        return
    }

    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { history.size },
    )
    val latestHistory by rememberUpdatedState(history)
    val latestPoemId by rememberUpdatedState(poem.id)

    // 外部切换(随机生成/历史点选/deeplink)→ pager 跟随;用户滑动中不打扰
    LaunchedEffect(poem.id) {
        val index = latestHistory.indexOfFirst { it.id == poem.id }
        if (index >= 0 && index != pagerState.currentPage && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(index)
        }
    }
    // 用户滑动翻页停稳 → 切换当前诗词
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val pagePoem = latestHistory.getOrNull(page) ?: return@collect
            if (pagePoem.id != latestPoemId) {
                onPageSelected(pagePoem.id)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        key = { index -> latestHistory.getOrNull(index)?.id ?: -(index + 1L) },
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        latestHistory.getOrNull(page)?.let { pagePoem ->
            PoemDetailPage(
                poem = pagePoem,
                isCurrent = pagePoem.id == latestPoemId,
                uiState = uiState,
                onRefresh = onRefresh,
                onShowHistory = onShowHistory,
                onGenerateInsight = onGenerateInsight,
                onGenerateTranslation = onGenerateTranslation,
                onGeneratePinyin = onGeneratePinyin,
            )
        }
    }
}

/**
 * 单首诗词整页:卡片 + 「随机生成/历史记录/AI 拼音」操作行 + 诗意解读 + 现代翻译 + 拼音状态。
 * 生成中与错误状态只对当前页生效(isCurrent),非当前页展示自己已持久化的内容。
 */
@Composable
private fun PoemDetailPage(
    poem: Poem,
    isCurrent: Boolean,
    uiState: PoemUiState,
    onRefresh: () -> Unit,
    onShowHistory: () -> Unit,
    onGenerateInsight: () -> Unit,
    onGenerateTranslation: () -> Unit,
    onGeneratePinyin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PoemCard(poem = poem)

        // 随机生成 + 历史记录 + AI 拼音:居中弱化一行
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PoemActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
                text = stringResource(R.string.poem_generate_random),
                enabled = isCurrent && !uiState.isLoading,
                onClick = onRefresh,
            )
            PoemActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                text = stringResource(R.string.poem_history_title),
                enabled = true,
                onClick = onShowHistory,
            )
            // AI 拼音:已有拼音置灰;生成中不可点;失败后可手动重试
            val hasPinyin = !poem.pinyin.isNullOrBlank()
            PoemActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver,
                text = stringResource(
                    if (hasPinyin) R.string.poem_pinyin_done else R.string.poem_ai_pinyin
                ),
                enabled = isCurrent && !hasPinyin && !uiState.isGeneratingPinyin,
                onClick = onGeneratePinyin,
            )
        }

        PoemAiSection(
            title = stringResource(R.string.poem_insight_title),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook,
            content = poem.aiInsight,
            isGenerating = isCurrent && uiState.isGeneratingInsight,
            generatingText = stringResource(R.string.poem_generating_insight),
            error = if (isCurrent) uiState.insightError else null,
            emptyHint = stringResource(R.string.poem_insight_empty_hint),
            onGenerate = onGenerateInsight,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )

        PoemAiSection(
            title = stringResource(R.string.poem_translation_title),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoStories,
            content = poem.translation,
            isGenerating = isCurrent && uiState.isGeneratingTranslation,
            generatingText = stringResource(R.string.poem_generating_translation),
            error = if (isCurrent) uiState.translationError else null,
            emptyHint = stringResource(R.string.poem_translation_empty_hint),
            onGenerate = onGenerateTranslation,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        )

        // 拼音生成中:底部灰色状态提示
        if (isCurrent && uiState.isGeneratingPinyin) {
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

/** 操作行小按钮:图标 + 文案,弱化配色,禁用态自动变灰 */
@Composable
private fun PoemActionButton(
    icon: ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.38f),
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 6.dp)
                .size(16.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
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
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePoem,
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
