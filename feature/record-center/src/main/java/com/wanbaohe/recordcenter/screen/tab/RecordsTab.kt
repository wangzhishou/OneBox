package com.wanbaohe.recordcenter.screen.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.sectionGradient
import com.shifenmiao.common.components.sectionIconColor
import com.shifenmiao.common.components.sectionIconContainerColor
import com.shifenmiao.common.components.sectionThemeForIndex
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.screen.util.formatRecordValueParts
import com.wanbaohe.recordcenter.screen.util.formatRelativeDate

/**
 * 记录 tab:每种记录类型一张彩色宫格卡片(配色循环复用 FeaturedGrid 的 SectionTheme),
 * 展示最新一条记录的大数字,点击进入该类型列表页。
 */
@Composable
fun RecordsTab(
    recordTypes: List<RecordTypeDefinition>,
    latestByType: Map<String, HealthRecordEntity>,
    gridState: LazyGridState,
    onTypeClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            items = recordTypes,
            key = { _, definition -> definition.key },
        ) { index, definition ->
            RecordTypeGridCard(
                themeIndex = index,
                definition = definition,
                latest = latestByType[definition.key],
                onClick = { onTypeClick(definition.key) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RecordTypeGridCard(
    themeIndex: Int,
    definition: RecordTypeDefinition,
    latest: HealthRecordEntity?,
    onClick: () -> Unit,
) {
    val theme = sectionThemeForIndex(themeIndex)
    val containerColor = sectionGradient(theme)
    // 与 FeaturedCard 一致:内容色按混合后的有效底色自动选深/浅,保证任意主题下可读
    val contentColor = tintedGlassContentColor(containerColor)
    val iconBgColor = sectionIconContainerColor(theme)
    val iconTint = sectionIconColor(theme)

    val valueParts = remember(latest) {
        latest?.let { formatRecordValueParts(definition, it.fieldsJson) }
    }
    val dateLabel = latest?.let { formatRelativeDate(it.happenedAt) }

    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(RECORD_CARD_HEIGHT),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 软色圆角图标容器
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = definition.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = stringResource(definition.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (valueParts != null && valueParts.first.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = valueParts.first,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        valueParts.second?.let { unit ->
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1,
                            )
                        }
                    }
                    Text(
                        text = dateLabel.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                    )
                } else {
                    Text(
                        text = "--",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.7f),
                    )
                    Text(
                        text = stringResource(R.string.record_center_no_record),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                    )
                }
            }
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(20.dp),
            )
        }
    }
}

/** 宫格卡片固定高度:图标 + 标题 + 大数字 + 日期标签,保证同行卡片上下对齐 */
private val RECORD_CARD_HEIGHT = 176.dp
