package com.shifenmiao.base.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear

@Composable
fun ClearTextFieldTrailingIcon(
    value: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (value.isEmpty()) return

    IconButton(
        modifier = modifier,
        onClick = onClear,
        enabled = enabled,
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClear,
            contentDescription = stringResource(id = R.string.clear),
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

