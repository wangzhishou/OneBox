package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.Avatar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.model.PlayerBarData

/**
 * 双方玩家条(上/下两侧 + VS 徽章),棋种无关。
 * 上下两侧行棋方显示「轮到」角标与激活描边;激活侧可点击(如换 AI 模型)。
 */
@Composable
fun PlayersBar(
    top: PlayerBarData,
    bottom: PlayerBarData,
    vsLabel: String,
    turnLabel: String,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerBarItem(
                data = top,
                mirrored = false,
                turnLabel = turnLabel,
                modifier = Modifier.weight(1f),
            )
            VsBadge(
                label = vsLabel,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            PlayerBarItem(
                data = bottom,
                mirrored = true,
                turnLabel = turnLabel,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlayerBarItem(
    data: PlayerBarData,
    mirrored: Boolean,
    turnLabel: String,
    modifier: Modifier = Modifier,
) {
    val clickableModifier = if (data.onClick != null) {
        Modifier.clickable { data.onClick.invoke() }
    } else {
        Modifier
    }
    val content: @Composable () -> Unit = {
        if (mirrored && data.isActiveTurn) {
            TurnBadge(label = turnLabel)
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (!mirrored) {
            Avatar(username = data.name, avatar = data.avatarUrl, size = 34.dp, isLogin = true)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start) {
            Text(
                text = data.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(data.indicatorColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!mirrored && data.isActiveTurn) {
            Spacer(modifier = Modifier.width(6.dp))
            TurnBadge(label = turnLabel)
        }
        if (mirrored) {
            Spacer(modifier = Modifier.width(8.dp))
            Avatar(username = data.name, avatar = data.avatarUrl, size = 34.dp, isLogin = true)
        }
    }

    if (data.isActiveTurn) {
        Box(
            modifier = modifier
                .then(clickableModifier)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = if (mirrored) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (mirrored) Arrangement.End else Arrangement.Start,
                content = { content() },
            )
        }
    } else {
        Row(
            modifier = modifier
                .then(clickableModifier)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (mirrored) Arrangement.End else Arrangement.Start,
            content = { content() },
        )
    }
}

@Composable
private fun VsBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** 行棋方的"轮到"角标:虚线描边小胶囊 */
@Composable
private fun TurnBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
            )
            .drawBehind {
                drawRoundRect(
                    color = primary,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 3.dp.toPx()),
                        ),
                    ),
                    cornerRadius = CornerRadius(size.height / 2, size.height / 2),
                )
            }
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = primary,
            maxLines = 1,
        )
    }
}
