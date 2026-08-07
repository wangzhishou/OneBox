package com.shifenmiao.login.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ErrorTextInputField
import com.shifenmiao.base.ui.clearFocusOnKeyboardDismiss
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVerifiedUser


@Composable
fun PhoneBody(
    phoneNumber: MutableState<String>,
    codeNumber: MutableState<String>,
    loginComponent: LoginComponent,
    sendType: Int = 0
) {
    val isError = remember { mutableStateOf(false) }
    val sendOkPhone = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    GlassOutlinedTextField(
        modifier = Modifier
            .clearFocusOnKeyboardDismiss()
            .focusRequester(focusRequester)
            .padding(top = AppTheme.dimens.paddingSmall)
            .fillMaxWidth(),
        value = phoneNumber.value,
        onValueChange = { newValue: String ->
            phoneNumber.value = newValue
            if (sendOkPhone.value.isNotEmpty() && sendOkPhone.value != newValue) {
                sendOkPhone.value = ""
            }
        },
        leadingIcon = {
            Text(text = "+86", color = MaterialTheme.colorScheme.primary)
        },
        placeholder = {
            Text(text = stringResource(R.string.phone_placeholder))
        },
        label = {
            Text(text = stringResource(id = R.string.input_phone_number))
        },
        singleLine = true,
        isError = isError.value,
        supportingText = {
            if (isError.value) {
                ErrorTextInputField(text = stringResource(id = R.string.phone_number_error))
            }
        },
        trailingIcon = {
            if (sendOkPhone.value.isNotEmpty()) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVerifiedUser,
                    contentDescription = "VerifiedUser",
                    tint = AppTheme.colors.getPrimaryColor()
                )
            }
        },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )
    LaunchedEffect(isFocused) {
        if (!isFocused && phoneNumber.value.isNotEmpty()) {
            isError.value = !StringUtils.isValidChinesePhoneNumber(phoneNumber.value)
        } else {
            isError.value = false
        }
    }
    Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isCodeError = remember { mutableStateOf(false) }
        val codeInteractionSource = remember { MutableInteractionSource() }
        val codeIsFocused = codeInteractionSource.collectIsFocusedAsState().value
        GlassOutlinedTextField(
            modifier = Modifier.width(160.dp),
            value = codeNumber.value,
            onValueChange = { newValue: String ->
                codeNumber.value = newValue
            },
            placeholder = {
                Text(text = stringResource(R.string.code_number))
            },
            label = {
                Text(text = stringResource(id = R.string.input_code_number))
            },
            isError = isCodeError.value,
            supportingText = {
                if (isCodeError.value) {
                    ErrorTextInputField(text = stringResource(id = R.string.code_number_error))
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = AppTheme.colors.getOutlinedTextFieldColors(),
            interactionSource = codeInteractionSource,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        LaunchedEffect(codeIsFocused) {
            if (!codeIsFocused && codeNumber.value.isNotEmpty()) {
                isCodeError.value = !StringUtils.isValidCode(codeNumber.value)
            } else {
                isCodeError.value = false
            }
        }
        Spacer(modifier = Modifier.width(AppTheme.dimens.paddingExtraSmall))
        SendCodeButton(
            phoneNumber = phoneNumber.value,
            validatedFail = {
                isError.value = true
            },
            loginComponent = loginComponent,
            onSendSuccess = {
                sendOkPhone.value = phoneNumber.value
            },
            sendType = sendType
        )
    }

}

@Composable
fun SendCodeButton(
    phoneNumber: String,
    loginComponent: LoginComponent,
    validatedFail: () -> Unit,
    onSendSuccess: () -> Unit,
    sendType: Int,
) {

    val codeSent = remember { mutableStateOf(false) }
    val countdown = remember { mutableIntStateOf(0) }
    val timer = remember { mutableStateOf<Job?>(null) }

    if (countdown.intValue > 0) {
        LaunchedEffect(countdown.intValue) {
            delay(1000L)
            countdown.intValue -= 1
        }
    }

    GlassTonalButton(
        onClick = {
            if (StringUtils.isValidChinesePhoneNumber(phoneNumber)) {
                loginComponent.sendCode(
                    value = phoneNumber,
                    sendType = sendType,
                    onError = {
                        AppToastHost.showToast(it, icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError)
                    }
                ) {
                    onSendSuccess.invoke()
                    codeSent.value = true
                    countdown.intValue = 60
                    timer.value?.cancel()
                    timer.value = CoroutineScope(Dispatchers.Main).launch {
                        while (countdown.intValue > 0) {
                            delay(1000L)
                            countdown.intValue -= 1
                        }
                        codeSent.value = false
                    }
                }
            } else {
                validatedFail.invoke()
            }
        },
        enabled = countdown.intValue == 0,
        colors = AppTheme.colors.getSecondaryContainerButtonColors()
    ) {
        Text(
            text = if (countdown.intValue > 0) {
                stringResource(R.string.get_code_ed) + " (${countdown.intValue}s)"
            } else {
                stringResource(R.string.get_code)
            }
        )
    }
}