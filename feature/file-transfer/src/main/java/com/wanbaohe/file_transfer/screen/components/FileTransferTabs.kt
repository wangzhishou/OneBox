package com.wanbaohe.file_transfer.screen.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.file_transfer.R
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSendToMobile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChatBubble

enum class FileTransferTab {
    Transfer,
    Chat
}

@Composable
fun FileTransferTabs(
    onSelectTab: (FileTransferTab) -> Unit = {},
    pagerState: PagerState = PagerState(pageCount = { 2 }),
    unreadCount: Int = 0
) {
    val options = listOf(
        stringResource(R.string.file_transfer_tab_transfer),
        stringResource(R.string.file_transfer_tab_chat)
    )
    val coroutineScope = rememberCoroutineScope()
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                    RoundedCornerShape(AppTheme.dimens.cornerRadiusSmall)
                ),
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                        if (index == 0) {
                            onSelectTab(FileTransferTab.Transfer)
                        } else {
                            onSelectTab(FileTransferTab.Chat)
                        }
                    }
                },
                selected = index == pagerState.currentPage,
                border = SegmentedButtonDefaults.borderStroke(
                    color = Color.Transparent
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    Icon(
                        imageVector = if (index == 0) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSendToMobile
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble
                        },
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(14.dp)
                    )
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (index == pagerState.currentPage) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
        }
    }
}
