/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalLayoutApi::class)

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R
import com.shifenmiao.online.component.NoteAiAction
import com.shifenmiao.online.component.NoteAiApplyMode
import com.shifenmiao.online.component.NoteAiUiState
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.delay

/**
 * 笔记 AI 写作面板。
 *
 * 面板只负责收集「动作 + 引擎 + 提示词」并展示流式结果；请求本身由
 * [com.shifenmiao.online.component.CreateNoteComponent] 持有，配合 LongTaskCenter 前台保活，
 * 退到后台再回来结果仍在。结果写回编辑器由调用方决定（替换/追加/插入）。
 */
@Composable
fun NoteAiRewriteSheet(
    state: NoteAiUiState,
    currentEngine: AiEngine?,
    onActionChange: (NoteAiAction) -> Unit,
    onPromptChange: (String) -> Unit,
    onSwitchEngine: () -> Unit,
    onGenerate: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    var copied by remember(state.result) { mutableStateOf(false) }
    val generate = {
        focusManager.clearFocus()
        onGenerate()
    }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    EnhancedModalBottomSheet(
        visible = state.visible,
        onDismiss = { visible -> if (!visible) onDismiss() },
        title = stringResource(state.action.labelRes),
        nestedScrollEnabled = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NoteAiActionChips(
                selected = state.action,
                enabled = !state.isGenerating,
                onSelect = onActionChange,
            )

            NoteAiEngineButton(
                engine = currentEngine,
                enabled = !state.isGenerating,
                onClick = onSwitchEngine,
            )

            GlassOutlinedTextField(
                value = state.prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isGenerating,
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { Text(stringResource(R.string.note_ai_prompt_label)) },
                placeholder = {
                    Text(stringResource(R.string.note_ai_prompt_hint))
                },
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                style = GlassStyle.Medium,
            )

            NoteAiOriginalPreview(markdown = state.originalMarkdown)

            NoteAiResultSection(state = state, engine = currentEngine)

            if (canUndo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = OneBoxIcons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.note_ai_applied),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    OneSecondaryButton(
                        text = stringResource(R.string.note_ai_undo_apply),
                        onClick = onUndo,
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    state.isGenerating -> {
                        OneBoxDangerButton(
                            text = stringResource(R.string.note_ai_stop),
                            leadingIcon = OneBoxIcons.Outlined.LineStop,
                            onClick = onStop,
                        )
                    }

                    state.error != null -> {
                        OnePrimaryButton(
                            text = stringResource(R.string.note_ai_regenerate),
                            leadingIcon = OneBoxIcons.Outlined.LineMagic,
                            enabled = currentEngine?.name?.isNotBlank() == true && state.prompt.isNotBlank(),
                            onClick = generate,
                        )
                    }

                    state.result.isNotBlank() -> {
                        OnePrimaryButton(
                            text = stringResource(state.action.applyMode.labelRes),
                            leadingIcon = OneBoxIcons.Outlined.LineMagic,
                            onClick = { onApply(state.result) },
                        )
                        OneSecondaryButton(
                            text = stringResource(R.string.note_ai_regenerate),
                            onClick = generate,
                        )
                        OneSecondaryButton(
                            text = stringResource(
                                if (copied) R.string.note_ai_copied else R.string.note_ai_copy
                            ),
                            leadingIcon = if (copied) OneBoxIcons.Outlined.Check else null,
                            onClick = {
                                clipboard.setText(AnnotatedString(state.result))
                                copied = true
                            },
                        )
                    }

                    else -> {
                        OnePrimaryButton(
                            text = stringResource(R.string.note_ai_panel_generate),
                            leadingIcon = OneBoxIcons.Outlined.LineMagic,
                            enabled = currentEngine?.name?.isNotBlank() == true && state.prompt.isNotBlank(),
                            onClick = generate,
                        )
                        OneSecondaryButton(
                            text = stringResource(R.string.note_ai_copy),
                            enabled = state.result.isNotBlank(),
                            onClick = {
                                clipboard.setText(AnnotatedString(state.result))
                                copied = true
                            },
                        )
                    }
                }
            }
        }
    }
}

private val NoteAiApplyMode.labelRes: Int
    get() = when (this) {
        NoteAiApplyMode.Replace -> R.string.note_ai_apply_replace
        NoteAiApplyMode.Append -> R.string.note_ai_apply_append
        NoteAiApplyMode.Prepend -> R.string.note_ai_apply_prepend
    }

@Composable
private fun NoteAiActionChips(
    selected: NoteAiAction,
    enabled: Boolean,
    onSelect: (NoteAiAction) -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selected) {
        val index = NoteAiAction.entries.indexOf(selected)
        if (index >= 0) listState.animateScrollToItem(index)
    }
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(items = NoteAiAction.entries, key = { it.name }) { action ->
            GlassFilterChip(
                selected = action == selected,
                enabled = enabled,
                onClick = { onSelect(action) },
                label = {
                    Text(
                        text = stringResource(action.labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                },
                style = GlassStyle.Thin,
            )
        }
    }
}

@Composable
private fun NoteAiEngineButton(
    engine: AiEngine?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.note_ai_engine),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OneSecondaryButton(
            text = engine?.let { current ->
                "${current.title.ifBlank { current.name }} · ${current.model.name}"
            } ?: stringResource(R.string.note_ai_no_engine),
            leadingIcon = OneBoxIcons.Outlined.LineRobot,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        )
    }
}

@Composable
private fun NoteAiOriginalPreview(markdown: String) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.note_ai_original_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.note_ai_original_meta, markdown.length),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GlassSurface(style = GlassStyle.Thin, shape = RoundedCornerShape(12.dp)) {
            SelectionContainer {
                Text(
                    text = markdown.ifBlank { "—" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 96.dp)
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NoteAiResultSection(
    state: NoteAiUiState,
    engine: AiEngine?,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.note_ai_result_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (state.result.isNotBlank() && state.tokens > 0) {
                Text(
                    text = stringResource(
                        R.string.note_ai_result_meta,
                        engine?.model?.name.orEmpty(),
                        state.tokens,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (state.isGenerating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        when {
            state.error != null -> {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            state.isGenerating || state.result.isNotBlank() -> {
                GlassSurface(style = GlassStyle.Thin, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        SelectionContainer {
                            Text(
                                text = state.result.ifBlank {
                                    stringResource(R.string.note_ai_panel_generating)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 12,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = stringResource(R.string.note_ai_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
