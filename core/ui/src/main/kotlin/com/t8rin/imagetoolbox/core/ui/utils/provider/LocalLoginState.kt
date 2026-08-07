package com.t8rin.imagetoolbox.core.ui.utils.provider

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.model.login.LoginState

val LocalLoginState = compositionLocalOf<LoginState> { error("LoginState not present") }
