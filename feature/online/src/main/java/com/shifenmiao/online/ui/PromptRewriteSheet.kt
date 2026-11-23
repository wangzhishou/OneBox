package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R
import com.shifenmiao.online.component.PromptRewriteState
import com.shifenmiao.online.service.PromptRewriteAction
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton

@Composable
internal fun PromptRewriteSheet(
    state: PromptRewriteState,
    engine: AiEngine?,
    onRequirementChange: (String) -> Unit,
    onSwitchEngine: () -> Unit,
    onGenerate: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val focus = LocalFocusManager.current
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        title = stringResource(state.action.label),
        modifier = Modifier.imePadding(),
        nestedScrollEnabled = true,
        endConfirmButtonPadding = 0.dp,
        confirmButton = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!state.running && state.candidate.isNotBlank()) {
                    OnePrimaryButton(stringResource(R.string.prompt_ai_apply), onClick = onApply)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AiEditorEngineSwitcher(
                        engine = engine,
                        enabled = !state.running,
                        onClick = onSwitchEngine,
                        modifier = Modifier.weight(1f, fill = false),
                        compact = true,
                    )
                    when {
                        state.running -> OneSecondaryButton(stringResource(R.string.note_ai_stop), onClick = onStop)
                        state.candidate.isNotBlank() -> OneSecondaryButton(
                            text = stringResource(R.string.note_ai_regenerate),
                            leadingIcon = OneBoxIcons.Outlined.LineMagic,
                            onClick = onGenerate,
                        )
                        else -> OnePrimaryButton(
                            text = stringResource(R.string.note_ai_panel_generate),
                            modifier = Modifier.heightIn(min = 44.dp),
                            leadingIcon = OneBoxIcons.Outlined.LineMagic,
                            enabled = state.input.isNotBlank() && engine?.name?.isNotBlank() == true,
                            onClick = {
                                focus.clearFocus()
                                onGenerate()
                            },
                        )
                    }
                }
            }
        },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.dimens.pageHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.action == PromptRewriteAction.Create) {
                GlassOutlinedTextField(
                    value = state.requirement,
                    onValueChange = onRequirementChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.running,
                    label = { Text(stringResource(R.string.prompt_ai_goal)) },
                    minLines = 3,
                    maxLines = 6,
                )
            }
            if (state.running) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            RewriteComparison(state.original, state.candidate)
        }
    }
}

@Composable
internal fun RewriteComparison(original: String, suggestion: String) {
    if (original.isBlank() && suggestion.isBlank()) return
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 600.dp && original.isNotBlank() && suggestion.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RewritePreview(stringResource(R.string.note_ai_original_label), original, Modifier.weight(1f))
                RewritePreview(stringResource(R.string.note_ai_result_label), suggestion, Modifier.weight(1f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (original.isNotBlank()) RewritePreview(stringResource(R.string.note_ai_original_label), original)
                if (suggestion.isNotBlank()) RewritePreview(stringResource(R.string.note_ai_result_label), suggestion)
            }
        }
    }
}

@Composable
private fun RewritePreview(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        GlassSurface(style = GlassStyle.Thin) {
            SelectionContainer {
                Text(body, Modifier.fillMaxWidth().padding(AppTheme.dimens.cardContentPadding),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
