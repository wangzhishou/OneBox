package com.wanbaohe.loancalculator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.loancalculator.ui.input.LoanInputPanel
import com.wanbaohe.loancalculator.ui.output.LoanOutputPanel
import com.wanbaohe.loancalculator.viewmodel.LoanViewModel

/**
 * 贷款计算器入口 Composable。
 * 基于 [BaseScreen] 扩展，组合输入面板和结果面板。
 */
@Composable
fun LoanCalculatorScreen(
    onGoBack: () -> Unit,
    viewModel: LoanViewModel = viewModel(),
) {
    BaseScreen(
        title = stringResource(R.string.loan_calc_title),
        onGoBack = onGoBack,
    ) {
        LoanCalculatorContent(viewModel = viewModel)
    }
}

@Composable
private fun LoanCalculatorContent(
    viewModel: LoanViewModel,
) {
    val scrollState = rememberScrollState()

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        // 输入面板（贷款金额、期数、利率、还款方式、计算按钮）
        LoanInputPanel(viewModel = viewModel)

        // 计算结果（只在计算完成后展示）
        viewModel.result?.let { result ->
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(top = 28.dp)
            )
            LoanOutputPanel(result = result)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLoanCalculatorScreen() {
    LoanCalculatorScreen(onGoBack = {})
}
