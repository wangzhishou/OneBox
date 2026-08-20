package com.wanbaohe.dsh.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.wire.model.GoalProjection
import com.wanbaohe.dsh.wire.model.GoalRef
import com.wanbaohe.dsh.wire.model.parseGoalProjection
import kotlinx.serialization.json.JsonObject

/**
 * goal 面板(对齐 Flutter goal_skill_widgets.dart 的 GoalPanel):
 * 数据源 = 会话 "goal" 投影({goal:{id,revision,phase,objective,…}, roundsStarted}),
 * 动作走 goal 六方法(create/edit/pause/resume/complete/clear,ref = id+revision CAS)。
 * 有目标时随投影常驻;无目标时由顶栏入口打开,只露「新建目标」。
 */
@Composable
fun GoalPanel(
    projectionValue: JsonObject?,
    busy: Boolean,
    component: DshRootComponent,
    modifier: Modifier = Modifier
) {
    val projection = parseGoalProjection(projectionValue)
    var editorMode by remember { mutableStateOf<GoalEditorMode?>(null) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = goalPhaseColor(projection?.phase),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.dsh_goal_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                projection?.phaseLabelRes()?.let { labelRes ->
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(labelRes),
                        fontSize = 10.sp,
                        color = goalPhaseColor(projection.phase)
                    )
                }
                projection?.roundsStarted?.let { rounds ->
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.dsh_goal_rounds, rounds),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(12.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            projection?.objective?.takeIf { it.isNotEmpty() }?.let { objective ->
                Text(
                    text = objective,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val ref = projection?.ref
                if (ref == null) {
                    AssistChip(
                        onClick = { editorMode = GoalEditorMode.Create },
                        label = { Text(stringResource(R.string.dsh_goal_create), fontSize = 12.sp) }
                    )
                } else {
                    AssistChip(
                        onClick = { editorMode = GoalEditorMode.Edit(ref) },
                        label = { Text(stringResource(R.string.dsh_goal_edit), fontSize = 12.sp) }
                    )
                    when (projection.phase) {
                        "active" -> AssistChip(
                            onClick = { component.goalPause(ref) },
                            label = { Text(stringResource(R.string.dsh_goal_pause), fontSize = 12.sp) }
                        )

                        "paused" -> AssistChip(
                            onClick = { component.goalResume(ref) },
                            label = { Text(stringResource(R.string.dsh_goal_resume), fontSize = 12.sp) }
                        )
                    }
                    if (projection.phase != "complete") {
                        AssistChip(
                            onClick = { component.goalComplete(ref) },
                            label = { Text(stringResource(R.string.dsh_goal_complete), fontSize = 12.sp) }
                        )
                    }
                    AssistChip(
                        onClick = { component.goalClear(ref) },
                        label = { Text(stringResource(R.string.dsh_goal_clear), fontSize = 12.sp) }
                    )
                }
            }
        }
    }

    editorMode?.let { mode ->
        GoalEditorDialog(
            mode = mode,
            initialObjective = projection?.objective.orEmpty(),
            onDismiss = { editorMode = null },
            onSubmit = { objective, maxRounds ->
                when (mode) {
                    is GoalEditorMode.Create -> component.goalCreate(objective, maxRounds)
                    is GoalEditorMode.Edit -> component.goalEdit(mode.ref, objective, maxRounds)
                }
                editorMode = null
            }
        )
    }
}

/** goal 编辑器形态:新建 / 编辑(CAS ref) */
private sealed class GoalEditorMode {
    data object Create : GoalEditorMode()
    data class Edit(val ref: GoalRef) : GoalEditorMode()
}

/** 新建/编辑目标对话框:objective + 可选最大轮次 */
@Composable
private fun GoalEditorDialog(
    mode: GoalEditorMode,
    initialObjective: String,
    onDismiss: () -> Unit,
    onSubmit: (objective: String, maxRounds: Int?) -> Unit
) {
    var objective by remember(mode) { mutableStateOf(initialObjective) }
    var maxRounds by remember(mode) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    when (mode) {
                        is GoalEditorMode.Create -> R.string.dsh_goal_create
                        is GoalEditorMode.Edit -> R.string.dsh_goal_edit
                    }
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassOutlinedTextField(
                    value = objective,
                    onValueChange = { objective = it },
                    label = { Text(stringResource(R.string.dsh_goal_objective_hint)) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassOutlinedTextField(
                    value = maxRounds,
                    onValueChange = { input -> maxRounds = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.dsh_goal_max_rounds_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(objective.trim(), maxRounds.toIntOrNull()) },
                enabled = objective.isNotBlank()
            ) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 阶段文案资源(active/paused/blocked/complete;未知阶段返回 null) */
private fun GoalProjection.phaseLabelRes(): Int? = when (phase) {
    "active" -> R.string.dsh_goal_phase_active
    "paused" -> R.string.dsh_goal_phase_paused
    "blocked" -> R.string.dsh_goal_phase_blocked
    "complete" -> R.string.dsh_goal_phase_complete
    else -> null
}

/** 阶段配色(active 主色 / paused 橙 / blocked 错误色 / complete 绿) */
@Composable
private fun goalPhaseColor(phase: String?) = when (phase) {
    "active" -> MaterialTheme.colorScheme.primary
    "paused" -> MaterialTheme.colorScheme.tertiary
    "blocked" -> MaterialTheme.colorScheme.error
    "complete" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
}
