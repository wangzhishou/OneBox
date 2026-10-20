package com.wanbaohe.decisionwheel.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChangeCircle
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.BasicEnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.other.DecisionWheelCanvas
import com.wanbaohe.core.ui.widget.other.DecisionWheelItem
import com.wanbaohe.core.ui.widget.other.DecisionWheelPointer
import com.wanbaohe.core.ui.widget.other.DecisionWheelSpinButton
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelSpinComponent
import com.wanbaohe.decisionwheel.component.WheelOption
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import com.t8rin.imagetoolbox.core.resources.Icons

@Composable
fun DecisionWheelSpinScreen(
    component: DecisionWheelSpinComponent,
    bottomInset: Dp = 0.dp
) {
    val uiState by component.uiState.collectAsState()
    val settings by component.settings.collectAsState()

    val wheel = uiState.currentWheel
    val options = wheel?.options.orEmpty()

    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val rotationAnim = remember { Animatable(component.rotationDeg) }
    /** 拖动产生的临时偏移，开始旋转时会被折进 [rotationAnim] */
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }
    val displayRotation = rotationAnim.value + dragOffset

    /**
     * 旋转请求计数器。
     *
     * 注意：不能把 LaunchedEffect 的 key 直接设成 plan 对象再在 effect 里把它置空 ——
     * 置空会改变 key，effect 立刻被取消，动画刚起步就停了（这就是"转不了"的原因）。
     * 这里用一个只增不减的计数器当 key，plan 在 effect 内部现算。
     */
    var spinNonce by remember { mutableIntStateOf(0) }
    var spinVelocity by remember { mutableFloatStateOf(0f) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val segmentPulse = remember { Animatable(0f) }

    // 切 tab / 退出时界面被销毁，未跑完的旋转必须复位，否则 isSpinning 永久卡 true
    DisposableEffect(Unit) {
        onDispose { component.cancelSpin() }
    }

    // ─── 旋转：Component 先定结果，UI 只负责补间 ───────────────────────────
    LaunchedEffect(spinNonce) {
        if (spinNonce == 0) return@LaunchedEffect

        val velocity = spinVelocity
        val plan = component.planSpin(rotationAnim.value + dragOffset) ?: return@LaunchedEffect

        // 把拖动偏移折进主角度，之后 dragOffset 归零，两者不再重复计入
        rotationAnim.snapTo(rotationAnim.value + dragOffset)
        dragOffset = 0f

        // 甩得越猛转得越多、越久，但不改变已经定好的 winner
        val boost = (abs(velocity) / 1000f).coerceIn(0f, 2f)
        val extraTurns = (boost * 3).toInt()
        val targetRotation = plan.targetRotation + extraTurns * 360f
        val durationMillis = (plan.durationMillis * (1f + boost * 0.3f)).toInt()

        isAnimating = true
        selectedIndex = null
        component.startSpinning(durationMillis)

        rotationAnim.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = DecisionWheelSpinComponent.SPIN_EASING
            )
        )

        isAnimating = false
        component.setRotation(targetRotation)
        selectedIndex = plan.winnerIndex
        AppToastHost.showConfetti()
        component.onSpinSettled(plan.winnerIndex)
    }

    // ─── 过扇区 tick 触觉 ──────────────────────────────────────────────────
    val hapticsEnabled = settings.hapticsEnabled
    val latestIsAnimating by rememberUpdatedState(isAnimating)
    val sectorCount by rememberUpdatedState(options.size)
    LaunchedEffect(sectorCount, hapticsEnabled) {
        if (sectorCount <= 0) return@LaunchedEffect
        var lastTick = Int.MIN_VALUE
        snapshotFlow { rotationAnim.value }.collect { value ->
            if (!latestIsAnimating || !hapticsEnabled) {
                lastTick = Int.MIN_VALUE
                return@collect
            }
            val sector = 360f / sectorCount
            val index = floor(value / sector).toInt()
            if (lastTick != Int.MIN_VALUE && index != lastTick) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            lastTick = index
        }
    }

    fun requestSpin(flingVelocity: Float = 0f) {
        if (isAnimating || uiState.isSpinning) return
        if (uiState.activeCount < 2) return
        spinVelocity = flingVelocity
        spinNonce++
    }

    val themePrimary = AppTheme.colorScheme.primaryContainer
    val firstItemColor = options.firstOrNull()?.color ?: themePrimary
    val (pointerColor, centerButtonColor) = remember(firstItemColor) {
        ColorGenerator.splitComplementaryPair(firstItemColor)
    }
    val pointerContentColor = remember(pointerColor) { ColorGenerator.contentColorFor(pointerColor) }
    val centerButtonContentColor = remember(centerButtonColor) { ColorGenerator.contentColorFor(centerButtonColor) }

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        // 标题区固定放模块名：转盘名长度不可控，塞进标题会挤掉返回键 / 触发 marquee 滚动。
        // 当前转盘名挪到右上角的切换控件里，超长也只是控件内省略，不影响导航布局。
        title = stringResource(R.string.decision_wheel_title),
        onGoBack = component::goBack,
        actions = {
            IconButton(onClick = component::openWheelList) {
                Icon(
                    imageVector = Icons.Outlined.LineChangeCircle,
                    contentDescription = stringResource(R.string.switch_wheel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 转盘名放正文：长度完全由用户决定，塞进标题栏会挤掉返回键 / 触发 marquee 滚动。
            // 在这里它有两行空间，超长也只是自己省略，右边还有切换图标兜底。
            Text(
                text = wheel?.title?.ifBlank { null } ?: stringResource(R.string.my_wheels),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            )

            // 指针自身还向上探出 8dp，这里的留白要把它一并算进去，否则盘顶会贴着标题
            Spacer(modifier = Modifier.height(32.dp))

            if (options.size >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .widthIn(max = 400.dp)
                        .aspectRatio(1f)
                        .wheelDrag(
                            enabled = !isAnimating && !uiState.isSpinning,
                            onDelta = { delta -> dragOffset += delta },
                            onDragEnd = { velocity ->
                                component.setRotation(rotationAnim.value + dragOffset)
                                if (abs(velocity) > 260f) requestSpin(velocity)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    DecisionWheelCanvas(
                        items = options.map {
                            DecisionWheelItem(
                                label = it.name,
                                color = it.color,
                                enabled = it.enabled
                            )
                        },
                        rotation = displayRotation,
                        selectedIndex = selectedIndex,
                        highlightAlpha = if (selectedIndex != null) 0.18f else 0f,
                        pulse = segmentPulse.value,
                        onSegmentClick = { index ->
                            if (!isAnimating && !uiState.isSpinning) {
                                selectedIndex = index
                                scope.launch {
                                    segmentPulse.snapTo(0f)
                                    segmentPulse.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                                    segmentPulse.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    DecisionWheelPointer(
                        color = pointerColor,
                        markerColor = pointerContentColor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-8).dp)
                    )

                    if (!isAnimating && !uiState.isSpinning) {
                        DecisionWheelSpinButton(
                            onClick = { requestSpin() },
                            containerColor = centerButtonColor,
                            contentColor = centerButtonContentColor,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassTonalButton(
                    onClick = component::openEditor,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit_options)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.edit_options))
                }
            }

            // 转盘要 2 个以上未抽中的选项才转得动。剩 1 个时点中间按钮是没反应的，
            // 不写清楚用户只会以为 App 卡了。
            if (options.size >= 2 && uiState.activeCount < 2) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        if (uiState.activeCount == 0) R.string.all_options_drawn
                        else R.string.not_enough_active_options
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            // 只有"抽后移除"开着时才解释这件事：关着的时候盘面不会有灰扇区，
            // 冒出一行"已移除 N 个"纯属把没用到的功能怼到用户脸上。
            val removedCount = options.count { !it.enabled }
            if (removedCount > 0 && settings.removeOnSpin) {
                Spacer(modifier = Modifier.height(8.dp))
                RemovedOptionsBar(
                    removedCount = removedCount,
                    onRestoreAll = component::restoreRemovedOptions
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OptionsPreview(
                options = options,
                selectedIndex = selectedIndex,
                onOptionClick = { selectedIndex = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    ResultDialog(
        option = uiState.settledIndex?.let { options.getOrNull(it) },
        visible = uiState.showResult,
        remaining = uiState.activeCount,
        removeOnSpin = settings.removeOnSpin,
        onDismiss = component::dismissResult,
        onSpinAgain = { requestSpin() }
    )
}

/**
 * 结果弹窗：用项目公共的 [BasicEnhancedAlertDialog]，自带遮罩、进出场动画与玻璃降级，
 * 不用自己再维护一套 AnimatedVisibility（之前那版 `visible = true` 恒真就是漏了受控）。
 */
@Composable
private fun ResultDialog(
    option: WheelOption?,
    visible: Boolean,
    remaining: Int,
    removeOnSpin: Boolean,
    onDismiss: () -> Unit,
    onSpinAgain: () -> Unit
) {
    BasicEnhancedAlertDialog(
        visible = visible && option != null,
        onDismissRequest = onDismiss
    ) {
        if (option == null) return@BasicEnhancedAlertDialog
        val optionTextColor = remember(option.color) { ColorGenerator.contentColorFor(option.color) }

        // 结果弹窗要的是"读得清"，不是氛围感：这里刻意不用 GlassCard 的半透明，
        // 用实色容器底色，避免背后盘面上的高饱和扇区透上来干扰文字。
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppTheme.colors.getContainerSurfaceColor(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.result_is),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = option.color,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = optionTextColor,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )
                }

                if (removeOnSpin) {
                    Text(
                        text = stringResource(R.string.remaining_options, remaining),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Text(stringResource(R.string.close))
                    }

                    GlassTonalButton(
                        onClick = onSpinAgain,
                        colors = AppTheme.colors.getSecondaryContainerButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.spin_again_action))
                    }
                }
            }
        }
    }
}

/**
 * 被移除选项的恢复入口。
 *
 * "已移除 N 个选项 + 全部恢复"：带上数量的上下文，比弹窗底部那句没头没尾的
 * "恢复全部选项"好懂得多，也不用进弹窗才能点。
 */
@Composable
private fun RemovedOptionsBar(
    removedCount: Int,
    onRestoreAll: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.removed_options_count, removedCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GlassTextButton(
            onClick = onRestoreAll,
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.restore_all_action),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * 转盘甩动手势：拖动跟手，松手时若角速度够大则触发一次旋转。
 */
private fun Modifier.wheelDrag(
    enabled: Boolean,
    onDelta: (Float) -> Unit,
    onDragEnd: (Float) -> Unit
): Modifier = pointerInput(enabled) {
    if (!enabled) return@pointerInput

    val center = Offset(size.width / 2f, size.height / 2f)

    fun angleOf(point: Offset): Float =
        Math.toDegrees(
            atan2(
                (point.y - center.y).toDouble(),
                (point.x - center.x).toDouble()
            )
        ).toFloat()

    var lastAngle: Float? = null
    var lastTime = 0L
    var velocity = 0f

    detectDragGestures(
        onDragStart = { offset ->
            lastAngle = angleOf(offset)
            lastTime = System.currentTimeMillis()
            velocity = 0f
        },
        onDragEnd = {
            onDragEnd(velocity)
            lastAngle = null
        },
        onDragCancel = { lastAngle = null },
        onDrag = { change, _ ->
            val angle = angleOf(change.position)
            val previous = lastAngle
            if (previous != null) {
                var delta = angle - previous
                if (delta > 180f) delta -= 360f
                if (delta < -180f) delta += 360f

                val now = System.currentTimeMillis()
                val dt = (now - lastTime).coerceAtLeast(1)
                // 简单低通，避免单帧抖动把角速度算飞
                velocity = velocity * 0.6f + (delta / dt) * 1000f * 0.4f
                lastTime = now

                onDelta(delta)
            }
            lastAngle = angle
        }
    )
}

/**
 * 被移除（抽后移除）的扇区显示为低饱和灰，但仍留在盘面上保持扇区角度不变。
 */
/**
 * 已移除选项的统一灰。
 *
 * 和盘面扇区用同一个值：下面 chip 灰、上面扇区也灰，用户才对得上号。
 * 早先是"保留原色、把饱和度压到 12%"，在浅底上几乎看不出来，等于没提示。
 */
private val removedOptionColor = Color(0xFF9E9E9E)

@Composable
private fun OptionsPreview(
    options: List<WheelOption>,
    selectedIndex: Int?,
    onOptionClick: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        options.forEachIndexed { index, option ->
            OptionChip(
                option = option,
                isSelected = selectedIndex == index,
                onClick = { onOptionClick(index) }
            )
        }
    }
}

@Composable
private fun OptionChip(
    option: WheelOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val baseColor = if (option.enabled) option.color else removedOptionColor
    val background by animateColorAsState(
        targetValue = if (isSelected) baseColor else baseColor.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = 200),
        label = "option_chip_bg"
    )
    val contentColor = ColorGenerator.contentColorFor(background)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "option_chip_scale"
    )

    Surface(
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 0.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(18.dp),
        color = background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                text = option.name + if (option.weight != 1f) " ×${option.weight.trim()}" else "",
                color = contentColor.copy(alpha = if (option.enabled) 1f else 0.65f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                textDecoration = if (option.enabled) null else TextDecoration.LineThrough
            )
        }
    }
}

private fun Float.trim(): String =
    if (this % 1f == 0f) toInt().toString() else String.format(Locale.US, "%.1f", this)
