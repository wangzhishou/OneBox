package com.shifenmiao.online.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.shifenmiao.common.markdown.rememberInAppLinkHandler
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.database.ai.EditorHistoryResult
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R
import com.shifenmiao.online.component.PromptRewriteState
import com.shifenmiao.online.service.PromptRewriteAction
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton

@Composable
internal fun PromptRewriteSheet(
    state: PromptRewriteState,
    engine: AiEngine?,
    onRequirementChange: (String) -> Unit,
    onSwitchEngine: () -> Unit,
    onGenerate: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    onGetPoints: () -> Unit,
    onApply: () -> Unit,
    loadHistory: suspend () -> List<EditorHistoryResult>,
    onHistorySelected: (String) -> Unit,
) {
    val focus = LocalFocusManager.current
    val scrollState = rememberScrollState()
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        title = stringResource(state.action.label),
        modifier = Modifier.imePadding(),
        nestedScrollEnabled = true,
        dragToDismissEnabled = false,
        endConfirmButtonPadding = 0.dp,
        confirmButton = {
            EditorAiActionBar(
                actionKey = state.action,
                engine = engine,
                isGenerating = state.running,
                canGenerate = state.canGenerate,
                hasResult = state.candidate.isNotBlank(),
                onSwitchEngine = onSwitchEngine,
                onGenerate = {
                    focus.clearFocus()
                    onGenerate()
                },
                onStop = onStop,
                loadHistory = loadHistory,
                onHistorySelected = onHistorySelected,
            ) {
                if (!state.running && state.candidate.isNotBlank()) {
                    OnePrimaryButton(
                        text = stringResource(R.string.prompt_ai_apply),
                        leadingIcon = Icons.Outlined.Check,
                        onClick = onApply,
                    )
                }
            }
        },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState)
                .padding(horizontal = AppTheme.dimens.pageHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassOutlinedTextField(
                value = state.requirement,
                onValueChange = onRequirementChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.running,
                label = {
                    Text(
                        stringResource(
                            if (state.action == PromptRewriteAction.Create) {
                                R.string.prompt_ai_goal
                            } else {
                                R.string.prompt_ai_requirement_optional
                            }
                        )
                    )
                },
                minLines = 3,
                maxLines = 6,
            )
            if (state.running) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { error ->
                Text(error.message, color = MaterialTheme.colorScheme.error)
                if (error.isBlocked) {
                    TextButton(onClick = onGetPoints) {
                        Text(stringResource(CoreR.string.ai_chat_error_action_get_points))
                    }
                }
            }
            RewriteComparison(state.original, state.candidate, scrollState)
        }
    }
}

@Composable
internal fun RewriteComparison(original: String, suggestion: String, scrollState: ScrollState) {
    if (original.isBlank() && suggestion.isBlank()) return
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 600.dp && original.isNotBlank() && suggestion.isNotBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RewritePreview(
                    stringResource(R.string.note_ai_original_label), original, scrollState, Modifier.weight(1f)
                )
                RewritePreview(
                    stringResource(R.string.note_ai_result_label), suggestion, scrollState, Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (original.isNotBlank()) {
                    RewritePreview(stringResource(R.string.note_ai_original_label), original, scrollState)
                }
                if (suggestion.isNotBlank()) {
                    RewritePreview(stringResource(R.string.note_ai_result_label), suggestion, scrollState)
                }
            }
        }
    }
}

@Composable
private fun RewritePreview(
    title: String,
    body: String,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        GlassSurface(style = GlassStyle.Thin) {
            OneBoxMarkdownContent(
                message = body,
                modifier = Modifier.fillMaxWidth(),
                paddingValues = PaddingValues(AppTheme.dimens.cardContentPadding),
                enableScroll = false,
                externalScrollState = scrollState,
                onLinkClick = rememberInAppLinkHandler(),
            )
        }
    }
}
