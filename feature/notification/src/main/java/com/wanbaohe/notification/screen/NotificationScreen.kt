package com.wanbaohe.notification.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.shifenmiao.base.pullrefresh.PullToRefreshLayout
import com.shifenmiao.base.pullrefresh.rememberPullToRefreshStateOnTime
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.base.utils.DateUtils
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.components.PageLoader
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.network.model.comment.MyComment
import com.shifenmiao.network.model.notification.UserNotification
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNotifications
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.notification.R
import com.wanbaohe.notification.component.NotificationComponent

@Composable
fun NotificationScreen(component: NotificationComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(com.shifenmiao.core.R.string.notification_center),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
        actions = {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.LineMore,
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.notification_mark_all_read)) },
                        enabled = uiState.replies.any { !it.read },
                        onClick = {
                            showMenu = false
                            component.markAllRead()
                        },
                    )
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
        if (uiState.myComments.isEmpty() && uiState.replies.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // section 空就不显示该 section
                if (uiState.myComments.isNotEmpty()) {
                    item(key = "header_my_comments") {
                        SectionHeader(text = stringResource(R.string.notification_section_my_comments))
                    }
                    items(
                        count = uiState.myComments.size,
                        key = { index -> "mc_${uiState.myComments[index].id}" },
                    ) { index ->
                        MyCommentCard(comment = uiState.myComments[index])
                    }
                }
                if (uiState.replies.isNotEmpty()) {
                    item(key = "header_replies") {
                        SectionHeader(text = stringResource(R.string.notification_section_replies))
                    }
                    items(
                        count = uiState.replies.size,
                        key = { index -> "rp_${uiState.replies[index].id}" },
                    ) { index ->
                        val item = uiState.replies[index]
                        ReplyCard(
                            item = item,
                            onClick = { component.markRead(item) },
                        )
                    }
                }
                if (uiState.isLoadingMoreMyComments || uiState.isLoadingMoreReplies) {
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
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
    )
}

/** 「我发表的评论」卡片:自己的头像 + 评论引用 + 来源 + 相对时间 */
@Composable
private fun MyCommentCard(comment: MyComment) {
    val loginState = LocalLoginState.current
    MessageCard(
        avatar = {
            Avatar(
                username = loginState.username,
                nickname = loginState.nickname,
                avatar = loginState.avatar,
                size = 36.dp,
                isLogin = loginState.isLogin,
            )
        },
        title = if (comment.sourceTitle.isNotBlank()) {
            stringResource(R.string.notification_you_commented, comment.sourceTitle)
        } else {
            stringResource(R.string.notification_you_commented_fallback)
        },
        titleBold = false,
        quote = comment.content,
        sourceTitle = comment.sourceTitle,
        createdAt = comment.createdAt,
        onClick = {},
    )
}

/** 「用户回复」卡片:回复人头像 + 回复引用 + 来源 + 相对时间,未读加红点/加粗 */
@Composable
private fun ReplyCard(
    item: UserNotification,
    onClick: () -> Unit,
) {
    MessageCard(
        avatar = {
            ReplyAvatar(
                avatarUrl = item.replierAvatar,
                name = item.replierName,
            )
        },
        title = stringResource(
            R.string.notification_replied_to_you,
            item.replierName.ifBlank { stringResource(R.string.notification_replier_anonymous) },
        ),
        titleBold = !item.read,
        quote = item.content,
        sourceTitle = item.sourceTitle,
        createdAt = item.createdAt,
        showUnreadDot = !item.read,
        onClick = onClick,
    )
}

/** 参考图卡片:左侧头像 + 标题 + 引用内容 + 来源,右侧相对时间 + chevron */
@Composable
private fun MessageCard(
    avatar: @Composable () -> Unit,
    title: String,
    titleBold: Boolean,
    quote: String,
    sourceTitle: String,
    createdAt: Long,
    showUnreadDot: Boolean = false,
    onClick: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            avatar()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (titleBold) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (showUnreadDot) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape),
                        )
                    }
                }
                if (quote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (sourceTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.notification_from_source, sourceTitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(
                    text = relativeTimeText(createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Outlined.LineChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 回复人头像:Coil 加载,空 URL / 加载失败时回退为首字母圆形占位 */
@Composable
private fun ReplyAvatar(
    avatarUrl: String,
    name: String,
) {
    var imageLoaded by remember(avatarUrl) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                onSuccess = { imageLoaded = true },
                onError = { imageLoaded = false },
            )
        }
        if (!imageLoaded) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
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

/** 相对时间:刚刚 / x 分钟前 / x 小时前 / x 天前,超过 7 天显示日期 */
@Composable
private fun relativeTimeText(epochMillis: Long): String {
    if (epochMillis <= 0) return ""
    val diff = System.currentTimeMillis() - epochMillis
    if (diff < 0) return stringResource(com.shifenmiao.core.R.string.time_just_now)
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> stringResource(com.shifenmiao.core.R.string.time_just_now)
        minutes < 60 -> pluralStringResource(
            com.shifenmiao.core.R.plurals.time_minutes_ago_format, minutes.toInt(), minutes.toInt()
        )
        hours < 24 -> pluralStringResource(
            com.shifenmiao.core.R.plurals.time_hours_ago_format, hours.toInt(), hours.toInt()
        )
        days < 7 -> pluralStringResource(
            com.shifenmiao.core.R.plurals.time_days_ago_format, days.toInt(), days.toInt()
        )
        else -> DateUtils.formatDate(epochMillis)
    }
}
