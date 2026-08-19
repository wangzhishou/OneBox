package com.wanbaohe.dsh.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.wire.model.ModelProviderGroup
import com.wanbaohe.dsh.wire.model.ModelReasoningEffort
import com.wanbaohe.dsh.wire.model.ModelSelection
import com.wanbaohe.dsh.wire.model.SessionModelsValue
import com.wanbaohe.dsh.wire.model.catalogEntryOf

/**
 * 模型选择器(对齐 Flutter model_picker.dart):
 * session.models 拉目录(provider 分组 + 当前选择 + routable 警示),
 * reasoningEffort 档位以 FilterChip 选择;「应用」走 session.selectModel。
 * 选择可与目录成员无关(服务端语义);routable=false 只警示不拦截
 * (prompt 前不可路由服务端会 model-unavailable)。
 */
@Composable
fun ModelPickerDialog(
    sessionId: String,
    component: DshRootComponent,
    onDismiss: () -> Unit
) {
    var catalog by remember { mutableStateOf<SessionModelsValue?>(null) }
    var failed by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<ModelSelection?>(null) }
    var effort by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionId) {
        val loaded = component.loadModelCatalog(sessionId)
        if (loaded == null) {
            failed = true
        } else {
            catalog = loaded
            picked = loaded.current
            // 推理力度初值:当前值 → 主机默认 → 首档
            val reasoning = loaded.catalogEntryOf(loaded.current)?.reasoning
            effort = loaded.current.reasoningEffort
                ?: reasoning?.defaultEffort
                ?: reasoning?.efforts?.firstOrNull()?.id
        }
    }

    val pickedEfforts = catalog?.let { c ->
        picked?.let { c.catalogEntryOf(it)?.reasoning?.efforts }
    }.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_model_picker)) },
        text = {
            when {
                failed -> Text(stringResource(R.string.dsh_model_load_failed))
                catalog == null -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }

                else -> ModelCatalogContent(
                    catalog = catalog!!,
                    picked = picked,
                    effort = effort,
                    efforts = pickedEfforts,
                    onPick = { selection ->
                        picked = selection
                        // 换模型后重定力度档:主机默认 → 首档
                        val reasoning = catalog!!.catalogEntryOf(selection)?.reasoning
                        effort = reasoning?.defaultEffort ?: reasoning?.efforts?.firstOrNull()?.id
                    },
                    onEffortChange = { effort = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selection = picked ?: return@Button
                    component.applyModelSelection(
                        sessionId = sessionId,
                        provider = selection.provider,
                        model = selection.model,
                        reasoningEffort = if (pickedEfforts.isEmpty()) null else effort
                    )
                    onDismiss()
                },
                enabled = picked != null && catalog != null
            ) {
                Text(stringResource(R.string.dsh_model_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 目录主体:routable 警示 + provider 分组(默认展开当前选择所在组)+ 力度档 */
@Composable
private fun ModelCatalogContent(
    catalog: SessionModelsValue,
    picked: ModelSelection?,
    effort: String?,
    efforts: List<ModelReasoningEffort>,
    onPick: (ModelSelection) -> Unit,
    onEffortChange: (String?) -> Unit
) {
    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
        if (!catalog.routable) {
            item(key = "routable_warning") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = stringResource(R.string.dsh_model_routable_warning),
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        for (group in catalog.groups) {
            item(key = "group_${group.id}") {
                ModelGroupSection(
                    group = group,
                    picked = picked,
                    initiallyExpanded = picked?.provider == group.id,
                    onPick = onPick
                )
            }
        }
        if (picked != null && efforts.isNotEmpty()) {
            item(key = "efforts") {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(R.string.dsh_model_reasoning_effort),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (option in efforts) {
                            FilterChip(
                                selected = effort == option.id,
                                onClick = { onEffortChange(option.id) },
                                label = { Text(option.name, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** provider 分组:头部可展开/收起(默认只展开当前选择所在组),模型单选行 */
@Composable
private fun ModelGroupSection(
    group: ModelProviderGroup,
    picked: ModelSelection?,
    initiallyExpanded: Boolean,
    onPick: (ModelSelection) -> Unit
) {
    var expanded by remember(group.id) { mutableStateOf(initiallyExpanded) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (expanded) "▾" else "▸",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            for (model in group.models) {
                val selected = picked != null &&
                    picked.provider == group.id && picked.model == model.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(ModelSelection(provider = group.id, model = model.id)) }
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(text = model.name, fontSize = 13.sp)
                        Text(
                            text = model.id,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
