package com.wanbaohe.loancalculator.ui.output

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wanbaohe.loancalculator.R
import com.wanbaohe.loancalculator.domain.InstallmentItem
import com.wanbaohe.loancalculator.viewmodel.fenToYuanString

/**
 * 还款计划表（5列：期次/月还款/本金/利息/剩余本金）。
 * 使用 LazyColumn 渲染大量期数时依然流畅。
 */
@Composable
fun RepaymentScheduleTable(
    items: List<InstallmentItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        // 表头
        item {
            TableHeader()
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
        // 数据行
        itemsIndexed(items) { index, item ->
            val bgColor = if (index % 2 == 0)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceContainerLowest

            TableRow(item = item, bgColor = bgColor)
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(stringResource(R.string.loan_result_month), weight = 0.12f)
        HeaderCell(stringResource(R.string.loan_result_monthly_col), weight = 0.22f)
        HeaderCell(stringResource(R.string.loan_result_principal_col), weight = 0.22f)
        HeaderCell(stringResource(R.string.loan_result_interest_col), weight = 0.22f)
        HeaderCell(stringResource(R.string.loan_result_remaining_col), weight = 0.22f)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TableRow(
    item: InstallmentItem,
    bgColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DataCell(text = "${item.month}", weight = 0.12f, fontWeight = FontWeight.Medium)
        DataCell(text = item.payment.fenToYuanString(), weight = 0.22f)
        DataCell(text = item.principal.fenToYuanString(), weight = 0.22f)
        DataCell(text = item.interest.fenToYuanString(), weight = 0.22f)
        DataCell(text = item.remaining.fenToYuanString(), weight = 0.22f)
    }
}

@Composable
private fun RowScope.DataCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

