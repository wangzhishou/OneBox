package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.Avatar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.model.PlayerBarData

/**
 * 双方玩家信息保持在同一行，行棋方显示动态描边。
 */
@Composable
fun PlayersBar(
    top: PlayerBarData,
    bottom: PlayerBarData,
    vsLabel: String,
    turnLabel: String,
    modifier: Modifier = Modifier,
    emphasizeVs: Boolean = false,
    onVsClick: (() -> Unit)? = null,
    vsActionLabel: String = vsLabel,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
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
                modifier = Modifier.padding(horizontal = 4.dp),
                emphasized = emphasizeVs,
                onClick = onVsClick,
                actionLabel = vsActionLabel,
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
        Modifier.clickable(onClickLabel = data.actionLabel.takeIf { it.isNotBlank() }, role = Role.Button) {
            data.onClick.invoke()
        }
    } else {
        Modifier
    }
    val content: @Composable RowScope.() -> Unit = {
        if (!mirrored) {
            Avatar(username = data.name, avatar = data.avatarUrl, size = 32.dp, isLogin = data.avatarIsLoggedIn)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (mirrored) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
                if (data.onClick != null && data.actionLabel.isNotBlank()) {
                    Icon(
                        Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
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
                    text = data.subtitle.ifBlank { if (data.isActiveTurn) turnLabel else "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (mirrored) {
            Spacer(modifier = Modifier.width(6.dp))
            Avatar(username = data.name, avatar = data.avatarUrl, size = 32.dp, isLogin = data.avatarIsLoggedIn)
        }
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (data.isActiveTurn) Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .animatedTurnBorder()
                else Modifier,
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = if (mirrored) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(
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
    emphasized: Boolean,
    onClick: (() -> Unit)?,
    actionLabel: String,
) {
    Box(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
                    .clickable(onClickLabel = actionLabel, role = Role.Button, onClick = onClick)
                else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(50))
                .background(
                    if (emphasized) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                )
                .padding(horizontal = if (emphasized) 12.dp else 10.dp, vertical = if (emphasized) 6.dp else 3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
