package com.wanbaohe.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChangeCircle

@Composable
fun ItemHeader(
    data: Triple<Int, ImageVector, ImageVector>,
    modifier: Modifier,
    onChange: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = data.second,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = data.first),
            modifier = Modifier.padding(start = AppTheme.dimens.spaceExtraSmall),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onChange != null) {
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onChange,
                    modifier = Modifier.size(14.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChangeCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.getPrimaryColor()
                    )
                }
                Text(
                    text = stringResource(id = R.string.see_random),
                    modifier = Modifier.padding(AppTheme.dimens.spaceExtraSmall),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.getPrimaryColor()
                )
            }
        }
    }
}