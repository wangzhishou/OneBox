package com.wanbaohe.iching.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.node.AstNode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCircularProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.iching.R
import com.wanbaohe.iching.component.CastingStage
import com.wanbaohe.iching.component.IChingDivinationComponent
import com.wanbaohe.iching.component.IChingPage
import com.wanbaohe.iching.component.IChingUiState
import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramInfo
import com.wanbaohe.iching.model.HexagramLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun IChingDivinationScreen(component: IChingDivinationComponent) {
    val state by component.uiState.collectAsState()
    val title = when (state.page) {
        IChingPage.RESULT -> stringResource(R.string.iching_result_title)
        IChingPage.CAST -> when (state.stage) {
            is CastingStage.Casting -> stringResource(R.string.iching_casting_title)
            else -> stringResource(R.string.iching_cast_title)
        }
    }

    BaseScreen(
        title = title,
        onGoBack = component::back,
        actions = {
            IconButton(
                onClick = component::navigateToHistory,
                enabled = state.stage !is CastingStage.Casting,
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
            is CastingStage.Casting -> CastingContent(stage.completed)
            is CastingStage.Error -> ErrorContent(stage.message, onCast)
            is CastingStage.Success -> CastingContent(completed = 6)
        }
    }
}

@Composable
private fun CastForm(question: String, onQuestionChange: (String) -> Unit, onCast: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(180.dp))
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
                glassColor = MaterialTheme.colorScheme.surfaceContainer,
                glassBorderWidth = 0.8.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PrimaryButton(text = stringResource(R.string.iching_cast_button), onClick = onCast)
    }
}

@Composable
private fun CastingContent(completed: Int) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(96.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            GlassSurface(
                style = GlassStyle.Thin,
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                borderWidth = 0.8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    repeat(6) { index ->
                        LoadingLine(active = index >= 6 - completed)
                    }
                }
            }
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
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCircularProgressIndicator(
                progress = { completed / 6f },
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
            Text(stringResource(R.string.iching_casting_status), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingLine(active: Boolean) {
    GlassSurface(
        modifier = Modifier.width(150.dp).height(9.dp).alpha(if (active) 1f else 0.18f),
        style = if (active) GlassStyle.Medium else GlassStyle.Thin,
        shape = RoundedCornerShape(99.dp),
        color = MaterialTheme.colorScheme.primary,
        borderWidth = 0.4.dp,
    ) {}
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
        PrimaryButton(stringResource(R.string.iching_retry), onRetry)
    }
}

@Composable
private fun ResultContent(
    state: IChingUiState,
    result: DivinationResult,
    onGenerateAI: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        QuestionCard(result.question)
        HexagramCard(result.primary, result.lines)
        result.changed?.let { changed ->
            Text(
                text = stringResource(R.string.iching_changing_lines, result.changingLineNumbers.joinToString("、")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            HexagramSummary(title = stringResource(R.string.iching_changed_hexagram), info = changed)
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
            Text(stringResource(R.string.iching_new_cast))
        }
    }
}

@Composable
private fun QuestionCard(question: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
            when {
                state.isGeneratingAI -> {
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
                }
                else -> IChingMarkdownText(
                    content = state.aiContent,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.iching_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Markdown 渲染(AI 解读):解析挂默认调度器,解析中/失败回落纯文本 */
@Composable
private fun IChingMarkdownText(content: String, color: Color) {
    val context = LocalContext.current
    val parser = remember { MarkdownAstNodeParser(context, CommonMarkdownParseOptions.Default) }
    val ast by produceState<AstNode?>(initialValue = null, parser, content) {
        value = withContext(Dispatchers.Default) {
            runCatching { parser.parse(content) }.getOrNull()
        }
    }
    val node = ast
    if (node != null) {
        RichText(
            contentColor = color,
            textStyle = MaterialTheme.typography.bodyMedium,
        ) {
            BasicMarkdown(astNode = node)
        }
    } else {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 24.sp,
            color = color,
        )
    }
}

@Composable
private fun HexagramCard(info: HexagramInfo, lines: List<HexagramLine>) {
    GlassCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        containerAlpha = 0.18f,
        borderWidth = 0.9.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.iching_hexagram_number, info.number), color = MaterialTheme.colorScheme.onSurfaceVariant)
            HexagramLines(lines)
            Text(info.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.iching_upper_trigram, info.upperTrigram))
            Text(stringResource(R.string.iching_lower_trigram, info.lowerTrigram))
        }
    }
}

@Composable
private fun HexagramSummary(title: String, info: HexagramInfo) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        containerAlpha = 0.16f,
        borderWidth = 0.7.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(info.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${info.upperTrigram} · ${info.lowerTrigram}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HexagramLines(lines: List<HexagramLine>) {
    GlassSurface(
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        borderWidth = 0.7.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            lines.asReversed().forEach { line -> HexagramLineView(line) }
        }
    }
}

@Composable
private fun HexagramLineView(line: HexagramLine) {
    val color = if (line.isChanging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.width(150.dp), horizontalArrangement = Arrangement.Center) {
        if (line.isYang) {
            GlassLineSegment(Modifier.fillMaxWidth(), color)
        } else {
            GlassLineSegment(Modifier.weight(1f), color)
            Spacer(Modifier.width(18.dp))
            GlassLineSegment(Modifier.weight(1f), color)
        }
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
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    GlassButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerAlpha = 0.58f,
        borderWidth = 0.8.dp,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
