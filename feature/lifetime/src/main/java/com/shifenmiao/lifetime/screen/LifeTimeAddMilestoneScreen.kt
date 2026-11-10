package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.icon.IconRegistry
import com.wanbaohe.base.ui.icon.IconSelector
import com.wanbaohe.base.ui.icon.IconSelectorDisplayMode
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeAddMilestoneComponent
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionHeader
import java.time.LocalDate

@Composable
fun LifeTimeAddMilestoneScreen(
    component: LifeTimeAddMilestoneComponent
) {
    val uiState by component.uiState.collectAsState()

    if (uiState.isLoading) {
        BaseScreen(
            title = stringResource(R.string.lifetime_loading),
            onGoBack = { component.onGoBack() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.lifetime_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    MilestoneForm(
        initial = uiState.milestone,
        onCancel = { component.onGoBack() },
        onSave = component::save,
    )
}

@Composable
private fun MilestoneForm(
    initial: PersonalMilestone?,
    onCancel: () -> Unit,
    onSave: (PersonalMilestone) -> Unit,
) {
    val isEditing = initial != null
    // 编辑时用原记录做默认值；remember key 用 id，切换记录会重置表单
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var selectedIconKey by remember(initial) { mutableStateOf(initial?.iconKey ?: "EmojiEvents") }
    var targetDate by remember(initial) { mutableStateOf(initial?.targetDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var note by remember(initial) { mutableStateOf(initial?.note.orEmpty()) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val trimmedName = name.trim()
    val isNameValid = trimmedName.isNotEmpty()
    val isTargetDateValid = targetDate != null
    val canSave = isNameValid && isTargetDateValid
    val shouldShowNameError = hasSubmitted && !isNameValid
    val shouldShowTargetDateError = hasSubmitted && !isTargetDateValid
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val dateCardContainerColor = if (shouldShowTargetDateError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val datePlaceholderColor = if (shouldShowTargetDateError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        placeholderColor
    }

    BaseScreen(
        title = stringResource(
            if (isEditing) R.string.lifetime_edit_milestone else R.string.lifetime_add_milestone
        ),
        onGoBack = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = OneBoxDesignSystem.screenPadding,
                    vertical = OneBoxDesignSystem.compactSpacing
                ),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing)
        ) {
            // Name input
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.lifetime_milestone_name_hint)
                )
                val nameTextStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                )
                val namePlaceholderTextStyle = MaterialTheme.typography.headlineMedium
                OneBoxOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.lifetime_milestone_name_placeholder),
                            style = namePlaceholderTextStyle,
                            color = placeholderColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = nameTextStyle,
                    isError = shouldShowNameError,
                    supportingText = {
                        if (shouldShowNameError) {
                            Text(stringResource(R.string.lifetime_validation_milestone_name_required))
                        }
                    }
                )
            }

            // Icon selector
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
                ) {
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_choose_icon)
                    )
                    IconSelector(
                        selectedIconKey = selectedIconKey,
                        onIconSelected = { selectedIconKey = it },
                        iconKeys = IconRegistry.allKeys,
                        displayMode = IconSelectorDisplayMode.ROW,
                        modifier = Modifier.height(56.dp)
                    )
                }
            }

            // Target date
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = dateCardContainerColor
                ),
                onClick = { showDatePicker = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                ) {
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_milestone_target_date)
                    )
                    if (targetDate != null) {
                        Text(
                            text = stringResource(R.string.lifetime_date_ymd, targetDate!!.year, targetDate!!.monthValue, targetDate!!.dayOfMonth),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.lifetime_select_date),
                            style = MaterialTheme.typography.titleMedium,
                            color = datePlaceholderColor
                        )
                    }
                }
            }
            if (shouldShowTargetDateError) {
                Text(
                    text = stringResource(R.string.lifetime_validation_milestone_target_date_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = OneBoxDesignSystem.compactSpacing)
                )
            }

            // Note
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.lifetime_milestone_note)
                )
                OneBoxOutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.lifetime_milestone_note_placeholder),
                            color = placeholderColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            // BaseScreen 已处理导航栏内边距，这里再叠一层会多出一条缝
            applyNavigationBarsPadding = false,
            primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
            secondaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
            onCancel = onCancel,
            onSave = {
                hasSubmitted = true
                if (!canSave) return@BottomSaveCancelBar

                // 编辑时保留 id / 创建时间 / 排序等原字段，只改表单里的四项
                val base = initial ?: PersonalMilestone(name = trimmedName)
                onSave(
                    base.copy(
                        name = trimmedName,
                        iconKey = selectedIconKey,
                        targetDate = targetDate,
                        note = note.takeIf { it.isNotBlank() }
                    )
                )
            },
            saveEnabled = true,
            saveText = stringResource(
                if (isEditing) R.string.lifetime_note_save else R.string.lifetime_add_milestone
            )
        )
    }

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = targetDate,
            onDateSelected = { date ->
                targetDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
