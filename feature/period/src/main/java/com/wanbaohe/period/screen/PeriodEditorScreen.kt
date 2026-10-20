package com.wanbaohe.period.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.period.R
import com.wanbaohe.period.component.PeriodEditorComponent
import com.wanbaohe.period.model.PeriodColors
import com.wanbaohe.period.model.PeriodDayStatus
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.model.periodColors
import com.wanbaohe.period.screen.component.PeriodFormSection
import com.wanbaohe.period.screen.component.PeriodInfoIcon
import com.wanbaohe.period.screen.component.PeriodMoodFace
import com.wanbaohe.period.screen.component.PeriodSymptomChip
import com.wanbaohe.period.screen.component.moodLabel
import com.wanbaohe.period.screen.component.periodFullDate

@Composable
fun PeriodEditorScreen(
    component: PeriodEditorComponent,
) {
    val state by component.editorState.collectAsState()
    val colors = periodColors()

    BaseScreen(
        title = stringResource(
            if (state.isEditing) R.string.period_edit_title else R.string.period_add_title
        ),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(onClick = component::save) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.period_save),
                    tint = colors.accent,
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DatePickerCard(
                    colors = colors,
                    dateText = state.recordDate.periodFullDate(),
                    onClick = component::toggleDatePicker,
                )

                PeriodFormSection(title = stringResource(R.string.period_field_status)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatusPill(
                            label = stringResource(R.string.period_status_start),
                            selected = state.isPeriodStart,
                            onClick = { component.onStatusSelect(PeriodDayStatus.START) },
                            colors = colors,
                            modifier = Modifier.weight(1f),
                        )
                        StatusPill(
                            label = stringResource(R.string.period_status_mid),
                            selected = !state.isPeriodStart && !state.isPeriodEnd,
                            onClick = { component.onStatusSelect(PeriodDayStatus.MID) },
                            colors = colors,
                            modifier = Modifier.weight(1f),
                        )
                        StatusPill(
                            label = stringResource(R.string.period_status_end),
                            selected = state.isPeriodEnd,
                            onClick = { component.onStatusSelect(PeriodDayStatus.END) },
                            colors = colors,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                PeriodFormSection(title = stringResource(R.string.period_field_flow)) {
                    val flows = listOf(
                        PeriodFlow.LIGHT to stringResource(R.string.period_flow_light),
                        PeriodFlow.MEDIUM to stringResource(R.string.period_flow_medium),
                        PeriodFlow.HEAVY to stringResource(R.string.period_flow_heavy),
                    )
                    GlassSegmentedButtonRow(
                        options = flows,
                        selectedOption = flows.firstOrNull { it.first == state.flowIntensity }
                            ?: flows[1],
                        onOptionSelected = { component.onFlowChange(it.first) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(it.second) },
                        selectedColor = colors.accent,
                        selectedContentColor = colors.onAccent,
                    )
                }

                PeriodFormSection(
                    title = stringResource(R.string.period_field_symptoms) +
                        stringResource(R.string.period_symptoms_multi_hint),
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PeriodSymptom.entries.forEach { symptom ->
                            PeriodSymptomChip(
                                symptom = symptom,
                                selected = symptom in state.symptoms,
                                onClick = { component.onSymptomToggle(symptom) },
                                colors = colors,
                            )
                        }
                    }
                }

                PeriodFormSection(title = stringResource(R.string.period_field_mood)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PeriodMood.entries.forEach { mood ->
                            MoodItem(
                                mood = mood,
                                selected = state.mood == mood,
                                onClick = { component.onMoodChange(mood) },
                                colors = colors,
                            )
                        }
                    }
                }

                PeriodFormSection(title = stringResource(R.string.period_field_note)) {
                    GlassOutlinedTextField(
                        value = state.note,
                        onValueChange = component::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(stringResource(R.string.period_note_hint))
                        },
                        minLines = 3,
                        maxLines = 4,
                    )
                    Text(
                        text = stringResource(
                            R.string.period_note_counter,
                            state.note.length,
                            PeriodEditorComponent.NOTE_MAX_LENGTH,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                    )
                }

                if (state.isEditing) {
                    TextButton(
                        onClick = component::toggleDeleteConfirm,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.period_delete_record),
                            color = colors.accent,
                        )
                    }
                }

                Button(
                    onClick = component::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                    ),
                    enabled = !state.isSaving,
                ) {
                    Text(
                        text = stringResource(R.string.period_save),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }

    if (state.showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = state.recordDate,
            onDateSelected = { date -> component.onDateChange(date) },
            onDismiss = component::toggleDatePicker,
        )
    }

    if (state.showDeleteConfirm) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = component::toggleDeleteConfirm,
            confirmButton = {
                TextButton(onClick = {
                    component.toggleDeleteConfirm()
                    component.delete()
                }) {
                    Text(stringResource(R.string.period_delete), color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = component::toggleDeleteConfirm) {
                    Text(stringResource(R.string.period_cancel))
                }
            },
            title = { Text(stringResource(R.string.period_delete_confirm_title)) },
            text = { Text(stringResource(R.string.period_delete_confirm_message)) },
            enableGlass = false,
        )
    }
}

@Composable
private fun DatePickerCard(
    colors: PeriodColors,
    dateText: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PeriodInfoIcon(icon = Icons.Outlined.LineCalendar, colors = colors)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.period_field_date),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** 「经期开始 / 经期中 / 经期结束」三选一胶囊 */
@Composable
private fun StatusPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.accent else colors.accentSoft)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.LineWaterDrop,
            contentDescription = null,
            tint = if (selected) colors.onAccent else colors.accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (selected) colors.onAccent else colors.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MoodItem(
    mood: PeriodMood,
    selected: Boolean,
    onClick: () -> Unit,
    colors: PeriodColors,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.accent else colors.accentSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        PeriodMoodFace(
            mood = mood,
            color = if (selected) colors.onAccent else colors.onSurfaceVariant,
            modifier = Modifier.size(30.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = moodLabel(mood),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onAccent else colors.onSurfaceVariant,
        )
    }
}
