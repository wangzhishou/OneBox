package com.shifenmiao.login.state

import com.shifenmiao.model.login.ErrorState

data class ForgotPasswordState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val countdown: Int = 0,
    val isCodeSent: Boolean = false,
    val errorState: ForgotPasswordErrorState = ForgotPasswordErrorState(),
    val isResetSuccessful: Boolean = false
)

data class ForgotPasswordErrorState(
    val emailErrorState: ErrorState = ErrorState(),
    val codeErrorState: ErrorState = ErrorState(),
    val newPasswordErrorState: ErrorState = ErrorState(),
    val confirmPasswordErrorState: ErrorState = ErrorState()
)
