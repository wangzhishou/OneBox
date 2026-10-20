package com.wanbaohe.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.wanbaohe.media.R

/**
 * 长列表的右侧日期锚点浮层（视频库 / 音乐库共用）。
 *
 * 收起时是右下角一个显示当前月份的胶囊，点开列出所有月份/日期未知分组；
 * 点某个锚点让调用方把 LazyColumn 滚到对应位置。调用方决定何时显示
 * （例如超过 60 条且至少跨两个月）。
 */
@Composable
internal fun MediaDateAnchorBar(
    anchors: List<MediaDateAnchor>,
    activeIndex: Int,
    onJump: (MediaDateAnchor) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (anchors.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val active = anchors.getOrNull(activeIndex) ?: anchors.first()
    val scrimInteraction = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // 展开时用透明遮罩接住列表点击：点空白处收起，不误触下面的列表
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = scrimInteraction,
                        indication = null,
                    ) { expanded = false },
            )
        }

        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(50),
            style = GlassStyle.Medium,
        ) {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MediaCalendarIcon,
                    contentDescription = stringResource(R.string.media_jump_to_date),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = active.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (expanded) {
            GlassCard(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 68.dp)
                    .width(168.dp)
                    .heightIn(max = 340.dp),
                shape = RoundedCornerShape(18.dp),
                style = GlassStyle.Medium,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.media_jump_to_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                    ) {
                        itemsIndexed(anchors, key = { _, anchor -> anchor.itemIndex }) { index, anchor ->
                            val isActive = index == activeIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isActive) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        expanded = false
                                        onJump(anchor)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = anchor.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isActive) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
