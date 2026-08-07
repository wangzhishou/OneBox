package com.wanbaohe.app.components

import androidx.compose.foundation.background
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.TopActions

@Composable
fun AppLargeTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    appComponent: AppComponent,
) {
    LargeTopAppBar(
        title = {
            TextLogo()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = modifier
            .background(Color.Transparent),
        scrollBehavior = scrollBehavior,
        actions = {
            TopActions(appComponent)
        }
    )
}