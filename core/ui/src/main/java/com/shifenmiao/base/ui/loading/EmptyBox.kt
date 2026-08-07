package com.shifenmiao.base.ui.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme

@Composable
fun EmptyBox(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.load_empty_toast),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onTextClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier.padding(AppTheme.dimens.spaceLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.5.dp,
                color = textColor.copy(0.3f)
            )
            TextButton(
                onClick = {
                    onTextClick.invoke()
                }
            ) {
                Text(
                    text = text,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceLarge)
                )
            }
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.5.dp,
                color = textColor.copy(0.3f)
            )
        }
    }
}