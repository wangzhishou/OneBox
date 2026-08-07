package com.shifenmiao.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet

/**
 * 提示词选择底部弹窗（重构版）
 *
 * 数据源从 Category + ChatPromptEntity (item 联表查询) 获取，
 * 替换原来的 Group + Prompt。
 */
@Composable
fun AIPromptsPickerBottomSheet(
    visible: Boolean,
    categories: List<Category>,
    selectedCategoryId: Int,
    prompts: List<PromptEntity>,
    onCategorySelected: (Int) -> Unit,
    onPromptSelected: (PromptEntity) -> Unit,
    onCreatePrompt: () -> Unit,
    onDismiss: (Boolean) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        dragHandle = {
            AiBottomSheetHeader(
                title = stringResource(R.string.ai_duel_choose_prompt),
                onClose = { onDismiss(true) }
            )
        }
    ) {
        var searchQuery by remember { mutableStateOf("") }

        val visiblePrompts by remember(prompts) {
            derivedStateOf {
                prompts.filterNot { it.isSystemPreset() }
            }
        }

        val filteredPrompts by remember(visiblePrompts, searchQuery) {
            derivedStateOf {
                if (searchQuery.isBlank()) visiblePrompts
                else visiblePrompts.filter {
                    (it.title ?: "").contains(searchQuery, ignoreCase = true) ||
                            (it.description ?: "").contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = AiBottomSheetDefaults.ContentMinHeight,
                    max = AiBottomSheetDefaults.ContentMaxHeight
                )
                .navigationBarsPadding()
                .padding(horizontal = AiBottomSheetDefaults.HorizontalPadding)
        ) {
            AiBottomSheetSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.search_prompts_hint)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    AiBottomSheetFilterChip(
                        text = category.name,
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredPrompts.isEmpty()) {
                PromptEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    hasSearchQuery = searchQuery.isNotBlank(),
                    onCreatePrompt = onCreatePrompt,
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        vertical = AppTheme.dimens.paddingSmall
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPrompts, key = { it.id }) { prompt ->
                        ChatPromptGridItem(
                            prompt = prompt,
                            onClick = { onPromptSelected(prompt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptEmptyState(
    modifier: Modifier = Modifier,
    hasSearchQuery: Boolean,
    onCreatePrompt: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    if (hasSearchQuery) {
                        R.string.ai_prompt_picker_empty_search
                    } else {
                        R.string.ai_prompt_picker_empty
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onCreatePrompt) {
                Text(text = stringResource(R.string.ai_prompt_picker_create))
            }
        }
    }
}

@Composable
private fun ChatPromptGridItem(
    prompt: PromptEntity,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // 标题首字符作为 emoji 标签
                val firstChar = prompt.title?.firstOrNull()?.toString()
                if (!firstChar.isNullOrBlank()) {
                    Text(
                        text = firstChar,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 标题
                Text(
                    text = prompt.title ?: "",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 描述
                if (!prompt.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prompt.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 底部：USE 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.use),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
