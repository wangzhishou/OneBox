package com.shifenmiao.login.state

import com.shifenmiao.model.login.ErrorState


/**
 * Registration State holding ui input values
 */
data class RegistrationState(
    val emailId: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorState: RegistrationErrorState = RegistrationErrorState(),
    val isRegistrationSuccessful: Boolean = false
)

/**
 * Error state in registration holding respective
 * text field validation errors
 */
data class RegistrationErrorState(
    val emailIdErrorState: ErrorState = ErrorState(),
    val usernameErrorState: ErrorState = ErrorState(),
    val passwordErrorState: ErrorState = ErrorState(),
    val confirmPasswordErrorState: ErrorState = ErrorState()
)