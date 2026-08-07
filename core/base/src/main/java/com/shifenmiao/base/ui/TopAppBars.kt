package com.shifenmiao.base.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun AppCenterAlignedTopAppBar(
    titleText: String = "",
    onBackClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = Color.Transparent,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = titleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        ),
        actions = actions,
        windowInsets = windowInsets
    )
}