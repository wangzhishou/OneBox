package com.wanbaohe.iching.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.StreamingMarkdownContent
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.VolumeOff
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCircularProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.text.ReasoningCollapseSection
import com.wanbaohe.iching.R
import com.wanbaohe.iching.component.CastingStage
import com.wanbaohe.iching.component.IChingDivinationComponent
import com.wanbaohe.iching.component.IChingPage
import com.wanbaohe.iching.component.IChingUiState
import com.wanbaohe.iching.domain.HexagramText
import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramInfo
import com.wanbaohe.iching.model.HexagramLine
import com.wanbaohe.iching.ui.icons.CoinBackRipple
import com.wanbaohe.iching.ui.icons.CoinFrontQian
import com.wanbaohe.iching.ui.icons.CoinFrontLi
import com.wanbaohe.iching.ui.icons.CoinFrontKun

/** 三枚铜钱的正面(字)图标,依次 乾/坤/離 */
private val CoinFronts = listOf(CoinFrontQian, CoinFrontKun, CoinFrontLi)

@Composable
fun IChingDivinationScreen(component: IChingDivinationComponent) {
    val state by component.uiState.collectAsState()
    val soundEnabled by component.soundEnabled.collectAsState()
    val title = when (state.page) {
        IChingPage.RESULT -> stringResource(R.string.iching_result_title)
        IChingPage.CAST -> when (state.stage) {
            is CastingStage.Tossing, is CastingStage.Casting -> stringResource(R.string.iching_casting_title)
            else -> stringResource(R.string.iching_cast_title)
        }
    }

    BaseScreen(
        title = title,
        onGoBack = component::back,
        actions = {
            IconButton(onClick = component::toggleSound) {
                Icon(
                    imageVector = if (soundEnabled) {
                        Icons.Outlined.VolumeUp
                    } else {
                        Icons.Outlined.VolumeOff
                    },
                    contentDescription = stringResource(
                        if (soundEnabled) R.string.iching_sound_on else R.string.iching_sound_off
                    ),
                )
            }
            IconButton(
                onClick = component::navigateToHistory,
                enabled = state.stage !is CastingStage.Tossing,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = stringResource(R.string.iching_history_title),
                )
            }
        },
        supportGlassEffect = true,
    ) {
        Crossfade(targetState = state.page, animationSpec = tween(250), label = "iching_page") { page ->
            when (page) {
                IChingPage.CAST -> CastContent(
                    state = state,
                    onQuestionChange = component::setQuestion,
                    onCast = component::startCasting,
                )
                IChingPage.RESULT -> state.result?.let {
                    ResultContent(
                        state = state,
                        result = it,
                        hexagramText = component::hexagramText,
                        trigramName = component::trigramName,
                        onGenerateAI = component::generateAIInterpretation,
                        onReset = component::reset,
                    )
                }
            }
        }
    }
}

@Composable
private fun CastContent(
    state: IChingUiState,
    onQuestionChange: (String) -> Unit,
    onCast: () -> Unit,
) {
    AnimatedContent(targetState = state.stage, label = "casting_stage") { stage ->
        when (stage) {
            CastingStage.Idle -> CastForm(state.question, onQuestionChange, onCast)
            is CastingStage.Tossing -> TossingContent(completed = stage.completed)
            is CastingStage.Casting -> LandedContent(lines = state.lines, onContinue = onCast)
            is CastingStage.Error -> ErrorContent(stage.message, onCast)
            is CastingStage.Success -> LandedContent(lines = state.lines, onContinue = {})
        }
    }
}

@Composable
private fun CoinImage(icon: ImageVector, modifier: Modifier = Modifier, size: Dp = 80.dp) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(size),
    )
}

@Composable
private fun CastForm(question: String, onQuestionChange: (String) -> Unit, onCast: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        CoinTripleRow(
            modifier = Modifier.padding(top = 56.dp),
            coinSize = 112.dp,
            spacing = 24.dp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.iching_question_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            GlassOutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                placeholder = { Text(stringResource(R.string.iching_question_hint)) },
                supportingText = { Text("${question.length}/100", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                minLines = 4,
                maxLines = 5,
                shape = RoundedCornerShape(20.dp),
                style = GlassStyle.Medium,
                glassColor = MaterialTheme.colorScheme.surfaceContainerLow,
                glassBorderWidth = 0.8.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PrimaryButton(
            text = stringResource(R.string.iching_cast_button),
            onClick = onCast,
            icon = CoinBackRipple,
        )
    }
}

/** 摇卦中:三枚铜钱在空中翻转下落,下方淡色椭圆承接 */
@Composable
private fun TossingContent(completed: Int) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 三枚铜钱共用一个动画时钟:任意时刻姿态完全一致,不会因错相位显得大小不一
            val transition = rememberInfiniteTransition(label = "coin_toss")
            val rotation by transition.animateFloat(
                initialValue = -28f,
                targetValue = 28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(360, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "coin_rotation",
            )
            val offsetY by transition.animateFloat(
                initialValue = -34f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "coin_offset",
            )
            val flip by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(180, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "coin_flip",
            )
            CoinTripleRow(
                coinSize = 96.dp,
                spacing = 20.dp,
                rotation = rotation,
                offsetY = offsetY,
                flip = flip,
            )
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(32.dp)
                    .alpha(0.35f)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), CircleShape)
            )
            Text(
                text = stringResource(R.string.iching_casting_progress, completed),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.iching_casting_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            KnowledgeTipCard(tipIndex = completed)
        }
        PrimaryButton(
            text = stringResource(R.string.iching_casting_status),
            onClick = {},
            enabled = false,
            icon = CoinBackRipple,
        )
    }
}

/**
 * 三枚铜钱一排:窄屏按比例等比缩小,保证任何宽度下三枚等大且不溢出。
 * [rotation]/[offsetY]/[flip] 为摇卦动画值,静止展示时用默认值即可。
 */
@Composable
private fun CoinTripleRow(
    coinSize: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    offsetY: Float = 0f,
    flip: Float = 1f,
) {
    // Row 包裹内容宽度,Box 默认 TopStart 会让整排靠左,必须显式居中
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val fittedSize = ((maxWidth - spacing * 2) / 3).coerceAtMost(coinSize)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)) {
            CoinFronts.forEach {
                CoinImage(
                    icon = it,
                    size = fittedSize,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotation
                        translationY = offsetY.dp.toPx()
                        scaleY = flip
                    },
                )
            }
        }
    }
}

/** 铜钱落地:三角落定(字/背面与爻值对应),下方自下而上累积已出爻 */
@Composable
private fun LandedContent(lines: List<HexagramLine>, onContinue: () -> Unit) {
    val last = lines.lastOrNull()
    // 铜钱起卦惯例:字(正面)=3、背=2,三枚之和即爻值 → 正面数 = 爻值 - 6
    val frontCount = (last?.value ?: 0) - 6
    val newestAlpha = remember { Animatable(1f) }
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            newestAlpha.snapTo(0f)
            newestAlpha.animateTo(1f, tween(400))
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CoinImage(icon = if (frontCount >= 1) CoinFronts[0] else CoinBackRipple, size = 84.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    CoinImage(icon = if (frontCount >= 2) CoinFronts[1] else CoinBackRipple, size = 84.dp)
                    CoinImage(icon = if (frontCount >= 3) CoinFronts[2] else CoinBackRipple, size = 84.dp)
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                lines.asReversed().forEachIndexed { index, line ->
                    Box(if (index == 0) Modifier.alpha(newestAlpha.value) else Modifier) {
                        HexagramLineView(line)
                    }
                }
            }
            Text(
                text = stringResource(R.string.iching_casting_progress, lines.size),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            last?.let { LineExplanationCard(line = it, position = lines.size) }
        }
        PrimaryButton(
            text = stringResource(R.string.iching_continue_cast),
            onClick = onContinue,
            icon = CoinBackRipple,
        )
    }
}

/** 本爻解释卡片:爻位 + 铜钱组合 + 老少阴阳 + 爻象 + 是否动爻 */
@Composable
private fun LineExplanationCard(line: HexagramLine, position: Int) {
    val positionName = stringResource(
        when (position) {
            1 -> R.string.iching_line_pos_1
            2 -> R.string.iching_line_pos_2
            3 -> R.string.iching_line_pos_3
            4 -> R.string.iching_line_pos_4
            5 -> R.string.iching_line_pos_5
            else -> R.string.iching_line_pos_6
        }
    )
    val (combo, name) = when (line.value) {
        6 -> R.string.iching_combo_6 to R.string.iching_line_name_6
        7 -> R.string.iching_combo_7 to R.string.iching_line_name_7
        8 -> R.string.iching_combo_8 to R.string.iching_line_name_8
        else -> R.string.iching_combo_9 to R.string.iching_line_name_9
    }
    val symbol = stringResource(if (line.isYang) R.string.iching_line_yang else R.string.iching_line_yin)
    val changing = stringResource(if (line.isChanging) R.string.iching_line_changing else R.string.iching_line_steady)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.14f,
        borderWidth = 0.6.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.iching_line_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(
                    R.string.iching_line_summary,
                    positionName,
                    stringResource(combo),
                    stringResource(name),
                    symbol,
                    changing,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** 易经小知识卡片:按已出爻数轮换展示铜钱摇卦法知识 */
@Composable
private fun KnowledgeTipCard(tipIndex: Int) {
    val tips = listOf(
        stringResource(R.string.iching_tip_1),
        stringResource(R.string.iching_tip_2),
        stringResource(R.string.iching_tip_3),
        stringResource(R.string.iching_tip_4),
        stringResource(R.string.iching_tip_5),
        stringResource(R.string.iching_tip_6),
    )
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.14f,
        borderWidth = 0.6.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.iching_tip_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Crossfade(targetState = tips[tipIndex % tips.size], label = "iching_tip") { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(180.dp))
        Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        PrimaryButton(
            text = stringResource(R.string.iching_retry),
            onClick = onRetry,
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
        )
    }
}

@Composable
private fun ResultContent(
    state: IChingUiState,
    result: DivinationResult,
    hexagramText: (Int) -> HexagramText,
    trigramName: (Int) -> String,
    onGenerateAI: () -> Unit,
    onReset: () -> Unit,
) {
    val primaryText = remember(result.primary.number) { hexagramText(result.primary.number) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        QuestionCard(result.question)
        HexagramCard(
            label = stringResource(R.string.iching_hexagram_number, result.primary.number),
            info = result.primary,
            lines = result.lines,
            name = primaryText.name,
            trigramName = trigramName,
        )
        JudgmentCard(primaryText = primaryText, changingLineNumbers = result.changingLineNumbers)
        result.changed?.let { changed ->
            Text(
                text = stringResource(R.string.iching_changing_lines, result.changingLineNumbers.joinToString("、")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            HexagramCard(
                label = stringResource(R.string.iching_changed_hexagram),
                info = changed,
                lines = result.changedLines,
                name = remember(changed.number) { hexagramText(changed.number) }.name,
                trigramName = trigramName,
            )
        } ?: Text(
            stringResource(R.string.iching_no_changing_lines),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AIInterpretationSection(state = state, onGenerate = onGenerateAI)
        GlassTonalButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestartAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.iching_new_cast))
        }
    }
}

/** 卦辞 + 动爻爻辞卡片:内置原文,离线可看;无动爻时只有卦辞 */
@Composable
private fun JudgmentCard(primaryText: HexagramText, changingLineNumbers: List<Int>) {
    if (primaryText.judgment.isBlank()) return
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.16f,
        borderWidth = 0.7.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.iching_judgment_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = primaryText.judgment, style = MaterialTheme.typography.bodyMedium)
            changingLineNumbers.forEach { position ->
                val lineText = primaryText.lines.getOrNull(position - 1)
                if (!lineText.isNullOrBlank()) {
                    Text(
                        text = "${linePositionName(position)} · $lineText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** 爻位名(初爻…上爻),卦辞卡片与摇卦解释卡片共用 */
@Composable
private fun linePositionName(position: Int): String = stringResource(
    when (position) {
        1 -> R.string.iching_line_pos_1
        2 -> R.string.iching_line_pos_2
        3 -> R.string.iching_line_pos_3
        4 -> R.string.iching_line_pos_4
        5 -> R.string.iching_line_pos_5
        else -> R.string.iching_line_pos_6
    }
)

@Composable
private fun QuestionCard(question: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.16f,
        borderWidth = 0.7.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.iching_question_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = question.ifBlank { stringResource(R.string.iching_history_no_question) },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun AIInterpretationSection(state: IChingUiState, onGenerate: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.16f,
        borderWidth = 0.7.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.iching_ai_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
            // 深度思考内容:折叠区展示(不落库),展开状态与 AI 助手全局一致;
            // 还在思考(正文未开始)时折叠态预览滚动提示
            if (state.aiReasoning.isNotBlank()) {
                ReasoningCollapseSection(
                    reasoning = state.aiReasoning,
                    isStreaming = state.isGeneratingAI && state.aiContent.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            when {
                // 流式生成中已有内容时直接落到下方 Markdown 展示,否则显示加载中
                state.isGeneratingAI && state.aiContent.isBlank() -> {
                    GlassCircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(
                        text = stringResource(R.string.iching_ai_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.aiError != null -> {
                    Text(
                        text = stringResource(R.string.iching_ai_error),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = state.aiError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    GlassButton(onClick = onGenerate) {
                        Text(stringResource(R.string.iching_ai_retry))
                    }
                }
                state.aiContent.isBlank() -> {
                    GlassButton(onClick = onGenerate) {
                        Text(stringResource(R.string.iching_view_ai))
                    }
                    if (state.aiPointsEstimate > 0) {
                        Text(
                            text = stringResource(R.string.iching_ai_points_estimate, state.aiPointsEstimate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    // 流式/完成态统一走通用 Markdown 渲染(块签名复用,流式也不退化为纯文本)
                    StreamingMarkdownContent(
                        content = state.aiContent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(com.shifenmiao.core.R.string.ai_content_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun HexagramCard(
    label: String,
    info: HexagramInfo,
    lines: List<HexagramLine>,
    name: String,
    trigramName: (Int) -> String,
) {
    GlassCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.18f,
        borderWidth = 0.9.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HexagramLines(lines)
            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.iching_upper_trigram, trigramName(info.upperTrigramCode)))
            Text(stringResource(R.string.iching_lower_trigram, trigramName(info.lowerTrigramCode)))
        }
    }
}

@Composable
private fun HexagramLines(lines: List<HexagramLine>) {
    // 无底衬背景,爻线直接落在卡片上
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        lines.asReversed().forEach { line -> HexagramLineView(line) }
    }
}

/** 单爻:阳爻整段/阴爻断段;动爻(老阳/老阴)在右侧加 ○/× 传统标记,不只依赖颜色区分 */
@Composable
private fun HexagramLineView(line: HexagramLine) {
    val color = if (line.isChanging) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer
    Row(modifier = Modifier.width(174.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.width(150.dp), horizontalArrangement = Arrangement.Center) {
            if (line.isYang) {
                GlassLineSegment(Modifier.fillMaxWidth(), color)
            } else {
                GlassLineSegment(Modifier.weight(1f), color)
                Spacer(Modifier.width(18.dp))
                GlassLineSegment(Modifier.weight(1f), color)
            }
        }
        Text(
            text = when {
                line.isChanging && line.isYang -> stringResource(R.string.iching_marker_changing_yang)
                line.isChanging -> stringResource(R.string.iching_marker_changing_yin)
                else -> ""
            },
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp),
        )
    }
}

@Composable
private fun GlassLineSegment(modifier: Modifier, color: Color) {
    GlassSurface(
        modifier = modifier.height(9.dp),
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(99.dp),
        color = color,
        borderWidth = 0.4.dp,
    ) {}
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    GlassButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        containerAlpha = 0.58f,
        borderWidth = 0.8.dp,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
