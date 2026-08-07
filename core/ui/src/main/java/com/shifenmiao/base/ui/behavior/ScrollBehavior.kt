package com.shifenmiao.base.ui.behavior

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable

@Composable
fun customExitUntilCollapsedScrollBehavior(): TopAppBarScrollBehavior {
    val topAppBarState = rememberTopAppBarState(

    )
    topAppBarState.heightOffsetLimit = 0f
    return TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
}