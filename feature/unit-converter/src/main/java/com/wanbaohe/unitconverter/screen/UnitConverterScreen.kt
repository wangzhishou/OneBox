package com.wanbaohe.unitconverter.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.unitconverter.R
import com.wanbaohe.unitconverter.component.CalculatorHistoryItem
import com.wanbaohe.unitconverter.component.CalculatorUiState
import com.wanbaohe.unitconverter.component.KinshipUiState
import com.wanbaohe.unitconverter.component.UnitConverterComponent
import com.wanbaohe.unitconverter.component.UnitConverterUiState
import com.wanbaohe.unitconverter.domain.KinshipGender
import com.wanbaohe.unitconverter.domain.KinshipStep
import com.wanbaohe.unitconverter.domain.UnitCategory
import com.wanbaohe.unitconverter.domain.UnitConverterTab
import com.wanbaohe.unitconverter.domain.UnitData
import com.wanbaohe.unitconverter.domain.UnitItem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalculate
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBackspace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFunctions
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMan
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWoman
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSyncAlt

@Composable
fun UnitConverterScreen(component: UnitConverterComponent) {
    val state by component.uiState.collectAsState()

    val invalidExpressionMessage = stringResource(R.string.unit_converter_invalid_expression)
    var pickingFromUnit by remember { mutableStateOf<Boolean?>(null) }

    BaseScreen(
        title = stringResource(R.string.unit_converter_title),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            when (state.selectedTab) {
                UnitConverterTab.Calculator -> CalculatorTab(
                    modifier = modifier,
                    state = state.calculator,
                    onAppendToken = component::appendCalculatorToken,
                    onBackspace = component::backspaceCalculator,
                    onClear = component::clearCalculator,
                    onHistoryClick = component::useCalculatorHistory,
                    onEvaluate = {
                        if (!component.calculateExpression()) {
                            AppToastHost.showToast(invalidExpressionMessage)
                        }
                    }
                )

                UnitConverterTab.Converter -> ConverterTab(
                    modifier = modifier,
                    state = state,
                    formatNumber = component::formatNumber,
                    onCategorySelected = component::setCategory,
                    onSwap = component::swapUnits,
                    onPickFromUnit = { pickingFromUnit = true },
                    onPickToUnit = { pickingFromUnit = false },
                    onAppendDigit = component::appendConverterDigit,
                    onBackspace = component::converterBackspace,
                    onClearInput = component::converterClearInput,
                )

                UnitConverterTab.Relative -> RelativeTab(
                    modifier = modifier,
                    state = state.kinship,
                    onGenderSelected = component::setKinshipGender,
                    onStepClick = component::addKinshipStep,
                    onRemoveLast = component::removeLastKinshipStep,
                    onClear = component::clearKinshipSteps,
                )
            }
            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        id = UnitConverterTab.Calculator.route,
                        label = stringResource(R.string.unit_converter_tab_calculator),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFunctions
                    ),
                    BottomNavItem(
                        id = UnitConverterTab.Converter.route,
                        label = stringResource(R.string.unit_converter_tab_converter),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSyncAlt
                    ),
                    BottomNavItem(
                        id = UnitConverterTab.Relative.route,
                        label = stringResource(R.string.unit_converter_tab_relative),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate
                    ),
                ),
                selectedItemId = state.selectedTab.route,
                onItemClick = { item ->
                    component.selectTab(UnitConverterTab.fromRoute(item.id))
                }
            )
        }
    }

    if (pickingFromUnit != null) {
        val isFrom = pickingFromUnit == true
        ModalBottomSheet(
            onDismissRequest = { pickingFromUnit = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent,
            scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        ) {
            UnitPickerContent(
                units = state.units,
                selectedUnit = if (isFrom) state.fromUnit else state.toUnit,
                onUnitSelected = { unit ->
                    if (isFrom) component.setFromUnit(unit) else component.setToUnit(unit)
                    pickingFromUnit = null
                },
                onDismiss = { pickingFromUnit = null }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// 计算器 Tab（确保一屏显示，历史可折叠）
// ─────────────────────────────────────────────────────────
@Composable
private fun CalculatorTab(
    modifier: Modifier = Modifier.fillMaxSize(),
    state: CalculatorUiState,
    onAppendToken: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onEvaluate: () -> Unit,
) {
    var showHistory by remember { mutableStateOf(false) }

    val keypadRows = remember {
        listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("%", "0", ".", "="),
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /* ── 显示区域（紧凑）── */
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            containerAlpha = 0.2f,
            borderWidth = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 表达式
                Text(
                    text = state.expression.ifEmpty { "0" },
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // 实时结果
                Text(
                    text = state.previewResult.ifEmpty {
                        stringResource(R.string.unit_converter_calculator_hint)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    color = if (state.previewResult.isNotEmpty()) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                )
            }
        }

        /* ── 图标按钮行（右对齐：历史 → 计算 → 退格）── */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 历史图标（有记录时才显示）
            if (state.history.isNotEmpty()) {
                IconButton(
                    onClick = { showHistory = !showHistory },
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = stringResource(R.string.unit_converter_calculator_history),
                        modifier = Modifier.size(22.dp),
                        tint = if (showHistory) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // 计算（等号）图标
            IconButton(
                onClick = onEvaluate,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate,
                    contentDescription = stringResource(R.string.unit_converter_equals),
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            // 退格图标
            IconButton(
                onClick = onBackspace,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace,
                    contentDescription = stringResource(R.string.unit_converter_backspace),
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        /* ── 历史记录展开区域（点击历史图标直接展开横向滚动）── */
        AnimatedVisibility(
            visible = showHistory && state.history.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.history) { item ->
                    CalculatorHistoryCard(
                        item = item,
                        onClick = { onHistoryClick(item.expression) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))


        /* ── 键盘（固定行高 52dp，确保一屏显示）── */
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 28.dp, topEnd = 28.dp,
                bottomStart = 24.dp, bottomEnd = 24.dp
            ),
            containerAlpha = 0.28f,
            borderWidth = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            CalculatorKey(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                label = key,
                                isPrimary = key == "=",
                                isOperator = key in setOf("+", "-", "×", "÷"),
                                isDanger = key == "C",
                                onClick = {
                                    when (key) {
                                        "C" -> onClear()
                                        "=" -> onEvaluate()
                                        else -> onAppendToken(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 单位换算 Tab（键盘可收起展开）
// ─────────────────────────────────────────────────────────
@Composable
private fun ConverterTab(
    state: UnitConverterUiState,
    formatNumber: (Double) -> String,
    onCategorySelected: (UnitCategory) -> Unit,
    onSwap: () -> Unit,
    onPickFromUnit: () -> Unit,
    onPickToUnit: () -> Unit,
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearInput: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    var keypadExpanded by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        /* ── 可滚动内容 ── */
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CategorySelectorRow(
                    selectedCategory = state.category,
                    onCategorySelected = onCategorySelected
                )
            }

            item {
                ConverterDisplayCards(
                    state = state,
                    onSwap = onSwap,
                    onPickFromUnit = onPickFromUnit,
                    onPickToUnit = onPickToUnit,
                )
            }

            if (state.category == UnitCategory.Currency) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerAlpha = 0.16f,
                        borderWidth = 0.dp,
                    ) {
                        Text(
                            text = stringResource(R.string.unit_converter_currency_disclaimer),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    title = stringResource(
                        R.string.unit_converter_other_conversions,
                        state.category.displayName
                    ),
                    subtitle = stringResource(R.string.unit_converter_reference_subtitle)
                )
            }

            val sourceValue = state.inputText.toDoubleOrNull() ?: 1.0
            val referenceItems = state.units.filter { it != state.fromUnit }.chunked(2)
            items(referenceItems.size) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    referenceItems[index].forEach { unit ->
                        val base = state.fromUnit.convertToBase(sourceValue)
                        val converted = unit.convertFromBase(base)
                        ReferenceCard(
                            modifier = Modifier.weight(1f),
                            value = formatNumber(converted),
                            unit = unit
                        )
                    }
                    if (referenceItems[index].size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        /* ── 收起态: 紧凑输入栏 ── */
        AnimatedVisibility(
            visible = !keypadExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            ConverterInputBar(
                inputText = state.inputText,
                onExpand = { keypadExpanded = true }
            )
        }

        /* ── 展开态: 完整数字键盘 ── */
        AnimatedVisibility(
            visible = keypadExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            ConverterKeypad(
                onDigit = onAppendDigit,
                onBackspace = onBackspace,
                onClear = onClearInput,
                onCollapse = { keypadExpanded = false },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// 亲戚关系 Tab
// ─────────────────────────────────────────────────────────
@Composable
private fun RelativeTab(
    state: KinshipUiState,
    onGenderSelected: (KinshipGender) -> Unit,
    onStepClick: (KinshipStep) -> Unit,
    onRemoveLast: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val relationActions = remember {
        listOf(
            RelationAction(KinshipStep.Father, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMan),
            RelationAction(KinshipStep.Mother, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWoman),
            RelationAction(KinshipStep.Spouse, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite),
            RelationAction(null, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace, isClear = true),
            RelationAction(KinshipStep.Brother, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMan),
            RelationAction(KinshipStep.Sister, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWoman),
            RelationAction(KinshipStep.Son, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMan),
            RelationAction(KinshipStep.Daughter, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWoman),
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* ── 结果面板：面包屑路径 + 称谓结果 ── */
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            containerAlpha = 0.2f,
            borderWidth = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.unit_converter_relative_path),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        /* 面包屑路径：chevron 分隔 */
                        if (state.steps.isEmpty()) {
                            Text(
                                text = stringResource(R.string.unit_converter_relative_path_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = stringResource(R.string.unit_converter_relative_path_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                state.steps.forEach { step ->
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.outline,
                                    )
                                    Text(
                                        text = step.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onRemoveLast,
                        colors = AppTheme.colors.iconButtonColors(),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace,
                            contentDescription = stringResource(R.string.unit_converter_backspace)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.unit_converter_relative_result_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = state.resultTitle,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.resultDescription.isNotEmpty()) {
                    Text(
                        text = state.resultDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ── 4×2 亲属按钮网格 ── */
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            relationActions.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { action ->
                        RelationButton(
                            modifier = Modifier.weight(1f),
                            action = action,
                            onClick = {
                                if (action.isClear) onClear()
                                else action.step?.let(onStepClick)
                            }
                        )
                    }
                }
            }
        }

        /* ── 底部：性别切换 ── */
        GenderToggleRow(
            modifier = Modifier.fillMaxWidth(),
            gender = state.gender,
            onGenderSelected = onGenderSelected
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────
// 子组件
// ─────────────────────────────────────────────────────────

/** 计算器历史卡片 */
@Composable
private fun CalculatorHistoryCard(
    item: CalculatorHistoryItem,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .width(136.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        containerAlpha = 0.18f,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.expression,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.result,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * 计算器按键
 * - `=` 使用 primary / onPrimary（实心强调，匹配设计稿）
 * - 运算符使用 primaryContainer / onPrimaryContainer
 * - C 使用 errorContainer
 * - 数字使用 surfaceContainerLow
 */
@Composable
private fun CalculatorKey(
    modifier: Modifier,
    label: String,
    isPrimary: Boolean,
    isOperator: Boolean,
    isDanger: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        isPrimary -> MaterialTheme.colorScheme.primary
        isDanger -> MaterialTheme.colorScheme.errorContainer
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = when {
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        isDanger -> MaterialTheme.colorScheme.onErrorContainer
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    GlassSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        borderWidth = 0.dp,
        style = GlassStyle.Regular,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}

/** 单位换算类别选择行（横向滚动 pill 样式） */
@Composable
private fun CategorySelectorRow(
    selectedCategory: UnitCategory,
    onCategorySelected: (UnitCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UnitData.ALL_CATEGORIES.forEach { category ->
            val selected = category == selectedCategory
            GlassSurface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                borderWidth = 0.dp,
            ) {
                Text(
                    text = category.displayName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 单位换算 FROM / TO 双面板 + 浮动交换按钮
 *
 * 拆成两张独立 GlassCard，中间叠加圆形交换按钮。
 */
@Composable
private fun ConverterDisplayCards(
    state: UnitConverterUiState,
    onSwap: () -> Unit,
    onPickFromUnit: () -> Unit,
    onPickToUnit: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /* ── FROM 面板 ── */
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                containerAlpha = 0.2f,
                borderWidth = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = state.fromUnit.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                        )
                        UnitSelectorChip(
                            unit = state.fromUnit,
                            onClick = onPickFromUnit
                        )
                    }
                    Text(
                        text = state.inputText.ifEmpty { "0" },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            /* ── TO 面板 ── */
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                containerAlpha = 0.2f,
                borderWidth = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = state.toUnit.name.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                        )
                        UnitSelectorChip(
                            unit = state.toUnit,
                            onClick = onPickToUnit
                        )
                    }
                    Text(
                        text = state.resultText.ifEmpty { "0" },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        /* ── 交换按钮（浮动居中）── */
        GlassSurface(
            onClick = onSwap,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            borderWidth = 0.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert,
                    contentDescription = stringResource(R.string.unit_converter_swap),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/**
 * 单位换算 Tab 底部的自定义数字键盘
 *
 * 布局: 折叠指示条 + 3 行标准按键 + 底行 0 键横跨 3 列 + 正负号键
 */
@Composable
private fun ConverterKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCollapse: () -> Unit,
) {
    val topRows = remember {
        listOf(
            listOf("1", "2", "3", "⌫"),
            listOf("4", "5", "6", "C"),
            listOf("7", "8", "9", "."),
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp, topEnd = 28.dp,
            bottomStart = 24.dp, bottomEnd = 24.dp
        ),
        containerAlpha = 0.28f,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            /* ── 折叠指示条：点击收起键盘 ── */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCollapse)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            topRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { key ->
                        val isDanger = key == "C"
                        val isAction = key == "⌫" || isDanger
                        ConverterKey(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            label = key,
                            showIcon = key == "⌫",
                            isDanger = isDanger,
                            isAction = isAction,
                            onClick = {
                                when (key) {
                                    "⌫" -> onBackspace()
                                    "C" -> onClear()
                                    else -> onDigit(key)
                                }
                            }
                        )
                    }
                }
            }
            /* 最后一行: 0 占 3 列，正负号占 1 列 */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConverterKey(
                    modifier = Modifier
                        .weight(3f)
                        .height(48.dp),
                    label = "0",
                    onClick = { onDigit("0") }
                )
                ConverterKey(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    label = "±",
                    isAction = true,
                    onClick = { onDigit("-") }
                )
            }
        }
    }
}

/** 换算键盘单个按键 */
@Composable
private fun ConverterKey(
    modifier: Modifier,
    label: String,
    showIcon: Boolean = false,
    isDanger: Boolean = false,
    isAction: Boolean = false,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isDanger -> MaterialTheme.colorScheme.errorContainer
        isAction -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val fgColor = when {
        isDanger -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    GlassSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        borderWidth = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showIcon) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBackspace,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = fgColor,
                )
            }
        }
    }
}

/**
 * 换算键盘收起态的紧凑输入栏
 *
 * 显示当前输入值 + 键盘图标，点击展开完整键盘
 */
@Composable
private fun ConverterInputBar(
    inputText: String,
    onExpand: () -> Unit,
) {
    GlassSurface(
        onClick = onExpand,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = inputText.ifEmpty { "0" },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** 单位选择器 Chip（含下拉箭头） */
@Composable
private fun UnitSelectorChip(
    unit: UnitItem,
    onClick: () -> Unit,
) {
    GlassSurface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${unit.name} (${unit.symbol})",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 参考换算卡片 */
@Composable
private fun ReferenceCard(
    modifier: Modifier = Modifier,
    value: String,
    unit: UnitItem,
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerAlpha = 0.16f,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${unit.name} (${unit.symbol})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 单位选择弹窗内容 */
@Composable
private fun UnitPickerContent(
    units: List<UnitItem>,
    selectedUnit: UnitItem,
    onUnitSelected: (UnitItem) -> Unit,
    onDismiss: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(
            topStart = 28.dp, topEnd = 28.dp,
            bottomStart = 24.dp, bottomEnd = 24.dp
        ),
        containerAlpha = 0.42f,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.unit_converter_select_unit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.unit_converter_close)
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(units) { unit ->
                    val selected = unit == selectedUnit
                    GlassSurface(
                        onClick = { onUnitSelected(unit) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        borderWidth = 0.dp,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 性别切换行（分段控件风格，匹配设计稿的 segmented toggle）
 */
@Composable
private fun GenderToggleRow(
    modifier: Modifier = Modifier,
    gender: KinshipGender,
    onGenderSelected: (KinshipGender) -> Unit,
) {
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GenderChip(
                modifier = Modifier.weight(1f),
                selected = gender == KinshipGender.Male,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMan,
                label = stringResource(R.string.unit_converter_relative_male),
                onClick = { onGenderSelected(KinshipGender.Male) }
            )
            GenderChip(
                modifier = Modifier.weight(1f),
                selected = gender == KinshipGender.Female,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWoman,
                label = stringResource(R.string.unit_converter_relative_female),
                onClick = { onGenderSelected(KinshipGender.Female) }
            )
        }
    }
}

@Composable
private fun GenderChip(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    GlassSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            Color.Transparent
        },
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

/**
 * 亲属关系按钮 —— 带圆形图标容器背景
 *
 * 设计稿: 每个图标包裹在 w-12 h-12 rounded-full bg-primary-container/10 的圆内
 */
@Composable
private fun RelationButton(
    modifier: Modifier,
    action: RelationAction,
    onClick: () -> Unit,
) {
    val contentColor = if (action.isClear) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    GlassSurface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* 圆形图标容器 */
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (action.isClear) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = if (action.isClear) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (action.isClear) {
                    stringResource(R.string.unit_converter_clear)
                } else {
                    action.step?.label.orEmpty()
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private data class RelationAction(
    val step: KinshipStep?,
    val icon: ImageVector,
    val isClear: Boolean = false,
)
