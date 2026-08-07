package com.wanbaohe.profile.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.profile.ui.AboutGlassCard
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkspacePremium

data class VipLevelInfo(
    val level: Int,
    val nameRes: Int,
    val descRes: Int,
    val requiredAmount: Int,
)

val vipLevelList: List<VipLevelInfo> = listOf(
    VipLevelInfo(0, R.string.vip_level_0_name, R.string.vip_level_0_desc, 0),
    VipLevelInfo(1, R.string.vip_level_1_name, R.string.vip_level_1_desc, 100),
    VipLevelInfo(2, R.string.vip_level_2_name, R.string.vip_level_2_desc, 200),
    VipLevelInfo(3, R.string.vip_level_3_name, R.string.vip_level_3_desc, 300),
    VipLevelInfo(4, R.string.vip_level_4_name, R.string.vip_level_4_desc, 400),
    VipLevelInfo(5, R.string.vip_level_5_name, R.string.vip_level_5_desc, 500),
    VipLevelInfo(6, R.string.vip_level_6_name, R.string.vip_level_6_desc, 600),
    VipLevelInfo(7, R.string.vip_level_7_name, R.string.vip_level_7_desc, 700),
    VipLevelInfo(8, R.string.vip_level_8_name, R.string.vip_level_8_desc, 800),
    VipLevelInfo(9, R.string.vip_level_9_name, R.string.vip_level_9_desc, 900),
)

private val levelColors = listOf(
    0xFF9E9E9E, // 0 - Gray
    0xFF8BC34A, // 1 - Light Green
    0xFF4CAF50, // 2 - Green
    0xFF00BCD4, // 3 - Cyan
    0xFF2196F3, // 4 - Blue
    0xFF3F51B5, // 5 - Indigo
    0xFF9C27B0, // 6 - Purple
    0xFFE91E63, // 7 - Pink
    0xFFFF9800, // 8 - Orange
    0xFFFFD700, // 9 - Gold
)

@Composable
fun VipLevelScreen(
    onGoBack: () -> Unit = {}
) {
    val loginState = LocalLoginState.current
    val currentLevel = loginState.vipLevel
    val totalRecharged = loginState.totalRechargeAmount

    BaseScreen(
        title = stringResource(id = R.string.vip_level_title),
        onGoBack = onGoBack,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Current level hero card
            item {
                VipCurrentLevelCard(
                    currentLevel = currentLevel,
                    totalRecharged = totalRecharged,
                )
            }

            // Progress to next level
            if (currentLevel < 9) {
                item {
                    VipProgressCard(
                        currentLevel = currentLevel,
                        totalRecharged = totalRecharged,
                    )
                }
            }

            // Section header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.vip_level_description),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }

            // Level list
            itemsIndexed(vipLevelList) { _, levelInfo ->
                VipLevelItem(
                    levelInfo = levelInfo,
                    isCurrentLevel = levelInfo.level == currentLevel,
                    isUnlocked = levelInfo.level <= currentLevel,
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun VipCurrentLevelCard(
    currentLevel: Int,
    totalRecharged: Double,
) {
    val levelInfo = vipLevelList.getOrNull(currentLevel) ?: vipLevelList[0]
    val color = levelColors.getOrNull(currentLevel) ?: levelColors[0]

    AboutGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${stringResource(R.string.vip_level_current)} · VIP $currentLevel",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(levelInfo.nameRes),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(levelInfo.descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(
                    R.string.vip_level_recharge_from_zero,
                    String.format("%.0f", totalRecharged)
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun VipProgressCard(
    currentLevel: Int,
    totalRecharged: Double,
) {
    val nextLevel = currentLevel + 1
    val nextLevelInfo = vipLevelList.getOrNull(nextLevel) ?: vipLevelList.last()
    val currentRequired = vipLevelList.getOrNull(currentLevel)?.requiredAmount ?: 0
    val nextRequired = nextLevelInfo.requiredAmount

    val progress = remember(totalRecharged, currentRequired, nextRequired) {
        val range = (nextRequired - currentRequired).toDouble()
        if (range <= 0) 1f
        else ((totalRecharged - currentRequired) / range).coerceIn(0.0, 1.0).toFloat()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "vip_progress",
    )

    val remaining = (nextRequired - totalRecharged).coerceAtLeast(0.0)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerAlpha = 0.24f,
        borderWidth = 0.8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "VIP $currentLevel",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "VIP $nextLevel",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    R.string.vip_level_next_level_progress,
                    String.format("%.0f", remaining)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VipLevelItem(
    levelInfo: VipLevelInfo,
    isCurrentLevel: Boolean,
    isUnlocked: Boolean,
) {
    val color = levelColors.getOrNull(levelInfo.level) ?: levelColors[0]

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerAlpha = if (isCurrentLevel) 0.36f else 0.20f,
        borderWidth = if (isCurrentLevel) 1.5.dp else 0.6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Level number badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isUnlocked && !isCurrentLevel) {
                    androidx.compose.material3.Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "${levelInfo.level}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isUnlocked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(levelInfo.nameRes),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isCurrentLevel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    if (isCurrentLevel) {
                        Text(
                            text = stringResource(com.wanbaohe.profile.R.string.vip_level_current_tag),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(levelInfo.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (levelInfo.requiredAmount == 0) stringResource(R.string.free)
                else stringResource(com.wanbaohe.profile.R.string.vip_level_required_amount, levelInfo.requiredAmount),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (isUnlocked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
