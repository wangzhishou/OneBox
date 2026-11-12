/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import com.wanbaohe.base.ui.EmailTextField
import com.wanbaohe.base.ui.PasswordTextField
import com.wanbaohe.base.ui.button.PrimaryButton
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
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )

        // Login Submit Button
        PrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingNormal)
                .requiredHeight(AppTheme.dimens.normalButtonHeight),
            enable = !loginState.isLoggingIn,
            text = stringResource(id = R.string.login_button_text),
            onClick = onSubmit
        )

        // Register Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Don't have an account?
            Text(
                text = stringResource(id = R.string.do_not_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            //Register
            Text(
                modifier = Modifier
                    .padding(start = AppTheme.dimens.paddingExtraSmall)
                    .clickable {
                        onNavigateToRegistration.invoke()
                    },
                text = stringResource(id = R.string.register),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

    }
}