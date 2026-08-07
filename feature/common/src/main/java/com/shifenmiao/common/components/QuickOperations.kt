package com.shifenmiao.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawer


@Composable
fun QuickOperations(
    appComponent: AppComponent
) {
    IconButton(onClick = {
        appComponent.onNavigate(Screen.Search())
    }) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    IconButton(onClick = {
        if (!RateLimiter.isFastClick()) {
            appComponent.toggleDrawer()
        }
    }) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

