package com.wanbaohe.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.agent.tool.AgentQuestionType
import com.shifenmiao.ai.agent.tool.AgentUserQuestionItem
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset

@Composable
fun AIQuestionContent(
    request: AgentUserQuestionRequest,
    formState: AIQuestionFormState,
    modifier: Modifier = Modifier,
    useLazyColumn: Boolean
) {
    val focusManager = LocalFocusManager.current
    val textQuestionNames = remember(request.questions) {
        request.questions.filter { !it.isChoiceQuestion }.map { it.name }
    }

    if (useLazyColumn) {
        LazyColumn(
            modifier = modifier.imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (request.message.isNotBlank()) {
                item {
                    Text(
                        text = request.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(
                items = request.questions,
                key = { it.name }
            ) { question ->
                AIQuestionItem(
                    question = question,
                    formState = formState,
                    isLastTextQuestion = textQuestionNames.lastOrNull() == question.name,
                    onMoveToNextTextQuestion = {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .heightIn(max = 480.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (request.message.isNotBlank()) {
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            request.questions.forEach { question ->
                AIQuestionItem(
                    question = question,
                    formState = formState,
                    isLastTextQuestion = textQuestionNames.lastOrNull() == question.name,
                    onMoveToNextTextQuestion = {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@Composable
private fun AIQuestionItem(
    question: AgentUserQuestionItem,
    formState: AIQuestionFormState,
    isLastTextQuestion: Boolean,
    onMoveToNextTextQuestion: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (question.header.isNotBlank()) {
            Text(
                text = question.header,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = buildString {
                append(question.question)
                if (question.required) append(" *")
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (question.isChoiceQuestion) {
            question.options.forEach { option ->
                if (question.multiSelect) {
                    MultipleChoiceRow(
                        questionName = question.name,
                        optionLabel = option.label,
                        optionValue = option.value,
                        selected = formState.isOptionSelected(question.name, option.value),
                        onToggle = { formState.toggleMultipleOption(question.name, option.value) }
                    )
                } else {
                    SingleChoiceRow(
                        questionName = question.name,
                        optionLabel = option.label,
                        optionValue = option.value,
                        selected = formState.isOptionSelected(question.name, option.value),
                        onSelect = { formState.selectSingleOption(question.name, option.value) }
                    )
                }
            }
        } else if (question.type != AgentQuestionType.text) {
            AISpecializedQuestionField(
                question = question,
                value = formState.getTextAnswer(question.name),
                onValueChange = { formState.updateTextAnswer(question.name, it) },
                modifier = Modifier.fillMaxWidth(),
                isLastTextQuestion = isLastTextQuestion,
                onMoveToNextTextQuestion = onMoveToNextTextQuestion,
                onDone = onDone,
            )
        } else {
            GlassOutlinedTextField(
                value = formState.getTextAnswer(question.name),
                onValueChange = { formState.updateTextAnswer(question.name, it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = !question.multiline,
                visualPreset = if (question.multiline) {
                    GlassTextFieldVisualPreset.Balanced
                } else {
                    GlassTextFieldVisualPreset.Expressive
                },
                placeholder = {
                    if (question.placeholder.isNotBlank()) {
                        Text(question.placeholder)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = when {
                        question.multiline -> ImeAction.Default
                        isLastTextQuestion -> ImeAction.Done
                        else -> ImeAction.Next
                    }
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onMoveToNextTextQuestion() },
                    onDone = { onDone() }
                ),
                minLines = if (question.multiline) 4 else 1,
                maxLines = if (question.multiline) 6 else 1
            )
        }
    }
}

@Composable
private fun SingleChoiceRow(
    questionName: String,
    optionLabel: String,
    optionValue: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = optionLabel,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MultipleChoiceRow(
    questionName: String,
    optionLabel: String,
    optionValue: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = optionLabel,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
