package com.shifenmiao.login.state

/**
 * Login Screen Events
 */
sealed class LoginUiEvent {
    data class EmailOrMobileChanged(val inputValue: String) : LoginUiEvent()
    data class PasswordChanged(val inputValue: String) : LoginUiEvent()
    data class VerificationCodeChanged(val inputValue: String) : LoginUiEvent()
    data object Submit : LoginUiEvent()
    data object SubmitCode : LoginUiEvent()
}