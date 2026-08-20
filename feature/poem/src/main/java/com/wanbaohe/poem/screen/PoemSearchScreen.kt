package com.wanbaohe.poem.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSearchTextField
import com.wanbaohe.poem.R
import com.wanbaohe.poem.component.PoemSearchComponent
import com.wanbaohe.poem.model.Poem

@Composable
fun PoemSearchScreen(component: PoemSearchComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.poem_search),
        onGoBack = component.onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassSearchTextField(
                value = uiState.query,
                onValueChange = component::onQueryChange,
                placeholder = stringResource(R.string.poem_search_hint),
                onSubmit = component::search,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PoemFilterGroup(
                    label = stringResource(R.string.poem_filter_type),
                    options = uiState.types,
                    selected = uiState.selectedType,
                    onSelected = component::onTypeSelected,
                )
                PoemFilterGroup(
                    label = stringResource(R.string.poem_filter_dynasty),
                    options = uiState.dynasties,
                    selected = uiState.selectedDynasty,
                    onSelected = component::onDynastySelected,
                )
                PoemFilterGroup(
                    label = stringResource(R.string.poem_filter_author),
                    options = PoemSearchComponent.FIXED_AUTHORS,
                    selected = uiState.selectedAuthor,
                    onSelected = component::onAuthorSelected,
                )

                when {
                    uiState.isSearching -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.error != null -> {
                        Text(
                            text = stringResource(R.string.poem_load_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        )
                    }

                    uiState.hasSearched -> {
                        val displayed = uiState.displayedResults
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.poem_search_results),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.poem_result_count,
                                        displayed.size,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (displayed.isEmpty()) {
                            Text(
                                text = stringResource(R.string.poem_search_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                            )
                        } else {
                            displayed.forEach { poem ->
                                PoemResultItem(
                                    poem = poem,
                                    onClick = { component.openPoem(poem) },
                                    onToggleFavorite = { component.toggleFavorite(poem) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 筛选组:label + 固定两行 chip 流,放不下时横向滚动(比 FlowRow 省纵向空间) */
@Composable
private fun PoemFilterGroup(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    // 「全部」打头,与选项一起按奇偶位拆成两行
    val chips = buildList {
        add(
            Triple(
                stringResource(R.string.poem_filter_all),
                selected == null,
                { onSelected(null) },
            )
        )
        options.forEach { option ->
            add(Triple(option, selected == option, { onSelected(option) }))
        }
    }
    val topRow = chips.filterIndexed { index, _ -> index % 2 == 0 }
    val bottomRow = chips.filterIndexed { index, _ -> index % 2 == 1 }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "✦ $label",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    topRow.forEach { (text, isSelected, onClick) ->
                        PoemFilterChipItem(text = text, selected = isSelected, onClick = onClick)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bottomRow.forEach { (text, isSelected, onClick) ->
                        PoemFilterChipItem(text = text, selected = isSelected, onClick = onClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun PoemFilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        glassSelectedContainerColor = MaterialTheme.colorScheme.primary,
    )
}

/** 结果卡片:标题 + 体裁标签 / 作者 | 朝代 / 前两句摘录 / 右侧收藏星标 */
@Composable
private fun PoemResultItem(
    poem: Poem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = poem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (poem.type.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = poem.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Text(
                    text = listOf(poem.author, poem.dynasty)
                        .filter { it.isNotBlank() }
                        .joinToString(" | "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (poem.content.isNotEmpty()) {
                    Text(
                        text = poem.content.take(2).joinToString(""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(
                onClick = onToggleFavorite,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = if (poem.isFavorite) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
                    },
                    contentDescription = stringResource(
                        if (poem.isFavorite) R.string.poem_favorited else R.string.poem_favorite
                    ),
                    tint = if (poem.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
