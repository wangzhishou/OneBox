package com.wanbaohe.setting.display.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.Navigation
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem

private const val GRID_COLUMNS = 4

/**
 * 启动入口「更多」设置页:从全部可作为启动页的 Screen 中单选一个,
 * 按 Screen.id 持久化(AppSharedStorage.startEntryScreenId),下次冷启动直达。
 */
@Composable
fun StartEntrySettingsScreen(onGoBack: () -> Unit) {
    val selectedScreenId by AppSharedStorage.startEntryScreenId.collectAsState()
    val groups = remember { Navigation.startEntryCandidateGroups() }

    BaseScreen(
        title = {
            Text(
                text = stringResource(R.string.profile_start_entry),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onGoBack = onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding)
        ) {
            item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing)) }

            groups.forEach { (titleRes, screens) ->
                item(key = "header_$titleRes") {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = OneBoxDesignSystem.compactSpacing)
                    )
                }
                screens.chunked(GRID_COLUMNS).forEachIndexed { rowIndex, rowScreens ->
                    item(key = "row_${titleRes}_$rowIndex") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowScreens.forEach { screen ->
                                StartEntryGridCell(
                                    screen = screen,
                                    selected = screen.id == selectedScreenId,
                                    onClick = {
                                        AppSharedStorage.saveStartEntryScreenId(screen.id)
                                        onGoBack()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 不足一行的用空白占位,保持格子等宽
                            repeat(GRID_COLUMNS - rowScreens.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing)) }
            }
        }
    }
}

/**
 * 宫格卡片:图标 + 标题,选中时右上角叠加蓝色对勾角标。
 */
@Composable
private fun StartEntryGridCell(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .glassBackground(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(16.dp),
                    borderWidth = 0.9.dp,
                    style = GlassStyle.Dense
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            screen.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(screen.title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
