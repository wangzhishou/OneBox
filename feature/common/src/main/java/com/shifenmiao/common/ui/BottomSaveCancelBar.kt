package com.shifenmiao.common.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxBottomActionBar

@Composable
fun BottomSaveCancelBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String? = null,
    cancelEnabled: Boolean = true,
    saveEnabled: Boolean = true,
    extraActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val resolvedSaveText = saveText ?: stringResource(R.string.settings_confirm)

    OneBoxBottomActionBar(
        modifier = modifier,
        primaryText = resolvedSaveText,
        onPrimaryClick = onSave,
        primaryEnabled = saveEnabled,
        secondaryText = stringResource(R.string.button_cancel),
        onSecondaryClick = onCancel,
        secondaryEnabled = cancelEnabled,
        extraActions = extraActions,
    )
}
