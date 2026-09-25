package com.wanbaohe.notification.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.base.utils.DateUtils
import com.shifenmiao.common.components.PageLoader
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.network.model.notification.UserNotification
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMessage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNotifications
import com.wanbaohe.notification.R
import com.wanbaohe.notification.component.NotificationComponent
import java.util.concurrent.TimeUnit

@Composable
fun NotificationScreen(component: NotificationComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(com.shifenmiao.core.R.string.notification_center),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
        actions = {
            if (uiState.items.any { !it.read }) {
                TextButton(onClick = component::markAllRead) {
                    Text(text = stringResource(R.string.notification_mark_all_read))
                }
            }
        },
    ) {
        when {
            !uiState.isLoggedIn -> LoginRequiredContent(onLogin = component::login)
            uiState.isLoading -> PageLoader(modifier = Modifier.fillMaxWidth())
            uiState.isError -> ErrorContent(onRetry = component::refresh)
            else -> NotificationList(component = component)
        }
    }
}

@Composable
private fun NotificationList(component: NotificationComponent) {
    val uiState by component.uiState.collectAsState()
    val listState = rememberLazyListState()

    // 滚动到底部附近时自动追加下一页
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) component.loadMore()
    }

    val pullRefreshLayoutState = rememberPullToRefreshStateOnTime(
        onTimeUpdated = { timeElapsed ->
            DateUtils.convertElapsedTimeIntoText(timeElapsed)
        }
    )

    PullToRefreshLayout(
        modifier = Modifier.fillMaxSize(),
        pullRefreshLayoutState = pullRefreshLayoutState,
        onRefresh = component::refresh,
        isRefreshing = uiState.isRefreshing,
    ) {
        if (uiState.items.isEmpty()) {
            EmptyBox(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.notification_empty),
            )
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    count = uiState.items.size,
                    key = { index -> uiState.items[index].id },
                ) { index ->
                    NotificationItem(
                        item = uiState.items[index],
                        onClick = { component.markRead(uiState.items[index]) },
                    )
                }
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    item: UserNotification,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = typeIcon(item.type),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp),
            tint = if (item.read) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = typeLabel(item.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatNotificationTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (item.read) FontWeight.Normal else FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!item.read) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
        }
    }
}

@Composable
private fun LoginRequiredContent(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LineNotifications,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.notification_login_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogin) {
            Text(text = stringResource(com.shifenmiao.core.R.string.login_button_text))
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.notification_load_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.notification_retry))
        }
    }
}

private fun typeIcon(type: String): ImageVector = when (type) {
    UserNotification.TYPE_COMMENT_REPLY -> Icons.Outlined.LineMessage
    UserNotification.TYPE_FEEDBACK_REPLY -> Icons.Outlined.LineInfo
    else -> Icons.Outlined.LineNotifications
}

@Composable
private fun typeLabel(type: String): String = stringResource(
    when (type) {
        UserNotification.TYPE_COMMENT_REPLY -> R.string.notification_type_comment_reply
        UserNotification.TYPE_FEEDBACK_REPLY -> R.string.notification_type_feedback_reply
        else -> R.string.notification_type_system
    }
)

/** 4 小时内显示相对时间(x 分钟前),更早显示日期 */
private fun formatNotificationTime(createdAtMs: Long): String {
    if (createdAtMs <= 0) return ""
    val elapsed = System.currentTimeMillis() - createdAtMs
    if (elapsed in 0 until TimeUnit.HOURS.toMillis(4)) {
        val relative = DateUtils.convertElapsedTimeIntoText(elapsed)
        if (relative.isNotBlank()) return relative
    }
    return DateUtils.formatDate(createdAtMs)
}
