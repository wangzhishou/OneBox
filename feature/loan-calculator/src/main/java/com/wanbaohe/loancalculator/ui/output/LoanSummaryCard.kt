package com.wanbaohe.loancalculator.ui.output

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.loancalculator.R
import com.wanbaohe.loancalculator.domain.LoanResult
import com.wanbaohe.loancalculator.viewmodel.fenToYuanString

/**
 * 计算结果摘要卡片（贷款金额、月均还款、总利息、还款总额）
 */
@Composable
fun LoanSummaryCard(
    result: LoanResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp),
    ) {
        // 副标题：还款方式
        Text(
            text = result.method.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(4.dp))

        // 月均还款（首月）
        Text(
            text = result.firstPayment.fenToYuanString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.loan_result_monthly_payment),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(16.dp))

        // 次要指标：总利息 + 还款总额
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryMetric(
                label = stringResource(R.string.loan_result_total_interest),
                value = result.totalInterest.fenToYuanString(),
            )
            SummaryMetric(
                label = stringResource(R.string.loan_result_total_repayment),
                value = result.totalRepayment.fenToYuanString(),
                alignEnd = true,
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    alignEnd: Boolean = false,
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
        )
    }
}

