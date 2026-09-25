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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.card.TonalCardPalette
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.item.entity.ItemEntity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChatBubble

@Composable
fun OperateBar(
    dataItem: ItemEntity,
    palette: TonalCardPalette? = null,
    tagBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    tagTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    tagText: String? = null,
    onTagClick: (() -> Unit)? = null,
    commentCount: Int? = null,
    onCommentClick: (() -> Unit)? = null,
) {
    val resolvedTagBackgroundColor = palette?.tagBackgroundColor ?: tagBackgroundColor
    val resolvedTagTextColor = palette?.tagTextColor ?: tagTextColor
    val supportingContentColor =
        palette?.supportingContentColor ?: MaterialTheme.colorScheme.outline
    val accentColor = palette?.accentColor ?: MaterialTheme.colorScheme.primary

    val resolvedTagText = tagText?.takeIf { it.isNotBlank() }
        ?: BaseUtils.getNameByType(dataItem.listType)

    val commentLabel = stringResource(com.shifenmiao.core.R.string.comment)
    val resolvedCommentCount = commentCount?.takeIf { it > 0 }
    // 只要业务层提供了点击回调就展示评论入口, 不因为评论数为 0 而隐藏.
    val showComment = onCommentClick != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        // 徽章 fill=false 只按内容占位,剩余空间由 SpaceBetween 分配,保证评论入口始终右对齐
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryTagBadge(
            text = resolvedTagText,
            backgroundColor = resolvedTagBackgroundColor,
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
                contentDescription = commentLabel,
                onClick = onCommentClick,
            )
        }
    }
}

/**
 * 评论入口小芯片: 图标 + (可选) 数量.
 *
 * - count=null 且有点击回调: 只展示图标 (评论数未知, 仅入口)
 * - count<=0 且有点击回调: 同样只展示图标
 * - count>0: 图标 + 文本 "23" 或 "99+" (>99 时)
 *
 * 点击区域命中整个 Row, 而非仅图标, 提升小屏幕命中率.
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
        .padding(horizontal = 6.dp, vertical = 2.dp)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble,
            contentDescription = contentDescription,
            modifier = Modifier.size(12.dp),
            tint = iconTint,
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
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
    backgroundColor: Color,
    textColor: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val badgeModifier = if (onClick != null) {
        modifier
            .glassBackground(
                style = GlassStyle.Thick,
                color = backgroundColor,
                shape = shape,
            )
            .clickable(onClick = onClick)
    } else {
        modifier.glassBackground(
            style = GlassStyle.Thick,
            color = backgroundColor,
            shape = shape,
        )
    }
    Row(
        modifier = badgeModifier.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            maxLines = 1,
            // 分类名超长时先自动降字号,仍放不下才省略号(不用 marquee:长语种会一直滚动、
            // 静止帧只剩残词);宽度上限同时避免挤压右侧评论入口
            autoSize = TextAutoSize.StepBased(
                minFontSize = 9.sp,
                maxFontSize = MaterialTheme.typography.labelSmall.fontSize,
                stepSize = 0.5.sp,
            ),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 140.dp),
        )
    }
}
