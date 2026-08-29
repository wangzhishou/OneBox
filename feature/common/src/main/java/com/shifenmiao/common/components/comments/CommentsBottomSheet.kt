package com.shifenmiao.common.components.comments

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.components.common.ImageThumbnailRow
import com.shifenmiao.common.upload.UploadingImage
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.network.model.comment.Comment
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnlock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSend
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReply

/**
 * 评论内容长度上限, 与 go-proxy [models.CommentContentMaxLen] 对齐.
 * 客户端先拦截, 提交前避免无效网络请求.
 */
const val COMMENT_CONTENT_MAX_LEN = 1000

/**
 * 评论浮动层的"宿主" Composable.
 *
 * - [component] 由父级 Decompose 组件通过 childSlot 创建并持有生命周期;
 *   本函数只负责 EnhancedModalBottomSheet 的开/关动画与内容渲染.
 * - 关闭动画结束后调用 [onDismissed], 父级应从 childSlot 中移除该 child,
 *   Component 随 DESTROY 自动取消 componentScope 里的协程.
 *
 * 用法:
 * ```
 * val commentsSlot by parentComponent.commentsSlot.subscribeAsState()
 * commentsSlot.child?.instance?.let { child ->
 *     CommentsHost(
 *         component = child.component,
 *         onDismissed = parentComponent::dismissComments,
 *     )
 * }
 * ```
 */
@Composable
fun CommentsHost(
    component: CommentsComponent,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    // 只要 CommentsHost 还在 composition, 说明 Decompose slot 里存在 child,
    // 底部弹层应当处于打开状态; 关闭动画结束后再通过 onDismissed 移除 slot.
    var sheetVisible by remember { mutableStateOf(true) }
    var pendingCloseJob by remember { mutableStateOf<Job?>(null) }

    val dismiss = {
        if (pendingCloseJob?.isActive != true) {
            sheetVisible = false
            pendingCloseJob = scope.launch {
                kotlinx.coroutines.delay(380.milliseconds)
                onDismissed()
            }
        }
    }

    EnhancedModalBottomSheet(
        visible = sheetVisible,
        // EnhancedModalBottomSheet 的 onDismiss 参数是"新的 visible 状态",
        // 用户下滑/点遮罩/返回键关闭时传入 false, 此时应触发 dismiss 流程.
        onDismiss = { visible ->
            if (!visible) {
                dismiss()
            }
        },
        dragHandle = {},
        sheetContent = {
            CommentsSheetContent(
                component = component,
                onRequestClose = dismiss,
            )
        },
    )
}

// ──────────────────────────────────────────────────────────────
// 内部 UI: 渲染 CommentsComponent 状态
// ──────────────────────────────────────────────────────────────

@Composable
private fun CommentsSheetContent(
    component: CommentsComponent,
    onRequestClose: () -> Unit,
) {
    val comments by component.comments.collectAsState()
    val isLoading by component.isLoading.collectAsState()
    val isSending by component.isSending.collectAsState()
    val isMutating by component.isMutating.collectAsState()
    val total by component.total.collectAsState()
    val hasMore by component.hasMore.collectAsState()
    val errorMessage by component.errorMessage.collectAsState()
    val replyTarget by component.replyTarget.collectAsState()
    val inputText by component.inputText.collectAsState()
    val isAdmin by component.isAdmin.collectAsState()
    val currentUser by component.currentUser.collectAsState()
    val isLoggedIn by component.isLoggedIn.collectAsState()
    val pendingImages by component.pendingImages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding().height(666.dp)
    ) {
        CommentsSheetHeader(
            title = component.itemTitle.ifBlank {
                AppContext.getString(com.shifenmiao.core.R.string.comments_title)
            },
            total = total,
            onClose = { onRequestClose() },
        )

        CommentsList(
            comments = comments,
            isLoading = isLoading,
            hasMore = hasMore,
            errorMessage = errorMessage,
            isLoggedIn = isLoggedIn,
            isAdmin = isAdmin,
            isMutating = isMutating,
            onLoadMore = component::loadMore,
            onReply = component::setReplyTarget,
            onCancelReply = component::clearReplyTarget,
            onDelete = { c -> component.deleteComment(c.id) },
            onToggleBlock = component::toggleBlock,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        CommentInputBar(
            text = inputText,
            onTextChange = component::setInputText,
            replyTarget = replyTarget,
            isLoggedIn = isLoggedIn,
            currentNickname = currentUser?.nickname.orEmpty(),
            isSending = isSending,
            pendingImages = pendingImages,
            onAddImages = component::addImages,
            onRemoveImage = component::removeImage,
            onCancelReply = component::clearReplyTarget,
            onSend = component::sendComment,
        )
    }
}

@Composable
private fun CommentsSheetHeader(
    title: String,
    total: Int,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = AppContext.getContext().getString(
                    com.shifenmiao.core.R.string.comments_count_format,
                    total,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        GlassIconButton(onClick = onClose) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = AppContext.getString(com.shifenmiao.core.R.string.comment_close),
            )
        }
    }
}

@Composable
private fun CommentsList(
    comments: List<Comment>,
    isLoading: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    isMutating: Boolean,
    onLoadMore: () -> Unit,
    onReply: (Comment?) -> Unit,
    onCancelReply: () -> Unit,
    onDelete: (Comment) -> Unit,
    onToggleBlock: (Comment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, comments.size) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to info.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, totalCount) ->
                if (totalCount > 0 && lastVisible >= totalCount - 3) {
                    onLoadMore()
                }
            }
    }
    if (comments.isEmpty() && isLoading) {
        Box(
            modifier = modifier.fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = AppContext.getString(com.shifenmiao.core.R.string.comment_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else if (comments.isEmpty() && errorMessage != null) {
        Box(
            modifier = modifier.fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    } else if (comments.isEmpty()) {
        EmptyFeedbackState(
            isLoggedIn = isLoggedIn,
            modifier = modifier,
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(comments, key = { it.id }) { comment ->
                CommentRow(
                    comment = comment,
                    isAdmin = isAdmin,
                    onReply = { onReply(comment) },
                    onCancelReply = onCancelReply,
                    onDelete = { onDelete(comment) },
                    onToggleBlock = { onToggleBlock(comment) },
                )
            }
            if (isLoading && hasMore) {
                item(key = "loading_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = AppContext.getString(com.shifenmiao.core.R.string.comment_loading_more),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (!hasMore && comments.isNotEmpty()) {
                item(key = "end_marker") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = AppContext.getString(com.shifenmiao.core.R.string.comment_end_marker),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
    if (isMutating) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassBackground(
                    style = GlassStyle.Thin,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun EmptyFeedbackState(
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = AppContext.getContext()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .glassBackground(
                    style = GlassStyle.Thin,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = context.getString(com.shifenmiao.core.R.string.comment_empty_eyebrow),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = context.getString(com.shifenmiao.core.R.string.comment_empty_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = context.getString(com.shifenmiao.core.R.string.comment_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = context.getString(com.shifenmiao.core.R.string.comment_empty_topics),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = context.getString(
                    if (isLoggedIn) {
                        com.shifenmiao.core.R.string.comment_empty_cta
                    } else {
                        com.shifenmiao.core.R.string.comment_empty_login_cta
                    },
                ),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    isAdmin: Boolean,
    onReply: () -> Unit,
    onCancelReply: () -> Unit,
    onDelete: () -> Unit,
    onToggleBlock: () -> Unit,
) {
    val blocked = comment.blocked

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .glassBackground(
                style = GlassStyle.Thin,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CommentAvatar(
                avatarUrl = comment.author.avatar,
                fallbackName = comment.author.nickname,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.author.nickname.ifBlank {
                        AppContext.getString(com.shifenmiao.core.R.string.comment_anonymous)
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRelativeTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isAdmin) {
                AdminRowActions(
                    blocked = blocked,
                    onDelete = onDelete,
                    onToggleBlock = onToggleBlock,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (blocked) {
                AppContext.getString(com.shifenmiao.core.R.string.comment_blocked)
            } else {
                comment.content
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (blocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        if (!blocked && comment.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ImageThumbnailRow(images = comment.images)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (comment.replyCount > 0) {
                Text(
                    text = AppContext.getContext().getString(
                        com.shifenmiao.core.R.string.comment_reply_count_format,
                        comment.replyCount.toInt(),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                modifier = Modifier
                    .glassBackground(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        style = GlassStyle.Thin,
                        borderWidth = 0.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onReply)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = AppContext.getString(com.shifenmiao.core.R.string.comment_reply),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        comment.recentReply?.let { reply ->
            Spacer(modifier = Modifier.height(6.dp))
            ReplyPreview(reply = reply, onCancelReply = onCancelReply)
        }
    }
}

@Composable
private fun AdminRowActions(
    blocked: Boolean,
    onDelete: () -> Unit,
    onToggleBlock: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(onClick = onToggleBlock) {
            Icon(
                imageVector = if (blocked) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnlock else Icons.Outlined.Block,
                contentDescription = AppContext.getString(
                    if (blocked) com.shifenmiao.core.R.string.comment_unblock
                    else com.shifenmiao.core.R.string.comment_block,
                ),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        GlassIconButton(onClick = onDelete) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = AppContext.getString(
                    com.shifenmiao.core.R.string.comment_delete,
                ),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ReplyPreview(
    reply: Comment,
    onCancelReply: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .glassBackground(
                style = GlassStyle.Thin,
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onCancelReply)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReply,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reply.author.nickname.ifBlank {
                    AppContext.getString(com.shifenmiao.core.R.string.comment_anonymous)
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
            )
            if (reply.content.isNotEmpty()) {
                Text(
                    text = reply.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // 支持纯图片回复: 后端 review #1 允许 content 为空, 这里照常渲染缩略图.
            // ImageThumbnailRow 内部 clickable 自带消费, 不会误触外层 onCancelReply.
            if (reply.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                ImageThumbnailRow(images = reply.images)
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    replyTarget: Comment?,
    isLoggedIn: Boolean,
    currentNickname: String,
    isSending: Boolean,
    pendingImages: List<UploadingImage>,
    onAddImages: (List<Uri>) -> Unit,
    onRemoveImage: (UploadingImage) -> Unit,
    onCancelReply: () -> Unit,
    onSend: () -> Unit,
) {
    val ctx = AppContext.getContext()
    val remaining = COMMENT_CONTENT_MAX_LEN - text.length
    val counterColor = when {
        remaining < 0 -> MaterialTheme.colorScheme.error
        remaining < 100 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    // 不登录也保持可点: 点击时由 ActionUtils.showLogin 弹统一登录组件, 登录成功后自动继续.
    val canAddMore = pendingImages.size < COMMENT_MAX_IMAGES
    val imagePicker = rememberImagePicker(
        picker = Picker.Multiple,
        onSuccess = { uris -> onAddImages(uris) },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 0.dp),
    ) {
        if (pendingImages.isNotEmpty()) {
            PendingImageStrip(
                images = pendingImages,
                maxImages = COMMENT_MAX_IMAGES,
                onRemove = onRemoveImage,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (replyTarget != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReply,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = ctx.getString(
                        com.shifenmiao.core.R.string.reply_hint,
                        replyTarget.author.nickname.ifBlank {
                            AppContext.getString(com.shifenmiao.core.R.string.comment_anonymous)
                        },
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = onCancelReply
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = AppContext.getString(
                            com.shifenmiao.core.R.string.comment_cancel_reply,
                        ),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }


        val hint = when {
            !isLoggedIn -> AppContext.getString(com.shifenmiao.core.R.string.login_to_comment)
            replyTarget != null -> ctx.getString(
                com.shifenmiao.core.R.string.reply_hint,
                replyTarget.author.nickname.ifBlank {
                    currentNickname.ifBlank {
                        AppContext.getString(
                            com.shifenmiao.core.R.string.comment_anonymous
                        )
                    }
                },
            )

            else -> AppContext.getString(com.shifenmiao.core.R.string.comment_hint)
        }
        GlassOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(hint) },
            enabled = isLoggedIn,
            singleLine = false,
            maxLines = 4,
            minLines = 1,
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        onClick = {
                            // 上传需要登录 (Strapi Authenticated 角色才有 upload 权限),
                            // 未登录先弹统一登录组件, 登录成功后自动继续打开图片选择器.
                            ActionUtils.showLogin(source = "comment_add_image") {
                                imagePicker.pickImage()
                            }
                        },
                        enabled = canAddMore,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.AddPhotoAlt,
                            contentDescription = AppContext.getString(
                                com.shifenmiao.core.R.string.comment_add_image,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    GlassIconButton(
                        onClick = onSend,
                        enabled = isLoggedIn &&
                                (text.isNotBlank() || pendingImages.isNotEmpty()) &&
                                !isSending &&
                                text.length <= COMMENT_CONTENT_MAX_LEN,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSend,
                            contentDescription = AppContext.getString(com.shifenmiao.core.R.string.comment_send),
                        )
                    }
                }
            },
            supportingText = {
                Text(
                    text = ctx.getString(
                        com.shifenmiao.core.R.string.comment_char_counter_format,
                        text.length,
                        COMMENT_CONTENT_MAX_LEN,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = counterColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp),
                    textAlign = TextAlign.End,
                )
            }
        )

    }
}

/**
 * 输入栏上方的待发送图片横滚条. 64dp 缩略图, 上传中显示进度圈, 失败显示红边,
 * 已上传显示成功缩略图 + 右上角删除按钮.
 */
@Composable
private fun PendingImageStrip(
    images: List<UploadingImage>,
    maxImages: Int,
    onRemove: (UploadingImage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = AppContext.getContext()
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(images, key = { index, image -> "${image.localUri}_$index" }) { _, image ->
            PendingImageChip(
                image = image,
                onRemove = { onRemove(image) },
            )
        }
        if (images.size < maxImages) {
            item(key = "add_hint") {
                Text(
                    text = ctx.getString(
                        com.shifenmiao.core.R.string.comment_max_images_format,
                        maxImages,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PendingImageChip(
    image: UploadingImage,
    onRemove: () -> Unit,
) {
    val borderColor = when {
        image.isError -> MaterialTheme.colorScheme.error
        image.isUploaded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = image.localUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        if (!image.isUploaded && !image.isError) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
        if (image.isError) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (image.isUploaded || image.isError) {
            // 已上传 / 已失败 两种终态都显示 X, 允许用户移除:
            //   - 失败态: 不显示 X 用户卡住无法清理 (review 反馈)
            //   - 成功态: 允许发送前移除已选图片
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            Color.Black.copy(alpha = 0.55f),
                            shape = CircleShape,
                        )
                        .padding(2.dp),
                )
            }
        }
    }
}

@Composable
private fun CommentAvatar(
    avatarUrl: String,
    fallbackName: String,
    modifier: Modifier = Modifier,
) {
    val size = 32.dp
    if (avatarUrl.isBlank()) {
        FallbackAvatar(name = fallbackName, modifier = modifier.size(size))
        return
    }
    val ctx = AppContext.getContext()
    var imageLoaded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(avatarUrl).build(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            onSuccess = { imageLoaded = true },
            onError = { imageLoaded = false },
        )
        if (!imageLoaded) {
            AvatarLetter(name = fallbackName, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FallbackAvatar(
    name: String,
    modifier: Modifier = Modifier,
) {
    val palette = listOf(
        Color(0xFF6B9DC2),
        Color(0xFFC25E6B),
        Color(0xFF6BC28C),
        Color(0xFFC2A56B),
        Color(0xFF8C6BC2),
        Color(0xFF6BC2C2),
    )
    val bgColor = remember(name) {
        palette[(name.hashCode().mod(palette.size).let { if (it < 0) it + palette.size else it })]
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        AvatarLetter(name = name, fontSize = 12.sp)
    }
}

@Composable
private fun AvatarLetter(name: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString()
        ?: AppContext.getString(com.shifenmiao.core.R.string.comment_anonymous_initial)
    Text(
        text = initial,
        color = Color.White,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize
        ),
    )
}

private fun formatRelativeTime(epochMillis: Long): String {
    if (epochMillis <= 0) return ""
    val now = System.currentTimeMillis()
    val diff = now - epochMillis
    val sec = diff / 1000
    val min = sec / 60
    val hour = min / 60
    val day = hour / 24
    val ctx = AppContext.getContext()
    return when {
        sec < 60 -> ctx.getString(com.shifenmiao.core.R.string.time_just_now)
        min < 60 -> ctx.getString(com.shifenmiao.core.R.string.time_minutes_ago_format, min.toInt())
        hour < 24 -> ctx.getString(com.shifenmiao.core.R.string.time_hours_ago_format, hour.toInt())
        day < 30 -> ctx.getString(com.shifenmiao.core.R.string.time_days_ago_format, day.toInt())
        else -> {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            date.format(java.util.Date(epochMillis))
        }
    }
}