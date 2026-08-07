package com.shifenmiao.model.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

// 定义全局 LocalSnackBarHostState
val LocalSnackBarHost = staticCompositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState 未初始化")
}