package com.wanbaohe.unitconverter.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.unitconverter.domain.CalculatorEvaluator
import com.wanbaohe.unitconverter.domain.KinshipCalculator
import com.wanbaohe.unitconverter.domain.KinshipGender
import com.wanbaohe.unitconverter.domain.KinshipStep
import com.wanbaohe.unitconverter.domain.UnitCategory
import com.wanbaohe.unitconverter.domain.UnitData
import com.wanbaohe.unitconverter.domain.UnitItem
import com.wanbaohe.unitconverter.domain.UnitConverterTab
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorHistoryItem(
    val expression: String,
    val result: String,
)

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val history: List<CalculatorHistoryItem> = emptyList(),
)

data class KinshipUiState(
    val gender: KinshipGender = KinshipGender.Male,
    val steps: List<KinshipStep> = emptyList(),
    val resultTitle: String = "自己",
    val resultDescription: String = "请选择亲属链路后计算关系",
)

data class UnitConverterUiState(
    val selectedTab: UnitConverterTab = UnitConverterTab.Converter,
    val category: UnitCategory = UnitCategory.Length,
    val fromUnit: UnitItem = UnitData.LENGTH[0],
    val toUnit: UnitItem = UnitData.LENGTH[1],
    val inputText: String = "1",
    val resultText: String = "",
    val units: List<UnitItem> = UnitData.LENGTH,
    val calculator: CalculatorUiState = CalculatorUiState(),
    val kinship: KinshipUiState = KinshipUiState(),
)

class UnitConverterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("initialTab") initialTab: String,
    @Assisted("initialCategory") initialCategory: String?,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        UnitConverterUiState(
            selectedTab = UnitConverterTab.fromRoute(initialTab),
        ).let { defaultState ->
            val resolvedCategory = resolveCategory(initialCategory)
            val units = UnitData.unitsFor(resolvedCategory)
            val from = units.first()
            val to = units.getOrElse(1) { units.first() }
            val kinshipResult = KinshipCalculator.resolve(
                gender = defaultState.kinship.gender,
                steps = defaultState.kinship.steps
            )
            val s = defaultState.copy(
                category = resolvedCategory,
                units = units,
                fromUnit = from,
                toUnit = to,
                kinship = defaultState.kinship.copy(
                    resultTitle = kinshipResult.title,
                    resultDescription = kinshipResult.description
                )
            )
            s.copy(resultText = convert(s.inputText, s.fromUnit, s.toUnit))
        }
    )
    val uiState = _uiState.asStateFlow()

    fun selectTab(tab: UnitConverterTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setCategory(category: UnitCategory) {
        val units = UnitData.unitsFor(category)
        val from = units[0]
        val to = units.getOrElse(1) { units[0] }
        _uiState.update { s ->
            val result = convert(s.inputText, from, to)
            s.copy(category = category, units = units, fromUnit = from, toUnit = to, resultText = result)
        }
    }

    fun setFromUnit(unit: UnitItem) {
        _uiState.update { s ->
            val result = convert(s.inputText, unit, s.toUnit)
            s.copy(fromUnit = unit, resultText = result)
        }
    }

    fun setToUnit(unit: UnitItem) {
        _uiState.update { s ->
            val result = convert(s.inputText, s.fromUnit, unit)
            s.copy(toUnit = unit, resultText = result)
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { s ->
            val result = convert(text, s.fromUnit, s.toUnit)
            s.copy(inputText = text, resultText = result)
        }
    }

    fun swapUnits() {
        _uiState.update { s ->
            val newInput = if (s.resultText.isNotEmpty()) s.resultText else s.inputText
            val result = convert(newInput, s.toUnit, s.fromUnit)
            s.copy(fromUnit = s.toUnit, toUnit = s.fromUnit, inputText = newInput, resultText = result)
        }
    }

    /**
     * 单位换算 Tab: 自定义数字键盘追加字符
     */
    fun appendConverterDigit(digit: String) {
        _uiState.update { s ->
            val current = s.inputText
            val newInput = when {
                digit == "." -> {
                    if (current.contains(".")) current
                    else if (current.isEmpty() || current == "-") "${current}0."
                    else "$current."
                }
                digit == "-" -> {
                    if (current.startsWith("-")) current.removePrefix("-")
                    else if (current == "0") "-"
                    else "-$current"
                }
                current == "0" -> digit
                current == "-0" -> "-$digit"
                else -> current + digit
            }
            val result = convert(newInput, s.fromUnit, s.toUnit)
            s.copy(inputText = newInput, resultText = result)
        }
    }

    /**
     * 单位换算 Tab: 数字键盘退格
     */
    fun converterBackspace() {
        _uiState.update { s ->
            val trimmed = s.inputText.dropLast(1)
            val newInput = when {
                trimmed.isEmpty() || trimmed == "-" -> "0"
                else -> trimmed
            }
            val result = convert(newInput, s.fromUnit, s.toUnit)
            s.copy(inputText = newInput, resultText = result)
        }
    }

    /**
     * 单位换算 Tab: 数字键盘清空输入
     */
    fun converterClearInput() {
        _uiState.update { s ->
            s.copy(inputText = "0", resultText = convert("0", s.fromUnit, s.toUnit))
        }
    }

    fun appendCalculatorToken(token: String) {
        _uiState.update { state ->
            val newExpression = buildExpression(state.calculator.expression, token) ?: state.calculator.expression
            state.copy(calculator = buildCalculatorState(newExpression, state.calculator.history))
        }
    }

    fun backspaceCalculator() {
        _uiState.update { state ->
            val trimmed = state.calculator.expression.dropLast(1)
            state.copy(
                calculator = buildCalculatorState(
                    expression = trimmed,
                    history = state.calculator.history
                )
            )
        }
    }

    fun clearCalculator() {
        _uiState.update { state ->
            state.copy(
                calculator = state.calculator.copy(
                    expression = "",
                    previewResult = ""
                )
            )
        }
    }

    fun clearCalculatorHistory() {
        _uiState.update { state ->
            state.copy(calculator = state.calculator.copy(history = emptyList()))
        }
    }

    fun useCalculatorHistory(expression: String) {
        _uiState.update { state ->
            state.copy(
                calculator = buildCalculatorState(
                    expression = expression,
                    history = state.calculator.history
                )
            )
        }
    }

    fun calculateExpression(): Boolean {
        val calculatorState = _uiState.value.calculator
        val expression = calculatorState.expression
        val result = CalculatorEvaluator.evaluate(expression) ?: return false
        val formatted = CalculatorEvaluator.format(result)
        _uiState.update { state ->
            val nextHistory = (
                listOf(CalculatorHistoryItem(expression = expression, result = formatted)) +
                    state.calculator.history.filterNot { it.expression == expression && it.result == formatted }
                ).take(8)
            state.copy(
                calculator = CalculatorUiState(
                    expression = formatted,
                    previewResult = formatted,
                    history = nextHistory
                )
            )
        }
        return true
    }

    fun setKinshipGender(gender: KinshipGender) {
        _uiState.update { state ->
            val result = KinshipCalculator.resolve(gender = gender, steps = state.kinship.steps)
            state.copy(
                kinship = state.kinship.copy(
                    gender = gender,
                    resultTitle = result.title,
                    resultDescription = result.description
                )
            )
        }
    }

    fun addKinshipStep(step: KinshipStep) {
        _uiState.update { state ->
            val steps = (state.kinship.steps + step).takeLast(6)
            val result = KinshipCalculator.resolve(gender = state.kinship.gender, steps = steps)
            state.copy(
                kinship = state.kinship.copy(
                    steps = steps,
                    resultTitle = result.title,
                    resultDescription = result.description
                )
            )
        }
    }

    fun removeLastKinshipStep() {
        _uiState.update { state ->
            val steps = state.kinship.steps.dropLast(1)
            val result = KinshipCalculator.resolve(gender = state.kinship.gender, steps = steps)
            state.copy(
                kinship = state.kinship.copy(
                    steps = steps,
                    resultTitle = result.title,
                    resultDescription = result.description
                )
            )
        }
    }

    fun clearKinshipSteps() {
        _uiState.update { state ->
            val result = KinshipCalculator.resolve(gender = state.kinship.gender, steps = emptyList())
            state.copy(
                kinship = state.kinship.copy(
                    steps = emptyList(),
                    resultTitle = result.title,
                    resultDescription = result.description
                )
            )
        }
    }

    private fun convert(inputText: String, from: UnitItem, to: UnitItem): String {
        val value = inputText.toDoubleOrNull() ?: return ""
        return try {
            val base = from.convertToBase(value)
            val result = to.convertFromBase(base)
            formatNumber(result)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "∞"
        if (value == 0.0) return "0"
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 1e15 || (abs < 1e-4 && abs > 0) -> {
                val exp = kotlin.math.floor(kotlin.math.log10(abs)).toInt()
                val mantissa = value / Math.pow(10.0, exp.toDouble())
                val mantissaStr = "%.4g".format(mantissa).trimEnd('0').trimEnd('.')
                "${mantissaStr}×10^${exp}"
            }
            abs < 1 -> "%.8g".format(value).trimEnd('0').trimEnd('.')
            abs < 1e6 -> "%.6g".format(value).trimEnd('0').trimEnd('.')
            else -> "%.6g".format(value).trimEnd('0').trimEnd('.')
        }
    }

    private fun resolveCategory(rawCategory: String?): UnitCategory {
        return UnitCategory.values().firstOrNull {
            it.name.equals(rawCategory, ignoreCase = true) ||
                it.displayName.equals(rawCategory, ignoreCase = true)
        } ?: UnitCategory.Length
    }

    private fun buildCalculatorState(
        expression: String,
        history: List<CalculatorHistoryItem>,
    ): CalculatorUiState {
        val preview = CalculatorEvaluator.evaluate(expression)?.let(CalculatorEvaluator::format).orEmpty()
        return CalculatorUiState(
            expression = expression,
            previewResult = preview,
            history = history
        )
    }

    private fun buildExpression(
        current: String,
        token: String,
    ): String? {
        if (token.isEmpty()) return current
        return when (token) {
            in setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9") -> {
                if (current == "0") token else current + token
            }

            "." -> {
                val lastNumber = current.takeLastWhile { it.isDigit() || it == '.' }
                if (lastNumber.contains(".")) current
                else if (current.isEmpty() || current.last().isOperator() || current.last() == '(') current + "0."
                else current + "."
            }

            "+", "-", "×", "÷" -> appendOperator(current, token)
            "%" -> if (current.isBlank() || current.last().isOperator() || current.last() == '(' || current.last() == '.') null else current + "%"
            "(" -> {
                when {
                    current.isEmpty() || current.last().isOperator() || current.last() == '(' -> current + "("
                    else -> current + "×("
                }
            }

            ")" -> {
                if (current.isEmpty()) null
                else if (current.count { it == '(' } <= current.count { it == ')' }) null
                else if (current.last().isOperator() || current.last() == '(') null
                else current + ")"
            }

            else -> null
        }
    }

    private fun appendOperator(
        current: String,
        operator: String,
    ): String? {
        if (current.isEmpty()) {
            return if (operator == "-") "-" else null
        }
        val last = current.last()
        if (last == '(') {
            return if (operator == "-") current + "-" else null
        }
        if (last.isOperator()) {
            if (operator == "-" && last != '-') return current + operator
            return current.dropLast(1) + operator
        }
        if (last == '.') return null
        return current + operator
    }

    private fun Char.isOperator(): Boolean {
        return this == '+' || this == '-' || this == '×' || this == '÷'
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            @Assisted("initialTab") initialTab: String,
            @Assisted("initialCategory") initialCategory: String?,
        ): UnitConverterComponent
    }
}
