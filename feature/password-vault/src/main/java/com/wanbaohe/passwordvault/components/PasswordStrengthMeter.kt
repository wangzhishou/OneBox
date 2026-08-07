package com.wanbaohe.passwordvault.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.util.PasswordStrength
import com.wanbaohe.passwordvault.util.PasswordStrengthEvaluator
import com.wanbaohe.passwordvault.util.PasswordStrengthResult

/**
 * 密码强度条 + 文字提示 + 改进建议。
 * 设计为纯展示组件，强度判定交给 [PasswordStrengthEvaluator]。
 */
@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier,
) {
    val result: PasswordStrengthResult = PasswordStrengthEvaluator.evaluate(password)
    val color = PasswordStrength.colorFor(result.level)
    val progress = result.level.score.toFloat() / PasswordStrength.STRONG.score.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.password_vault_strength_title),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(result.level.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        if (result.suggestions.isNotEmpty()) {
            result.suggestions.forEach { suggestionRes ->
                Text(
                    text = "• " + stringResource(suggestionRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
