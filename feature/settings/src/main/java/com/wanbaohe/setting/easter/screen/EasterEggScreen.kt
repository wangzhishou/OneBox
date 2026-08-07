@file:Suppress("KotlinConstantConditions")

package com.wanbaohe.setting.easter.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.resources.BuildConfig
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.resources.shapes.MaterialStarShape
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppVersion
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.hapticsClickable
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.liquidGlassRegular
import com.t8rin.imagetoolbox.core.ui.widget.other.EmojiItem
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
import com.wanbaohe.setting.easter.component.EasterEggComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun EasterEggScreen(component: EasterEggComponent) {
    val themeState = LocalDynamicThemeState.current
    val isStartupTraceOverlayEnabled by AppSharedStorage.isStartupTraceOverlayEnabled.collectAsState()
    val allEmojis = Emoji.allIcons()
    val emojiData = remember {
        mutableStateListOf<String>().apply {
            addAll(
                List(11) {
                    allEmojis.random().toString()
                }
            )
        }
    }
    val counter: MutableState<Int> = remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(700.milliseconds)
            if (counter.value > 10) counter.value = 0
            emojiData[counter.value] = allEmojis.random().toString()
            emojiData[10 - counter.value] = allEmojis.random().toString()
            counter.value++
        }
    }

    val painter = painterResource(CoreR.drawable.ic_launcher_foreground)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        EnhancedTopAppBar(
            title = {
                Row(modifier = Modifier.marquee()) {
                    emojiData.forEach { emoji ->
                        EmojiItem(
                            emoji = emoji,
                            fontScale = 1f
                        )
                    }
                }
            },
            navigationIcon = {
                EnhancedIconButton(
                    onClick = component.onGoBack
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.exit)
                    )
                }
            },
            type = EnhancedTopAppBarType.Center
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .fillMaxWidth()
                .liquidGlassRegular(borderWidth = 0.8.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(CoreR.string.startup_trace_overlay_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(CoreR.string.startup_trace_overlay_toggle_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GlassSwitch(
                checked = isStartupTraceOverlayEnabled,
                onCheckedChange = { enabled ->
                    AppSharedStorage.saveStartupTraceOverlayEnabled(enabled)
                    StartupTrace.setEnabled(enabled)
                },
            )
        }

        BoxWithConstraints(
            modifier = Modifier.weight(1f)
        ) {
            val width = this.constraints.maxWidth
            val height = constraints.maxHeight
            val ballSize = (min(maxWidth, maxHeight) * 0.3f).coerceAtMost(180.dp)
            val ballSizePx = with(LocalDensity.current) { ballSize.toPx().roundToInt() }
            var speed by remember { mutableFloatStateOf(0.2f) }

            var x by remember { mutableFloatStateOf((width - ballSizePx) * 1f) }
            var y by remember { mutableFloatStateOf((height - ballSizePx) * 1f) }

            val animatedX = animateFloatAsState(x)
            val animatedY = animateFloatAsState(y)

            var xSpeed by rememberSaveable { mutableFloatStateOf(10f) }
            var ySpeed by rememberSaveable { mutableFloatStateOf(10f) }

            var bounces by remember {
                mutableIntStateOf(0)
            }

            LaunchedEffect(bounces) {
                if (bounces % 10 == 0) {
                    themeState.updateColorTuple(ColorTuple(Color(Random.nextInt())))
                }
            }

            LaunchedEffect(speed) {
                while (isActive) {
                    x += xSpeed * speed
                    y += ySpeed * speed

                    val rightBounce = x > width - ballSizePx
                    val leftBounce = x < 0
                    val bottomBounce = y > height - ballSizePx
                    val topBounce = y < 0

                    if (rightBounce || leftBounce) {
                        xSpeed = -xSpeed
                        bounces++
                    }

                    if (topBounce || bottomBounce) {
                        ySpeed = -ySpeed
                        bounces++
                    }

                    delay(1.milliseconds)
                }
            }

            val icons = remember {
                Screen.entries.mapNotNull { it.icon }
            }
            FlowRow {
                repeat(width * height / (24 * 24)) {
                    val icon = remember(it) {
                        icons.shuffled()[it % icons.size]
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(1.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Box(
                modifier = Modifier.offset {
                    IntOffset(
                        animatedX.value.roundToInt(),
                        animatedY.value.roundToInt()
                    )
                },
                contentAlignment = Alignment.Center
            ) {
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .clip(MaterialStarShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .hapticsClickable {
                            speed = if (speed == 0.2f) {
                                Random.nextFloat()
                            } else 0.2f
                            scope.launch {
                                AppToastHost.showConfetti()
                            }
                        }
                        .size(ballSize),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painter,
                        contentDescription = stringResource(R.string.version),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .size(ballSize * 0.6f)
                            .weight(1f, false)
                    )
                    Column(
                        modifier = Modifier.offset(
                            y = -ballSize * 0.15f
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.version),
                            style = LocalTextStyle.current.copy(
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.9f)
                            ),
                            maxLines = 1
                        )
                        AutoSizeText(
                            text = "$AppVersion\n(${BuildConfig.VERSION_CODE})",
                            style = LocalTextStyle.current.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.5f)
                            ),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
