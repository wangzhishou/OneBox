package com.wanbaohe.bookkeeping.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.CategoriesGrouped
import com.wanbaohe.bookkeeping.screen.sheet.CategoryPickerSheet
import java.time.LocalDate
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBackspace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote

@Composable
fun AddRecordScreen(component: BookkeepingComponent) {
    val uiState by component.uiState.collectAsState()
    val allCategories by component.allCategoriesFlow.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    // 数字键盘默认展开；折叠后只显示一条窄 bar
    var keypadExpanded by remember { mutableStateOf(true) }

    val isEditing = uiState.editingRecordId != null

    val selectedCategoryName = uiState.categories
        .firstOrNull { it.id == uiState.selectedCategoryId }
        ?.name
        ?: getString(R.string.bookkeeping_selected_category_empty)
    val pickerCategories = remember(uiState.selectedType, allCategories) {
        when (uiState.selectedType) {
            BookkeepingRecordType.EXPENSE -> CategoriesGrouped(expense = allCategories.expense)
            BookkeepingRecordType.INCOME -> CategoriesGrouped(income = allCategories.income)
            BookkeepingRecordType.EXCLUDED -> CategoriesGrouped(excluded = allCategories.excluded)
        }
    }

    val successMessage = getString(
        if (isEditing) R.string.bookkeeping_edit_success
        else R.string.bookkeeping_submit_success
    )
    val invalidMessage = getString(R.string.bookkeeping_submit_invalid_amount)

    val submit: () -> Unit = {
        when {
            uiState.selectedCategoryId == null -> {
                AppToastHost.showToast(getString(R.string.bookkeeping_category_required))
            }
            if (isEditing) component.submitEditRecord() else component.submitRecord() -> {
                AppToastHost.showToast(successMessage)
            }
            else -> {
                AppToastHost.showToast(invalidMessage)
            }
        }
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (isEditing) R.string.bookkeeping_edit_record_title
                    else R.string.bookkeeping_add_record_title
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        onGoBack = component.onGoBack,
        actions = {
            Text(
                text = stringResource(R.string.bookkeeping_save),
                color = AppTheme.colors.getPrimaryColor(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = submit)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        supportGlassEffect = true,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                // 内容区底部留出动态 padding：折叠时仅留 96dp（窄 bar），展开时 340dp
                val contentBottomPadding = if (keypadExpanded) 340.dp else 96.dp
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = contentBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypeButton(
                            text = stringResource(R.string.bookkeeping_expense),
                            selected = uiState.selectedType == BookkeepingRecordType.EXPENSE,
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onSelectedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { component.onTypeChange(BookkeepingRecordType.EXPENSE) },
                            modifier = Modifier.weight(1f)
                        )
                        TypeButton(
                            text = stringResource(R.string.bookkeeping_income),
                            selected = uiState.selectedType == BookkeepingRecordType.INCOME,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            onSelectedContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { component.onTypeChange(BookkeepingRecordType.INCOME) },
                            modifier = Modifier.weight(1f)
                        )
                        TypeButton(
                            text = stringResource(R.string.bookkeeping_excluded),
                            selected = uiState.selectedType == BookkeepingRecordType.EXCLUDED,
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            onSelectedContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = { component.onTypeChange(BookkeepingRecordType.EXCLUDED) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.bookkeeping_amount_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            borderWidth = 0.dp,
                            containerAlpha = 0.7f,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "¥",
                                        color = AppTheme.colors.getPrimaryColor(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.amountInput.ifEmpty { "0.00" },
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    InfoPill(
                                        modifier = Modifier.weight(1f),
                                        label = stringResource(R.string.bookkeeping_category_label),
                                        value = selectedCategoryName,
                                        onClick = { showCategoryPicker = true },
                                    )
                                    InfoPill(
                                        modifier = Modifier.weight(1f),
                                        label = stringResource(R.string.bookkeeping_date_label),
                                        value = if (uiState.selectedDate == LocalDate.now()) {
                                            stringResource(R.string.bookkeeping_today_label)
                                        } else {
                                            "${uiState.selectedDate.monthValue}/${uiState.selectedDate.dayOfMonth}"
                                        },
                                        onClick = { showDatePicker = true },
                                    )
                                }
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        borderWidth = 0.dp,
                        containerAlpha = 0.4f,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            BasicTextField(
                                value = uiState.noteInput,
                                onValueChange = component::onNoteInputChange,
                                minLines = 2,
                                maxLines = 4,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(AppTheme.colors.getPrimaryColor()),
                                decorationBox = { innerTextField ->
                                    if (uiState.noteInput.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.bookkeeping_note_hint),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            fontSize = 16.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── 底部输入面板（可折叠） ─────────────────────────────────────
                GlassCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    containerAlpha = 0.8f,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // 顶部折叠/展开 bar：chevron + 备注摘要 / 占位提示
                        KeypadCollapseBar(
                            expanded = keypadExpanded,
                            noteSummary = uiState.noteInput,
                            onToggle = { keypadExpanded = !keypadExpanded },
                        )

                        AnimatedVisibility(
                            visible = keypadExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(3f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        listOf("1", "2", "3"),
                                        listOf("4", "5", "6"),
                                        listOf("7", "8", "9"),
                                        listOf(".", "0", "BACKSPACE")
                                    ).forEach { rowKeys ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowKeys.forEach { key ->
                                                if (key == "BACKSPACE") {
                                                    KeypadButton(
                                                        modifier = Modifier.weight(1f),
                                                        onClick = component::deleteAmountLast
                                                    ) {
                                                        Icon(
                                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                } else {
                                                    KeypadButton(
                                                        modifier = Modifier.weight(1f),
                                                        onClick = { component.appendAmountDigit(key.first()) }
                                                    ) {
                                                        Text(
                                                            text = key,
                                                            fontSize = 20.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(104.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.dp,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        containerAlpha = 0.5f,
                                        onClick = { showDatePicker = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val isToday = uiState.selectedDate == LocalDate.now()
                                            Text(
                                                text = if (isToday) stringResource(R.string.bookkeeping_today_label) else "${uiState.selectedDate.monthValue}/${uiState.selectedDate.dayOfMonth}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(132.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 0.dp,
                                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.getPrimaryColor()),
                                        onClick = submit
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.bookkeeping_confirm),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AppTheme.colors.getOnPrimaryColor()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = uiState.selectedDate,
            maxDate = LocalDate.now(),
            onDateSelected = { date ->
                component.onDateChange(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            title = stringResource(R.string.bookkeeping_picker_category_title),
            grouped = pickerCategories,
            showAllOption = false,
            onSelectAll = {},
            onSelectCategory = { id, _ ->
                component.onCategorySelected(id)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
        )
    }
}

/**
 * 数字键盘折叠/展开顶部 bar。
 *
 * 布局: [备注摘要 weight(1f)]  [收起/展开 文字 + chevron]
 * - 触发区横跨整条（整行 clickable）
 * - 文字 + chevron 放右侧符合用户右手单手操作直觉
 * - 备注为空时占位提示让 bar 在折叠态也不空
 */
@Composable
private fun KeypadCollapseBar(
    expanded: Boolean,
    noteSummary: String,
    onToggle: () -> Unit,
) {
    val toggleLabel = stringResource(
        if (expanded) R.string.bookkeeping_collapse_keypad
        else R.string.bookkeeping_expand_keypad
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 24.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = toggleLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowDown else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward,
                contentDescription = toggleLabel,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val baseModifier = modifier
        .clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }
    Column(
        modifier = finalModifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TypeButton(
    text: String,
    selected: Boolean,
    selectedContainerColor: Color,
    onSelectedContainerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) selectedContainerColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) onSelectedContainerColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun KeypadButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        containerAlpha = 0.4f,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
