/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.card.TonalCardPalette
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

    // 只要业务层提供了点击回调就展示评论入口(气泡常显);数字只在 >0 时显示,
    // 不给每张卡挂一个「0」当噪点。
    val showComment = onCommentClick != null
    val resolvedCommentCount = if (showComment) commentCount ?: 0 else null
    val commentDescription = if ((resolvedCommentCount ?: 0) > 0) {
        pluralStringResource(
            com.shifenmiao.core.R.plurals.comments_count_format,
            resolvedCommentCount ?: 0,
            resolvedCommentCount ?: 0,
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
            // 短分类贴合文字，长分类截断，避免挤压右侧评论入口
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

/** 评论入口是卡片的次要信息: 比分类文字再淡一档, 图标也小一号。 */
private const val COMMENT_CHROME_ALPHA = 0.7f

private val CommentIconSize = 14.dp

/**
 * 评论入口: 气泡 + 数量。
 *
 * 气泡常显;数量仅在 >0 时出现(0 不显示,别在卡片上留一个「0」)。
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
        .padding(horizontal = 8.dp, vertical = 4.dp)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble,
            contentDescription = null,
            modifier = Modifier.size(CommentIconSize),
            tint = iconTint.copy(alpha = COMMENT_CHROME_ALPHA),
        )
        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = textColor.copy(alpha = COMMENT_CHROME_ALPHA),
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
            .padding(vertical = 4.dp)
            .widthIn(max = 140.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = textColor,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
