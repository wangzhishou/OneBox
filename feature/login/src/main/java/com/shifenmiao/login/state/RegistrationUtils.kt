package com.shifenmiao.login.state

import com.shifenmiao.core.R
import com.shifenmiao.model.login.ErrorState


val emailEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.registration_error_msg_empty_email
)

val userNameEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.registration_error_msg_empty_username
)

val confirmPasswordEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.registration_error_msg_empty_confirm_password
)

val passwordMismatchErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.registration_error_msg_password_mismatch
)

val emailOrUsernameAlreadyTakenErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.registration_error_msg_already_taken
)
