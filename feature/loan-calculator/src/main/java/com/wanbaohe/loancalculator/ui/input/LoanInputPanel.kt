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
            errorMessage = viewModel.amountError,
        )
        Spacer(Modifier.height(12.dp))

        // ── 贷款期数（选择行）─────────────────────────────────────────────────
        LoanInputItem(
            label = stringResource(R.string.loan_calc_term_label),
            value = if (viewModel.termMonths > 0) termLabel(viewModel.termMonths) else "",
            hint = stringResource(R.string.loan_calc_term_hint),
            isSelector = true,
            onClick = { showTermPicker = true },
            errorMessage = viewModel.termError,
        )
        Spacer(Modifier.height(12.dp))

        // ── 年利率（带年/月切换下拉）──────────────────────────────────────────
        // label 只写"年利率"/"月利率"，单位符号由 leadingContent 承载，避免重复显示
        LoanInputItem(
            label = if (isAnnualRate) "年利率" else "月利率",
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
            hint = if (isAnnualRate)
                stringResource(R.string.loan_calc_rate_hint)
            else
                "请输入月利率",
            errorMessage = viewModel.rateError,
            leadingContent = {
                // 年/月切换触发器，显示"（%）▾"
                Text(
                    text = "（%）▾",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { showRateDropdown = true },
                )
                DropdownMenu(
                    expanded = showRateDropdown,
                    onDismissRequest = { showRateDropdown = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("年利率（%）") },
                        onClick = {
                            isAnnualRate = true
                            showRateDropdown = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("月利率（%）") },
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
            value = viewModel.repaymentMethod?.label ?: "",
            hint = stringResource(R.string.loan_calc_method_hint),
            isSelector = true,
            onClick = { showMethodPicker = true },
            errorMessage = viewModel.methodError,
        )
        Spacer(Modifier.height(28.dp))

        // ── 开始计算按钮 ───────────────────────────────────────────────────────
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.calculate() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
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
private fun termLabel(months: Int): String {
    val years = months / 12
    val remainMonths = months % 12
    return when {
        years > 0 && remainMonths == 0 -> "${years}年（${months}个月）"
        years > 0 -> "${years}年${remainMonths}个月"
        else -> "${months}个月"
    }
}
