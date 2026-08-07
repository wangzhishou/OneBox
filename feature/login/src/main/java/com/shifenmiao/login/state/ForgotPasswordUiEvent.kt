package com.shifenmiao.login.state

sealed class ForgotPasswordUiEvent {
    data class EmailChanged(val inputValue: String) : ForgotPasswordUiEvent()
    data class CodeChanged(val inputValue: String) : ForgotPasswordUiEvent()
    data class NewPasswordChanged(val inputValue: String) : ForgotPasswordUiEvent()
    data class ConfirmPasswordChanged(val inputValue: String) : ForgotPasswordUiEvent()
    data object SendCode : ForgotPasswordUiEvent()
    data object Submit : ForgotPasswordUiEvent()
}
