package com.shifenmiao.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.shifenmiao.common.components.QuickOperations
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.isShellPortraitOrientationAsState

@Composable
fun TopActions(
    appComponent: AppComponent
) {

    val isPortrait by isShellPortraitOrientationAsState()
    if (isPortrait) {
        QuickOperations(appComponent)
    }
}