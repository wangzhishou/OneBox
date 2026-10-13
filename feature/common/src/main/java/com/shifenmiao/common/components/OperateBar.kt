package com.shifenmiao.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.card.TonalCardPalette
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.item.entity.ItemEntity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChatBubble

@Composable
fun OperateBar(
    dataItem: ItemEntity,
    palette: TonalCardPalette? = null,
    tagTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    tagText: String? = null,
    onTagClick: (() -> Unit)? = null,
    commentCount: Int? = null,
    onCommentClick: (() -> Unit)? = null,
) {
    val resolvedTagTextColor = palette?.tagTextColor ?: tagTextColor
    val supportingContentColor =
        palette?.supportingContentColor ?: MaterialTheme.colorScheme.outline
    val accentColor = palette?.accentColor ?: MaterialTheme.colorScheme.onPrimaryContainer

    val resolvedTagText = tagText?.takeIf { it.isNotBlank() }
        ?: BaseUtils.getNameByType(dataItem.listType)

    // 只要业务层提供了点击回调就展示评论入口。数量未知按 0 显示,
    // 避免「有的卡片只有气泡、有的气泡旁边有数字」被读成未读提醒。
    val showComment = onCommentClick != null
    val resolvedCommentCount = if (showComment) commentCount ?: 0 else null
    val commentDescription = if (resolvedCommentCount != null) {
        stringResource(
            com.shifenmiao.core.R.string.comments_count_format,
            resolvedCommentCount,
        )
    } else {
        stringResource(com.shifenmiao.core.R.string.comment)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        // 徽章 fill=false 只按内容占位,剩余空间由 SpaceBetween 分配,保证评论入口始终右对齐
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryTagBadge(
            text = resolvedTagText,
            textColor = resolvedTagTextColor,
            onClick = onTagClick,
            // 占满剩余空间但不强制撑满:短分类贴合文字,长分类截断后由徽章内文字跑马灯滚动
            modifier = Modifier.weight(1f, fill = false),
        )
        if (showComment) {
            Spacer(modifier = Modifier.width(8.dp))
            CommentCountChip(
                count = resolvedCommentCount,
                iconTint = supportingContentColor,
                textColor = supportingContentColor,
                contentDescription = commentDescription,
                onClick = onCommentClick,
            )
        }
    }
}

/**
 * 评论入口: 气泡 + 数量。
 *
 * 数量始终显示(含 0), 读作「评论数」而不是未读红点或会话数。
 * 超过 99 显示 "99+"。整行都是点击热区。
 */
@Composable
private fun CommentCountChip(
    count: Int?,
    iconTint: Color,
    textColor: Color,
    contentDescription: String,
    onClick: (() -> Unit)?,
) {
    val baseModifier = Modifier
        .let { mod ->
            if (onClick != null) mod.clickable(onClick = onClick) else mod
        }
        .clearAndSetSemantics { this.contentDescription = contentDescription }
        .padding(horizontal = 6.dp, vertical = 2.dp)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = iconTint,
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategoryTagBadge(
    text: String,
    textColor: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // 纯文字, 不做成胶囊, 避免和顶部 Notes/App 分段抢。点击仍然按分类筛选当前列表。
    Text(
        text = text,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 2.dp)
            .widthIn(max = 140.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = textColor,
        // 分类名超长时先自动降字号,再允许折成两行,仍放不下才省略号(不用 marquee:
        // 长语种会一直滚动、静止帧只剩残词);宽度上限同时避免挤压右侧评论入口
        maxLines = 2,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 9.sp,
            maxFontSize = MaterialTheme.typography.labelSmall.fontSize,
            stepSize = 0.5.sp,
        ),
        overflow = TextOverflow.Ellipsis,
    )
}
