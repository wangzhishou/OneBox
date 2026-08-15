package com.shifenmiao.ai.execution.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.execution.model.AiExecutionPhase
import com.shifenmiao.ai.execution.model.AiExecutionUiModel
import com.shifenmiao.ai.execution.model.DeepLinkItemUiModel
import com.shifenmiao.ai.execution.model.ExecutionStepStatus
import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCardSegment
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassCardSegment
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHourglass

@Composable
fun AiExecutionTimelineCard(
    uiModel: AiExecutionUiModel,
    onPrimaryAction: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null,
    initiallyExpanded: Boolean = false,
    forceExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!uiModel.isVisible) return

    var expanded by remember(uiModel.title, uiModel.steps, initiallyExpanded, forceExpanded) {
        mutableStateOf(
            forceExpanded ||
                    initiallyExpanded ||
                    uiModel.phase == AiExecutionPhase.WAITING_USER_ACTION
        )
    }
    val isExpanded = forceExpanded || expanded
    val expandedSteps = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = uiModel.steps.size > 1 && !forceExpanded) {
                    if (uiModel.steps.size > 1) {
                        expanded = !expanded
                        onExpandedChange?.invoke(expanded)
                    }
                }
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeaderStatusIcon(phase = uiModel.phase)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiModel.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        uiModel.summary?.takeIf { it.isNotBlank() }?.let {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        uiModel.progressText?.takeIf { it.isNotBlank() }?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (uiModel.steps.size > 1) {
                        Icon(
                            imageVector = if (isExpanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded && uiModel.steps.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        uiModel.steps.forEachIndexed { index, step ->
                            AiExecutionStepRow(
                                step = step,
                                showConnector = index != uiModel.steps.lastIndex,
                                isStepExpanded = expandedSteps[step.id] ?: false,
                                onStepToggle = {
                                    expandedSteps[step.id] = !(expandedSteps[step.id] ?: false)
                                },
                            )
                        }
                    }
                }

                if (uiModel.primaryActionLabel != null || uiModel.secondaryActionLabel != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiModel.secondaryActionLabel?.let { label ->
                            TextButton(onClick = { onSecondaryAction?.invoke() }) {
                                Text(text = label)
                            }
                        }
                        uiModel.primaryActionLabel?.let { label ->
                            FilledTonalButton(
                                onClick = { onPrimaryAction?.invoke() },
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = label)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderStatusIcon(phase: AiExecutionPhase) {
    when (phase) {
        AiExecutionPhase.RUNNING,
        AiExecutionPhase.WAITING_FINAL_RESPONSE -> {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        AiExecutionPhase.COMPLETED -> {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
        }

        AiExecutionPhase.FAILED,
        AiExecutionPhase.PAUSED -> {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }

        AiExecutionPhase.WAITING_USER_ACTION -> {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHourglass,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        AiExecutionPhase.HIDDEN -> Unit
    }
}

@Composable
private fun AiExecutionStepRow(
    step: ExecutionStepUiModel,
    showConnector: Boolean,
    isStepExpanded: Boolean,
    onStepToggle: () -> Unit,
) {
    val hasDetailContent = !step.arguments.isNullOrBlank() || !step.result.isNullOrBlank()
    val debugInfo = step.debugInfo
    val debugInfoForRender = if (BuildConfig.DEBUG) debugInfo else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepStatusNode(status = step.status)
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            when (step.status) {
                                ExecutionStepStatus.DONE,
                                ExecutionStepStatus.RUNNING,
                                ExecutionStepStatus.WAITING_USER -> MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.32f
                                )

                                ExecutionStepStatus.FAILED -> MaterialTheme.colorScheme.error.copy(
                                    alpha = 0.32f
                                )

                                ExecutionStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.16f
                                )
                            }
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (showConnector) 4.dp else 0.dp)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (step.status == ExecutionStepStatus.RUNNING) FontWeight.SemiBold else FontWeight.Medium,
                color = stepTextColor(step.status)
            )
            step.subtitle?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (step.status == ExecutionStepStatus.FAILED && !step.detail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (hasDetailContent || debugInfoForRender != null) {
                Spacer(modifier = Modifier.height(if (step.status == ExecutionStepStatus.FAILED) 4.dp else 6.dp))
                ToolCallDetailPanel(
                    arguments = step.arguments,
                    result = step.result,
                    debugInfo = debugInfoForRender,
                    isExpanded = isStepExpanded,
                    onToggle = onStepToggle,
                )
            }
            if (step.deepLinks.isNotEmpty() && step.status != ExecutionStepStatus.FAILED) {
                Spacer(modifier = Modifier.height(6.dp))
                DeepLinkActionList(items = step.deepLinks)
            }
        }
    }
}

/**
 * 工具执行后的可点击 deep link 卡片列表。
 *
 * 一次工具调用可能产生多个 deep link（例如"打开刚创建的笔记" + "返回笔记列表"），
 * 按 [DeepLinkItemUiModel.primary] 排序：primary 项排在前且视觉强调（icon + 文字色）。
 */
@Composable
private fun DeepLinkActionList(items: List<DeepLinkItemUiModel>) {
    val sorted = remember(items) {
        items.sortedWith(compareByDescending<DeepLinkItemUiModel> { it.primary }.thenBy { it.label })
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        sorted.forEach { item ->
            DeepLinkActionCard(item = item)
        }
    }
}

@Composable
private fun DeepLinkActionCard(item: DeepLinkItemUiModel) {
    val urlNavigator = LocalUrlNavigator.current
    val primary = item.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .glassBackground(
                color = if (primary) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(12.dp),
                borderWidth = if (primary) 0.8.dp else 0.dp,
            )
            .clickable {
                urlNavigator.navigate(item.uri)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = if (primary) Icons.Outlined.Link else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
            contentDescription = null,
            tint = if (primary) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(18.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Medium,
                color = if (primary) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.guidance?.takeIf { it.isNotBlank() }?.let { guidance ->
                Text(
                    text = guidance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
            contentDescription = null,
            tint = if (primary) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun ToolCallDetailPanel(
    arguments: String?,
    result: String?,
    debugInfo: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val hasArguments = !arguments.isNullOrBlank()
    val hasResult = !result.isNullOrBlank()
    val hasDebug = !debugInfo.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                borderWidth = 0.dp
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
            Text(
                text = stringResource(R.string.ai_execution_detail_collapsed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(8.dp)
            )
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            if (hasArguments) {
                ToolCallDetailBlock(
                    label = stringResource(R.string.ai_execution_input_label),
                    color = MaterialTheme.colorScheme.tertiary,
                    text = arguments.orEmpty(),
                )
            }
            if (hasResult) {
                if (hasArguments) Spacer(modifier = Modifier.height(8.dp))
                ToolCallDetailBlock(
                    label = stringResource(R.string.ai_execution_output_label),
                    color = MaterialTheme.colorScheme.primary,
                    text = result.orEmpty(),
                )
            }
            if (hasDebug) {
                if (hasArguments || hasResult) Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = debugInfo.orEmpty(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun ToolCallDetailBlock(
    label: String,
    color: Color,
    text: String,
) {
    val isJson = text.trimStart().startsWith("{") || text.trimStart().startsWith("[")
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .glassBackground(
                style = GlassStyle.Dense,
                color = color,
                shape = RoundedCornerShape(10.dp),
                borderWidth = 0.dp
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            val copyDescription = stringResource(R.string.copy)
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                contentDescription = copyDescription,
                tint = color.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .clickable {
                        Clipboard.copy(text)
                    }
                    .padding(2.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .horizontalScroll(scrollState)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (isJson) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "JSON",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun StepStatusNode(status: ExecutionStepStatus) {
    when (status) {
        ExecutionStepStatus.DONE -> Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )

        ExecutionStepStatus.RUNNING -> CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )

        ExecutionStepStatus.WAITING_USER -> Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHourglass,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        ExecutionStepStatus.FAILED -> Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )

        ExecutionStepStatus.PENDING -> Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))
        )
    }
}

@Composable
private fun stepTextColor(status: ExecutionStepStatus): Color {
    return when (status) {
        ExecutionStepStatus.DONE -> MaterialTheme.colorScheme.onSurface
        ExecutionStepStatus.RUNNING -> MaterialTheme.colorScheme.onSurface
        ExecutionStepStatus.WAITING_USER -> MaterialTheme.colorScheme.onSurface
        ExecutionStepStatus.FAILED -> MaterialTheme.colorScheme.error
        ExecutionStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
