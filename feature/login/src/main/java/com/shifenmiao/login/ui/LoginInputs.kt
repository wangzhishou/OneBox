package com.shifenmiao.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.EmailTextField
import com.shifenmiao.base.ui.PasswordTextField
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme

@Composable
fun LoginInputs(
    loginState: LoginState,
    onEmailOrMobileChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {

    val emailOrMobileInputText = remember { mutableStateOf(loginState.emailOrMobile) }

    // Login Inputs Section
    Column(modifier = Modifier.fillMaxWidth()) {

        // Email or Mobile Number
        EmailTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = emailOrMobileInputText.value,
            onValueChange = {
                emailOrMobileInputText.value = it
                onEmailOrMobileChange.invoke(it)
            },
            label = stringResource(id = R.string.login_email_id_or_phone_label),
            isError = loginState.errorState.emailOrMobileErrorState.hasError,
            errorText = stringResource(id = loginState.errorState.emailOrMobileErrorState.errorMessageStringResource)
        )


        val passwordInputText = remember { mutableStateOf(loginState.password) }
        // Password
        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = passwordInputText.value,
            onValueChange = {
                passwordInputText.value = it
                onPasswordChange.invoke(it)
            },
            label = stringResource(id = R.string.login_password_label),
            isError = loginState.errorState.passwordErrorState.hasError,
            errorText = stringResource(id = loginState.errorState.passwordErrorState.errorMessageStringResource),
        )

        // Forgot Password
        Text(
            modifier = Modifier
                .align(alignment = Alignment.End)
                .clickable {
                    onForgotPasswordClick.invoke()
                },
            text = stringResource(id = R.string.forgot_password),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )

        // Register Section
        Row(
            modifier = Modifier.padding(0.dp, AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Login Submit Button
            PrimaryButton(
                enable = !loginState.isLoggingIn,
                text = stringResource(id = R.string.login_button_text),
                onClick = onSubmit
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.paddingNormal))

            // Don't have an account?
            Text(text = stringResource(id = R.string.do_not_have_account))

            //Register
            Text(
                modifier = Modifier
                    .padding(start = AppTheme.dimens.paddingExtraSmall)
                    .clickable {
                        onNavigateToRegistration.invoke()
                    },
                text = stringResource(id = R.string.register),
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}