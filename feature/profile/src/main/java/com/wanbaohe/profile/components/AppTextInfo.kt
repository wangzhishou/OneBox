package com.wanbaohe.profile.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.theme.AppTheme

@Composable
fun AppTextInfo(
    modifier: Modifier = Modifier.padding(16.dp)
) {
    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.donate_description,
            AppContext.getContext().getString(R.string.app_name)
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun NeedCoffeeTextInfo(
    modifier: Modifier = Modifier.padding(
        horizontal = AppTheme.dimens.paddingNormal,
        vertical = AppTheme.dimens.paddingSmall
    )
) {
    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.need_coffee_description
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}