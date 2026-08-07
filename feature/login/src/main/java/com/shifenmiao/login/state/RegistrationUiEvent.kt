package com.shifenmiao.login.state

/**
 * Registration Screen Events
 */
sealed class RegistrationUiEvent {
    data class EmailChanged(val inputValue: String) : RegistrationUiEvent()
    data class UsernameChanged(val inputValue: String) : RegistrationUiEvent()
    data class PasswordChanged(val inputValue: String) : RegistrationUiEvent()
    data class ConfirmPasswordChanged(val inputValue: String) : RegistrationUiEvent()
    data object Submit : RegistrationUiEvent()
}