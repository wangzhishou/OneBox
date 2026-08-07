package com.shifenmiao.login.state

import com.shifenmiao.core.R
import com.shifenmiao.model.login.ErrorState


val emailOrMobileEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.login_error_msg_empty_email_mobile
)

val passwordEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.login_error_msg_empty_password
)

val codeEmptyErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.login_error_msg_empty_verification_code
)

val codeLengthErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.login_error_msg_verification_code_length
)

var codeIsDigitErrorState = ErrorState(
    hasError = true,
    errorMessageStringResource = R.string.login_error_msg_invalid_verification_code
)