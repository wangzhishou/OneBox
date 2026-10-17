package com.wanbaohe.loancalculator.ui.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.loancalculator.R
import com.wanbaohe.loancalculator.ui.component.LoanInputItem
import com.wanbaohe.loancalculator.ui.input.widget.RepaymentMethodSheet
import com.wanbaohe.loancalculator.ui.input.widget.TermPickerSheet
import com.wanbaohe.loancalculator.viewmodel.LoanViewModel

/**
 * 贷款计算器输入面板。
 * 包含：贷款金额、期数选择、年利率（年/月切换）、还款方式选择、开始计算按钮。
 */
@Composable
fun LoanInputPanel(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier,
) {
    // 控制底部 Sheet 显示
    var showTermPicker by remember { mutableStateOf(false) }
    var showMethodPicker by remember { mutableStateOf(false) }
    // 年利率输入模式：true=年利率，false=月利率
    var isAnnualRate by remember { mutableStateOf(true) }
    var showRateDropdown by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {

        // ── 贷款金额 ───────────────────────────────────────────────────────────
        LoanInputItem(
            label = stringResource(R.string.loan_calc_amount_label),
            value = viewModel.amount,
            onValueChange = { viewModel.updateAmount(it) },
            hint = stringResource(R.string.loan_calc_amount_hint),
            errorMessage = viewModel.amountError?.let { stringResource(it) },
        )
        Spacer(Modifier.height(12.dp))

        // ── 贷款期数（选择行）─────────────────────────────────────────────────
        LoanInputItem(
            label = stringResource(R.string.loan_calc_term_label),
            value = if (viewModel.termMonths > 0) termLabel(viewModel.termMonths) else "",
            hint = stringResource(R.string.loan_calc_term_hint),
            isSelector = true,
            onClick = { showTermPicker = true },
            errorMessage = viewModel.termError?.let { stringResource(it) },
        )
        Spacer(Modifier.height(12.dp))

        // ── 年利率（带年/月切换下拉）──────────────────────────────────────────
        // 单位符号已包含在 label 中，leadingContent 只显示切换触发器
        LoanInputItem(
            label = if (isAnnualRate) {
                stringResource(R.string.loan_calc_rate_label)
            } else {
                stringResource(R.string.loan_calc_monthly_rate_label)
            },
            value = viewModel.annualRate,
            onValueChange = { input ->
                if (isAnnualRate) {
                    viewModel.updateAnnualRate(input)
                } else {
                    val monthly = input.toDoubleOrNull()
                    viewModel.updateAnnualRate(
                        if (monthly != null) (monthly * 12).toBigDecimal()
                            .stripTrailingZeros().toPlainString()
                        else input
                    )
                }
            },
            hint = if (isAnnualRate) {
                stringResource(R.string.loan_calc_rate_hint)
            } else {
                stringResource(R.string.loan_calc_monthly_rate_hint)
            },
            errorMessage = viewModel.rateError?.let { stringResource(it) },
            leadingContent = {
                // 年/月切换触发器
                Text(
                    text = "▾",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { showRateDropdown = true },
                )
                DropdownMenu(
                    expanded = showRateDropdown,
                    onDismissRequest = { showRateDropdown = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.loan_calc_rate_label)) },
                        onClick = {
                            isAnnualRate = true
                            showRateDropdown = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.loan_calc_monthly_rate_label)) },
                        onClick = {
                            isAnnualRate = false
                            showRateDropdown = false
                        },
                    )
                }
            },
        )
        Spacer(Modifier.height(12.dp))

        // ── 还款方式（选择行）─────────────────────────────────────────────────
        LoanInputItem(
            label = stringResource(R.string.loan_calc_method_label),
            value = viewModel.repaymentMethod?.let { stringResource(it.labelRes) } ?: "",
            hint = stringResource(R.string.loan_calc_method_hint),
            isSelector = true,
            onClick = { showMethodPicker = true },
            errorMessage = viewModel.methodError?.let { stringResource(it) },
        )
        Spacer(Modifier.height(28.dp))

        // ── 开始计算按钮 ───────────────────────────────────────────────────────
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.calculate() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Text(
                text = stringResource(R.string.loan_calc_start),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }

    // ── 底部 Sheet ────────────────────────────────────────────────────────────

    TermPickerSheet(
        visible = showTermPicker,
        selectedMonths = viewModel.termMonths,
        onDismiss = { showTermPicker = false },
        onSelect = { viewModel.updateTermMonths(it) },
    )

    RepaymentMethodSheet(
        visible = showMethodPicker,
        onDismiss = { showMethodPicker = false },
        onSelect = { viewModel.updateRepaymentMethod(it) },
    )
}

/** 将月数格式化为友好的期数标签 */
@Composable
private fun termLabel(months: Int): String {
    val years = months / 12
    val remainMonths = months % 12
    if (years <= 0) {
        return pluralStringResource(R.plurals.loan_term_months, months, months)
    }

    val yearsLabel = pluralStringResource(R.plurals.loan_term_years_count, years, years)
    val displayedMonths = if (remainMonths == 0) months else remainMonths
    val monthsLabel = pluralStringResource(
        R.plurals.loan_term_months,
        displayedMonths,
        displayedMonths,
    )
    return if (remainMonths == 0) {
        stringResource(R.string.loan_term_years, yearsLabel, monthsLabel)
    } else {
        stringResource(R.string.loan_term_years_months, yearsLabel, monthsLabel)
    }
}
