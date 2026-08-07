package com.wanbaohe.decisionwheel.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheelCanvas
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheelItem
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheelPointer
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheelSpinButton
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelComponent
import com.wanbaohe.decisionwheel.component.WheelOption
import com.wanbaohe.decisionwheel.ui.DecisionWheelPalettePickerSheet
import com.wanbaohe.decisionwheel.ui.DecisionWheelSettingsSheet
import com.wanbaohe.decisionwheel.ui.EditWheelDialog
import com.wanbaohe.decisionwheel.ui.HistoryDialog
import com.wanbaohe.decisionwheel.ui.WheelListDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

@Composable
fun DecisionWheelScreen(
    decisionWheelComponent: DecisionWheelComponent,
    appComponent: AppComponent
) {
    val uiState by decisionWheelComponent.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var isAnimating by remember { mutableStateOf(false) }
    // 旋转角度状态
    var targetRotation by remember { mutableFloatStateOf(0f) }

    // Settings (local only; no persistence requested)
    var showSettingsSheet by remember { mutableStateOf(false) }
    var spinDurationMillis by remember { mutableIntStateOf(4000) }
    var spinSpeedMultiplier by remember { mutableFloatStateOf(1f) }

    // 自定义缓动函数：模拟物理减速效果 (加速 -> 匀速 -> 减速 -> 轻微回弹)
    val customEasing = CubicBezierEasing(0.33f, 0.0f, 0.2f, 1.0f)

    // 本地Essentials


    // 旋转动画（用 Animatable 避免 animateFloatAsState 在该模块不可用的问题）
    val rotationAnim = remember { Animatable(0f) }
    val latestIsAnimating by rememberUpdatedState(isAnimating)
    val latestUiState by rememberUpdatedState(uiState)

    LaunchedEffect(targetRotation) {
        val from = rotationAnim.value
        if (from == targetRotation) return@LaunchedEffect

        rotationAnim.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(
                durationMillis = spinDurationMillis,
                easing = customEasing
            )
        )

        if (latestIsAnimating) {
            // finishListener 逻辑
            isAnimating = false
            val currentWheel = latestUiState.currentWheel
            if (currentWheel != null && currentWheel.options.isNotEmpty()) {
                val sectorAngle = 360f / currentWheel.options.size
                val normalizedRotation = ((360f - (targetRotation % 360f)) % 360f)
                val selectedIndex =
                    (normalizedRotation / sectorAngle).toInt() % currentWheel.options.size
                decisionWheelComponent.onSpinComplete(currentWheel.options[selectedIndex])
                AppToastHost.showConfetti()
            }
        }
    }

    val rotation = rotationAnim.value

    BaseScreen(
        title = stringResource(CoreR.string.decision_wheel_title),
        onGoBack = {
            appComponent.onGoBack()
        },
        navigationIcon = {
            Row {
                IconButton(
                    onClick = {
                        appComponent.onGoBack()
                    }
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                // 历史记录按钮
                IconButton(
                    onClick = {
                        decisionWheelComponent.toggleHistory()
                    }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = stringResource(R.string.history),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        },
        actions = {
            // 设置按钮
            IconButton(
                onClick = { showSettingsSheet = true }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.wheel_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 转盘列表按钮
            IconButton(
                onClick = {
                    decisionWheelComponent.toggleWheelList()
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.my_wheels)
                )
            }
        }
    ) {
        DecisionWheelContent(
            decisionWheelComponent = decisionWheelComponent,
            isAnimating = isAnimating,
            rotation = rotation,
            onStartSpin = { newRotation ->
                isAnimating = true
                targetRotation = newRotation
            },
            spinSpeedMultiplier = spinSpeedMultiplier
        )
    }

    BackHandler {
        appComponent.onGoBack()
    }

    // 设置面板
    DecisionWheelSettingsSheet(
        visible = showSettingsSheet,
        durationMillis = spinDurationMillis,
        speedMultiplier = spinSpeedMultiplier,
        enabled = !isAnimating && !uiState.isSpinning,
        onDismiss = { showSettingsSheet = false },
        onApply = { duration, speed ->
            // Boundaries for safety
            spinDurationMillis = duration.coerceIn(1500, 8000)
            spinSpeedMultiplier = speed.coerceIn(0.6f, 1.6f)
        }
    )

    // 结果对话框
    if (uiState.showResult && uiState.selectedOption != null) {
        ResultDialog(
            option = uiState.selectedOption!!,
            onDismiss = { decisionWheelComponent.resetWheel() },
            onSpinAgain = {
                decisionWheelComponent.resetWheel()
                scope.launch {
                    delay(100.milliseconds)
                    if (uiState.currentWheel != null && !isAnimating) {
                        decisionWheelComponent.startSpinning()
                        isAnimating = true
                        val baseRotations = (Random.nextInt(5, 9) * 360f) * spinSpeedMultiplier
                        val randomAngle = Random.nextFloat() * 360f
                        targetRotation = rotation + baseRotations + randomAngle
                    }
                }
            }
        )
    }

    // 编辑对话框
    EditWheelDialog(
        visible = uiState.showEditDialog && uiState.currentWheel != null,
        title = uiState.currentWheel?.title ?: "",
        options = uiState.currentWheel?.options ?: emptyList(),
        onDismiss = { decisionWheelComponent.toggleEditDialog() },
        onSave = { newTitle, newOptions ->
            scope.launch {
                decisionWheelComponent.createWheel(newTitle, newOptions)
                decisionWheelComponent.toggleEditDialog()
            }
        },
        onAddOption = { name, color ->
            // Add option immediately to database
            decisionWheelComponent.addOption(name, color)
        },
        onDeleteOption = { optionId ->
            // Delete option immediately from database
            decisionWheelComponent.removeOption(optionId)
        }
    )

    // 历史记录对话框
    HistoryDialog(
        visible = uiState.showHistory,
        historyList = uiState.historyList,
        onDismiss = { decisionWheelComponent.toggleHistory() },
        onClearHistory = { decisionWheelComponent.clearHistory() }
    )

    // 转盘列表对话框
    WheelListDialog(
        visible = uiState.showWheelList,
        wheels = uiState.savedWheels,
        currentWheelId = uiState.currentWheel?.id,
        onDismiss = { decisionWheelComponent.toggleWheelList() },
        onSelectWheel = { wheel ->
            decisionWheelComponent.switchWheel(wheel)
        },
        onDeleteWheel = { wheelId ->
            decisionWheelComponent.deleteWheel(wheelId)
        },
        onCreateWheel = {
            decisionWheelComponent.createNewWheel()
            // 保持弹窗打开，让用户看到新创建的转盘
        }
    )
}

@Composable
fun DecisionWheelContent(
    decisionWheelComponent: DecisionWheelComponent,
    isAnimating: Boolean,
    rotation: Float,
    onStartSpin: (Float) -> Unit,
    spinSpeedMultiplier: Float
) {
    val uiState by decisionWheelComponent.uiState.collectAsState()

    var showPaletteSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Cross-interaction state between wheel and list.
    var selectedOptionIndex by remember(uiState.currentWheel?.id, uiState.currentWheel?.options) {
        mutableStateOf<Int?>(null)
    }

    val segmentPulse = remember { Animatable(0f) }

    // Additional rotation used only for interactions (not for spinning).
    val interactionRotation = remember(uiState.currentWheel?.id) { Animatable(0f) }
    val displayRotation = rotation + interactionRotation.value

    // List item feedback animation (bounce + indicator).
    val listItemPrompt = remember(uiState.currentWheel?.id) { Animatable(0f) }

    val themePrimary = AppTheme.colorScheme.primary

    val wheel = uiState.currentWheel

    // Base color is user-chosen if available; fallback only for first launch/empty colors.
    val baseColorForPalette = remember(wheel?.id, wheel?.options) {
        wheel?.options?.firstOrNull()?.color?.takeIf { it != Color.Unspecified } ?: themePrimary
    }

    val wheelOptionCount = wheel?.options?.size ?: 0

    // The UI accent (pointer + center button) follows the first segment.
    val firstItemColor = wheel?.options?.firstOrNull()?.color ?: themePrimary
    // Use split-complementary harmony to make the center UI pop while staying color-theory aligned.
    val (pointerColor, centerButtonColor) = remember(firstItemColor) {
        ColorGenerator.splitComplementaryPair(firstItemColor)
    }
    val pointerContentColor = remember(pointerColor) {
        ColorGenerator.contentColorFor(pointerColor)
    }
    val centerButtonContentColor = remember(centerButtonColor) {
        ColorGenerator.contentColorFor(centerButtonColor)
    }

    // Segment highlight animation (flat: just alpha; Animatable for compatibility).
    val highlightAlphaAnim = remember { Animatable(0f) }
    val targetHighlightAlpha = if (selectedOptionIndex != null) 0.18f else 0f
    LaunchedEffect(targetHighlightAlpha) {
        highlightAlphaAnim.animateTo(
            targetValue = targetHighlightAlpha,
            animationSpec = tween(
                durationMillis = 220,
                easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
            )
        )
    }
    val highlightAlpha = highlightAlphaAnim.value

    suspend fun selectIndex(index: Int) {
        val opts = wheel?.options ?: return
        if (index !in opts.indices) return

        selectedOptionIndex = index

        // Prompt the list item: quick bounce + indicator flash.
        listItemPrompt.snapTo(0f)
        // 0 -> 1 quickly (flash in)
        listItemPrompt.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
        )
        // slight settle
        listItemPrompt.animateTo(
            targetValue = 0.6f,
            animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
        )
        // bounce back
        listItemPrompt.animateTo(
            targetValue = 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
        // fade out
        listItemPrompt.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
        )

        // If not spinning, gently rotate wheel so selected segment center aligns to top pointer.
        // Our segment centers are at: (index * sector + sector/2 - 90 + rotation) degrees.
        // We want that to equal -90 => rotation modulo 360 should be: -index*sector - sector/2.
        if (!isAnimating && !uiState.isSpinning) {
            val sector = 360f / opts.size
            val desiredRotationMod = (-index * sector - sector / 2f)
            val currentTotal = (rotation + interactionRotation.value)
            val currentMod = ((currentTotal % 360f) + 360f) % 360f
            val desiredMod = ((desiredRotationMod % 360f) + 360f) % 360f

            // Shortest delta in [-180, 180].
            var delta = desiredMod - currentMod
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f

            // Animate interactionRotation by delta with a tiny overshoot (1–2°) then settle.
            // This creates a more physical "snap" without adding shadows or heavy effects.
            val from = interactionRotation.value
            val target = from + delta

            // Overshoot direction follows delta sign; if delta is tiny, don't overshoot.
            val overshoot = when {
                kotlin.math.abs(delta) < 0.5f -> 0f
                delta > 0f -> 1.5f
                else -> -1.5f
            }

            // 1) Move to overshoot target
            if (overshoot != 0f) {
                interactionRotation.animateTo(
                    targetValue = target + overshoot,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                )
                // 2) Settle back
                interactionRotation.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                )
            } else {
                interactionRotation.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
            }
        }

        // Pulse the segment.
        segmentPulse.snapTo(0f)
        segmentPulse.animateTo(
            targetValue = 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
        segmentPulse.animateTo(
            targetValue = 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }

    val contentScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(contentScrollState)
        ) {
            // 标题
            uiState.currentWheel?.let { wheel ->
                Text(
                    text = wheel.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 转盘区域
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .widthIn(max = 420.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // 转盘
                uiState.currentWheel?.let { wheel ->
                    val wheelItems = wheel.options.map {
                        DecisionWheelItem(label = it.name, color = it.color)
                    }
                    DecisionWheelCanvas(
                        items = wheelItems,
                        rotation = displayRotation,
                        selectedIndex = selectedOptionIndex,
                        highlightAlpha = highlightAlpha,
                        pulse = segmentPulse.value,
                        onSegmentClick = { idx ->
                            if (!uiState.isSpinning && !isAnimating) {
                                scope.launch { selectIndex(idx) }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 中心指针
                DecisionWheelPointer(
                    color = pointerColor,
                    markerColor = pointerContentColor,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-8).dp)
                )

                // 中心按钮
                if (!uiState.isSpinning && !isAnimating) {
                    DecisionWheelSpinButton(
                        onClick = {
                            if (uiState.currentWheel != null && !isAnimating) {
                                decisionWheelComponent.startSpinning()

                                // Speed multiplier scales the number of full rotations.
                                val baseRotations =
                                    (Random.nextInt(5, 9) * 360f) * spinSpeedMultiplier
                                val randomAngle = Random.nextFloat() * 360f
                                val newTargetRotation = rotation + baseRotations + randomAngle
                                onStartSpin(newTargetRotation)
                            }
                        },
                        containerColor = centerButtonColor,
                        contentColor = centerButtonContentColor,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑转盘按钮
                if (!uiState.isSpinning && !isAnimating) {
                    FilledTonalButton(
                        onClick = { decisionWheelComponent.toggleEditDialog() },
                        modifier = Modifier.height(48.dp),
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_options)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.edit_options))
                    }

                    FilledTonalButton(
                        onClick = { showPaletteSheet = true },
                        modifier = Modifier.height(48.dp),
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                            contentDescription = stringResource(R.string.palette_scheme)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.palette_scheme))
                    }
                }

                // 重新开始按钮（统一 FilledTonalButton + AppTheme.colors）
                if (uiState.showResult) {
                    GlassTonalButton(
                        onClick = { decisionWheelComponent.resetWheel() },
                        modifier = Modifier.height(48.dp),
                        colors = AppTheme.colors.getSecondaryContainerButtonColors()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.spin_again)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.spin_again))
                    }
                }
            }

            // 选项列表预览
            uiState.currentWheel?.let { wheel ->
                OptionsPreview(
                    options = wheel.options,
                    selectedIndex = selectedOptionIndex,
                    promptProgress = listItemPrompt.value,
                    onOptionClick = { idx ->
                        if (!uiState.isSpinning && !isAnimating) {
                            scope.launch { selectIndex(idx) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    DecisionWheelPalettePickerSheet(
        visible = showPaletteSheet,
        onDismiss = { showPaletteSheet = false },
        initialBaseColor = baseColorForPalette,
        previewCount = wheelOptionCount,
        onConfirm = { confirmedBaseColor ->
            val current = uiState.currentWheel
            if (current != null && current.options.isNotEmpty()) {
                val colors = ColorGenerator.generateSegmentColors(
                    baseColor = confirmedBaseColor,
                    count = current.options.size
                )
                val updatedOptions = current.options.mapIndexed { idx, opt ->
                    opt.copy(color = colors.getOrNull(idx)?.background ?: opt.color)
                }
                decisionWheelComponent.createWheel(current.title, updatedOptions)
            }
        }
    )
}

/**
 * 选项预览列表
 */
@Composable
fun OptionsPreview(
    options: List<WheelOption>,
    selectedIndex: Int?,
    promptProgress: Float,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(0.8f)
    ) {
        options.forEachIndexed { index, option ->
            key(option.id) {
                OptionChip(
                    option = option,
                    isSelected = selectedIndex == index,
                    promptProgress = if (selectedIndex == index) promptProgress else 0f,
                    onClick = { onOptionClick(index) }
                )
            }
        }
    }
}


@Composable
fun OptionChip(
    option: WheelOption,
    isSelected: Boolean,
    promptProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (isSelected) option.color else option.color.copy(alpha = 0.92f),
        animationSpec = tween(durationMillis = 200),
        label = "option_chip_bg"
    )
    val contentColor = ColorGenerator.contentColorFor(background)

    // Base selection scale + short bounce when promptProgress runs.
    val targetScale = when {
        promptProgress > 0f -> 1f + 0.10f * promptProgress
        isSelected -> 1.06f
        else -> 1f
    }
    val scaleAnim = remember(option.id) { Animatable(1f) }
    LaunchedEffect(targetScale) {
        scaleAnim.animateTo(
            targetValue = targetScale,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }
    val scale = scaleAnim.value

    Surface(
        modifier = modifier
            .height(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
                text = option.name,
                color = contentColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 结果对话框
 */
@Composable
fun ResultDialog(
    option: WheelOption,
    onDismiss: () -> Unit,
    onSpinAgain: () -> Unit
) {
    val optionTextColor = remember(option.color) {
        ColorGenerator.contentColorFor(option.color)
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.82f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(32.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.result_celebration),
                        fontSize = 64.sp
                    )

                    Text(
                        text = stringResource(R.string.result_is),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = option.color,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = optionTextColor,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.spin_again_action))
                        }
                    }
                }
            }
        }
    }
}
