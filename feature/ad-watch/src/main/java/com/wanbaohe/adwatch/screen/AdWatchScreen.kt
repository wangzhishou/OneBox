package com.wanbaohe.adwatch.screen

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.adwatch.R
import com.wanbaohe.adwatch.ads.AdWatchAds
import com.wanbaohe.adwatch.ads.RewardedAdStatus
import com.wanbaohe.adwatch.component.AdWatchComponent
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AdWatchScreen(
    component: AdWatchComponent
) {
    val state by component.uiState.collectAsState()
    val adStatus by component.adStatus.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    BaseScreen(
        title = stringResource(R.string.ad_watch_title),
        onGoBack = component.onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            AdWatchHero()
            Spacer(modifier = Modifier.height(20.dp))
            RotatingHeadline()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ad_watch_subtitle, state.rewardPoints),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            RemainingCard(
                remainingToday = state.remainingToday,
                dailyLimit = AdWatchAds.DAILY_LIMIT,
            )
            Spacer(modifier = Modifier.height(24.dp))
            WatchCta(
                rewardPoints = state.rewardPoints,
                remainingToday = state.remainingToday,
                adStatus = adStatus,
                isShowing = state.isShowing,
                onWatch = { activity?.let(component::onWatchClick) },
                onRetry = component::retryLoad,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 头部图标群: 中央眼睛(脉冲 + 雷达扩散圈), 周围星星/礼盒/金币/放大镜/闪光
 * 各自错位浮动、闪光眨动、星星摇摆, 全部动画跑在 graphicsLayer 里不触发重组。
 */
@Composable
private fun AdWatchHero() {
    // 跟随主题: 眼睛 primary / 环绕图标 tertiary·secondary·primary 轮换, 背景 primaryContainer
    val eyeColor = MaterialTheme.colorScheme.onPrimaryContainer
    val eyeBg = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val transition = rememberInfiniteTransition(label = "hero")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val eyePulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eyePulse",
    )
    val ringProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring",
    )
    val twinkle by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "twinkle",
    )

    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 眼睛背后的雷达扩散圈
        Box(
            modifier = Modifier
                .size((100 + 80 * ringProgress).dp)
                .graphicsLayer { alpha = (1f - ringProgress) * 0.4f }
                .border(width = 2.dp, color = eyeColor, shape = CircleShape),
        )
        // 中央眼睛
        Box(
            modifier = Modifier
                .size(108.dp)
                .background(color = eyeBg.copy(alpha = 0.45f), shape = CircleShape)
                .border(width = 3.dp, color = eyeColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = null,
                tint = eyeColor,
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = eyePulse
                        scaleY = eyePulse
                    },
            )
        }

        OrbitIcon(
            imageVector = Icons.Rounded.Star,
            tint = tertiaryColor,
            x = (-88).dp,
            y = (-76).dp,
            size = 36.dp,
            phase = phase,
            phaseShift = 0f,
            swing = true,
        )
        OrbitIcon(
            imageVector = Icons.Rounded.AutoAwesome,
            tint = secondaryColor,
            x = 86.dp,
            y = (-70).dp,
            size = 34.dp,
            phase = phase,
            phaseShift = 0.35f,
            twinkleAlpha = twinkle,
        )
        OrbitIcon(
            imageVector = Icons.Rounded.CardGiftcard,
            tint = eyeColor,
            x = 96.dp,
            y = 28.dp,
            size = 38.dp,
            phase = phase,
            phaseShift = 0.55f,
        )
        OrbitIcon(
            imageVector = Icons.Rounded.Paid,
            tint = secondaryColor,
            x = (-96).dp,
            y = 12.dp,
            size = 40.dp,
            phase = phase,
            phaseShift = 0.2f,
        )
        OrbitIcon(
            imageVector = Icons.Rounded.Search,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            x = (-62).dp,
            y = 84.dp,
            size = 32.dp,
            phase = phase,
            phaseShift = 0.75f,
        )
        OrbitIcon(
            imageVector = Icons.Rounded.AutoAwesome,
            tint = tertiaryColor,
            x = 58.dp,
            y = 88.dp,
            size = 30.dp,
            phase = phase,
            phaseShift = 0.9f,
            twinkleAlpha = 1f - twinkle * 0.6f,
        )
    }
}

@Composable
private fun BoxScope.OrbitIcon(
    imageVector: ImageVector,
    tint: Color,
    x: Dp,
    y: Dp,
    size: Dp,
    phase: Float,
    phaseShift: Float,
    swing: Boolean = false,
    twinkleAlpha: Float? = null,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .align(Alignment.Center)
            .size(size)
            .graphicsLayer {
                translationX = x.toPx()
                translationY =
                    y.toPx() + sin((phase + phaseShift) * 2 * PI).toFloat() * 6.dp.toPx()
                if (swing) {
                    rotationZ = sin((phase + phaseShift) * 2 * PI).toFloat() * 14f
                }
                twinkleAlpha?.let { alpha = it }
            },
    )
}

/** 大标题轮换: 每 4 秒上下滚动切换一条创意文案 */
@Composable
private fun RotatingHeadline() {
    val headlines = listOf(
        stringResource(R.string.ad_watch_headline_1),
        stringResource(R.string.ad_watch_headline_2),
        stringResource(R.string.ad_watch_headline_3),
        stringResource(R.string.ad_watch_headline_4),
        stringResource(R.string.ad_watch_headline_5),
    )
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(headlines.size) {
        while (true) {
            delay(4000)
            index = (index + 1) % headlines.size
        }
    }
    AnimatedContent(
        targetState = index,
        transitionSpec = {
            (slideInVertically { it / 2 } + fadeIn()) togetherWith
                (slideOutVertically { -it / 2 } + fadeOut())
        },
        label = "headline",
    ) { i ->
        Text(
            text = headlines[i],
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** 今日剩余次数卡: 播放圆钮 + 剩余文案 + 圆点进度(亮 = 剩余) */
@Composable
private fun RemainingCard(
    remainingToday: Int,
    dailyLimit: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassRegular(shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(
                    R.string.ad_watch_remaining,
                    remainingToday,
                    dailyLimit,
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(dailyLimit) { i ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (i < remainingToday) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

/** 底部 CTA: 加载中转圈 / 失败重试 / 次数用完置灰 / 正常橙色大按钮 */
@Composable
private fun WatchCta(
    rewardPoints: Int,
    remainingToday: Int,
    adStatus: RewardedAdStatus,
    isShowing: Boolean,
    onWatch: () -> Unit,
    onRetry: () -> Unit,
) {
    val ctaColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    when {
        remainingToday <= 0 -> {
            EnhancedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = false,
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(
                    text = stringResource(R.string.ad_watch_daily_limit_reached),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        adStatus == RewardedAdStatus.FAILED -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.ad_watch_load_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ctaColors,
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ad_watch_retry),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        else -> {
            val isBusy = adStatus == RewardedAdStatus.LOADING || isShowing
            Button(
                onClick = onWatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = adStatus == RewardedAdStatus.READY && !isShowing,
                colors = ctaColors,
                shape = RoundedCornerShape(28.dp),
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.ad_watch_loading),
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.ad_watch_cta, rewardPoints),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
