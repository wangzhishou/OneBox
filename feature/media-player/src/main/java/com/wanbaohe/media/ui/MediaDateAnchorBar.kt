package com.wanbaohe.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.media.R

/**
 * 长列表的日期锚点浮层（视频库 / 音乐库共用）。
 *
 * 收起时是右下角一个不透明的月份胶囊；点开用全 App 共用的带箭头
 * [EnhancedDropdownMenu]（`surface` 底色 + 阴影，不是玻璃透明底）向上展开，
 * 点某个月份让调用方把列表滚到对应锚点。调用方决定何时显示
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

    Box(modifier = modifier) {
        // 这个 Box 同时是胶囊和箭头菜单的锚点：箭头会正对胶囊中心
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
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

            EnhancedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // 胶囊在屏幕底部，菜单向上展开、箭头朝下
                expandUp = true,
                maxWidth = 200.dp,
                maxHeight = 360.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.media_jump_to_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
                anchors.forEachIndexed { index, anchor ->
                    val isActive = index == activeIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
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
