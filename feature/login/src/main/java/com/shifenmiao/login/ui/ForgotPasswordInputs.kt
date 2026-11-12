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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.EmailTextField
import com.wanbaohe.base.ui.ErrorTextInputField
import com.wanbaohe.base.ui.PasswordTextField
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.clearFocusOnKeyboardDismiss
import com.shifenmiao.core.R
import com.shifenmiao.login.state.ForgotPasswordState
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

@Composable
fun ForgotPasswordInputs(
    forgotPasswordState: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EmailTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingLarge),
            value = forgotPasswordState.email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.forgot_password_email_hint),
            isError = forgotPasswordState.errorState.emailErrorState.hasError,
            errorText = stringResource(id = forgotPasswordState.errorState.emailErrorState.errorMessageStringResource),
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.padding(top = AppTheme.dimens.paddingNormal))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val focusManager = LocalFocusManager.current
            GlassOutlinedTextField(
                modifier = Modifier
                    .width(160.dp)
                    .clearFocusOnKeyboardDismiss(),
                value = forgotPasswordState.code,
                onValueChange = onCodeChange,
                label = {
                    Text(text = stringResource(id = R.string.forgot_password_code_hint))
                },
                singleLine = true,
                isError = forgotPasswordState.errorState.codeErrorState.hasError,
                supportingText = {
                    if (forgotPasswordState.errorState.codeErrorState.hasError) {
                        ErrorTextInputField(text = stringResource(id = forgotPasswordState.errorState.codeErrorState.errorMessageStringResource))
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Spacer(modifier = Modifier.width(AppTheme.dimens.paddingExtraSmall))

            GlassTonalButton(
                onClick = onSendCode,
                enabled = forgotPasswordState.countdown == 0,
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                shape = OneBoxDesignSystem.pillShape,
            ) {
                Text(
                    text = if (forgotPasswordState.countdown > 0) {
                        stringResource(R.string.forgot_password_resend_code, forgotPasswordState.countdown)
                    } else {
                        stringResource(R.string.forgot_password_send_code)
                    }
                )
            }
        }

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingLarge),
            value = forgotPasswordState.newPassword,
            onValueChange = onNewPasswordChange,
            label = stringResource(id = R.string.forgot_password_new_password_hint),
            isError = forgotPasswordState.errorState.newPasswordErrorState.hasError,
            errorText = stringResource(id = forgotPasswordState.errorState.newPasswordErrorState.errorMessageStringResource),
            imeAction = ImeAction.Next,
        )

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingLarge),
            value = forgotPasswordState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(id = R.string.forgot_password_confirm_password_hint),
            isError = forgotPasswordState.errorState.confirmPasswordErrorState.hasError,
            errorText = stringResource(id = forgotPasswordState.errorState.confirmPasswordErrorState.errorMessageStringResource),
        )

        PrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingExtraLarge)
                .requiredHeight(AppTheme.dimens.normalButtonHeight),
            text = stringResource(id = R.string.forgot_password_submit),
            onClick = onSubmit
        )
    }
}
