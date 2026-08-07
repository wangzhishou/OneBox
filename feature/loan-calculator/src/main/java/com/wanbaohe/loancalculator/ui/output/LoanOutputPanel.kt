package com.wanbaohe.loancalculator.ui.output

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.loancalculator.R
import com.wanbaohe.loancalculator.domain.LoanResult

/**
 * 结果区布局：摘要卡片 + 还款计划表标题 + 还款计划表。
 * 注意：不要自己套 ScrollState，由外层 Screen 统一控制滚动。
 */
@Composable
fun LoanOutputPanel(
    result: LoanResult,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 摘要卡片
        LoanSummaryCard(result = result)

        Spacer(Modifier.height(20.dp))

        // 还款计划表标题
        Text(
            text = stringResource(R.string.loan_result_schedule_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // 还款计划表（固定高度，内部自滚动）
        RepaymentScheduleTable(
            items = result.schedule,
            modifier = Modifier
                .fillMaxWidth()
                .height((48 * result.schedule.size.coerceAtMost(12) + 48).dp),
        )
    }
}

