package com.wanbaohe.bookkeeping.screen.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.model.CategoriesGrouped
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * 分类筛选底部弹出表单。
 *
 * 视觉风格：
 * - 4 列色块布局（极致扁平、无 divider、无 border）
 * - 支出 / 入账 / 不计入 三色系区分（secondary / primary / tertiary container）
 * - Section header 轻量化：仅文字 + 上下留白，无背景条
 */
@Composable
internal fun CategoryPickerSheet(
    title: String,
    grouped: CategoriesGrouped,
    showAllOption: Boolean = true,
    onSelectAll: () -> Unit,
    onSelectCategory: (id: String, name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TopAppBarDefaults.TopAppBarExpandedHeight),
                contentAlignment = Alignment.TopEnd,
            ) {
                Row(
                    modifier = Modifier
                        .height(TopAppBarDefaults.TopAppBarExpandedHeight)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close, contentDescription = null)
                }
            }
        },
        onDismiss = { if (!it) onDismiss() },
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (showAllOption) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = stringResource(R.string.bookkeeping_filter_all),
                            )
                        }
                        item {
                            CategoryCell(
                                name = stringResource(R.string.bookkeeping_filter_all),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = onSelectAll,
                            )
                        }
                    }

                    if (grouped.expense.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = stringResource(R.string.bookkeeping_expense),
                            )
                        }
                        items(grouped.expense.size) { idx ->
                            val cat = grouped.expense[idx]
                            CategoryCell(
                                name = cat.name,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) { onSelectCategory(cat.id, cat.name) }
                        }
                    }

                    if (grouped.income.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = stringResource(R.string.bookkeeping_income),
                            )
                        }
                        items(grouped.income.size) { idx ->
                            val cat = grouped.income[idx]
                            CategoryCell(
                                name = cat.name,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) { onSelectCategory(cat.id, cat.name) }
                        }
                    }

                    if (grouped.excluded.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = stringResource(R.string.bookkeeping_excluded),
                            )
                        }
                        items(grouped.excluded.size) { idx ->
                            val cat = grouped.excluded[idx]
                            CategoryCell(
                                name = cat.name,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ) { onSelectCategory(cat.id, cat.name) }
                        }
                    }
                }
            }
        },
    )
}

// ── 内部子组件 ───────────────────────────────────────────────────────────────

/**
 * 极简 section header：仅文字 + 上下留白，无背景条、无 divider。
 */
@Composable
private fun CategorySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
    )
}

/**
 * 色块 chip：纯色块（无 border/无 divider），文字居中。
 */
@Composable
private fun CategoryCell(
    name: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
