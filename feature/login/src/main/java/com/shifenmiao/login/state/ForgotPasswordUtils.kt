package com.shifenmiao.login.state

import com.shifenmiao.core.R
import com.shifenmiao.model.login.ErrorState

val forgotPasswordEmailEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.forgot_password_error_invalid_email
)

val forgotPasswordCodeEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.forgot_password_error_empty_code
)

val forgotPasswordNewPasswordEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.forgot_password_error_empty_password
)

val forgotPasswordConfirmPasswordMismatchErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.forgot_password_error_password_mismatch
)
