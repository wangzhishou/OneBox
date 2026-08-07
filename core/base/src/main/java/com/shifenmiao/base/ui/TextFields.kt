package com.shifenmiao.base.ui


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff

/**
 * Password Text Field
 */
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorText: String = "",
    onClearValue: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Done
) {
    val localFocusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }
    GlassOutlinedTextField(
        modifier = modifier
            .clearFocusOnKeyboardDismiss()
            .focusRequester(focusRequester),
        value = value,
        onValueChange = onValueChange,
        label = {
            LabelText(text = label)
        },
        trailingIcon = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (value.isNotEmpty() && onClearValue != null) {
                    ClearTextFieldTrailingIcon(
                        value = value,
                        onClear = onClearValue,
                    )
                }
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                }) {

                    val visibleIconAndText = Pair(
                        first = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                        second = stringResource(id = R.string.icon_password_visible)
                    )

                    val hiddenIconAndText = Pair(
                        first = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff,
                        second = stringResource(id = R.string.icon_password_hidden)
                    )

                    val passwordVisibilityIconAndText =
                        if (isPasswordVisible) visibleIconAndText else hiddenIconAndText

                    Icon(
                        imageVector = passwordVisibilityIconAndText.first,
                        contentDescription = passwordVisibilityIconAndText.second
                    )
                }
            }
        },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = {
            localFocusManager.clearFocus()
        }),
        isError = isError,
        supportingText = {
            if (isError) {
                ErrorTextInputField(text = errorText)
            }
        },
        shape = AppTheme.shapes.getTextFieldShape()
    )
}

/**
 * Email Text Field
 */
@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Next
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val inputValueText = remember { mutableStateOf(value) }
    GlassOutlinedTextField(
        modifier = modifier
            .clearFocusOnKeyboardDismiss()
            .focusRequester(focusRequester),
        value = inputValueText.value,
        onValueChange = {
            inputValueText.value = it
            onValueChange(it)
        },
        label = {
            LabelText(text = label)
        },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                ErrorTextInputField(text = errorText)
            }
        },
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        )
    )

}

/**
 * Mobile Number Text Field
 */
@Composable
fun UsernameTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Next
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    GlassOutlinedTextField(
        modifier = modifier
            .clearFocusOnKeyboardDismiss()
            .focusRequester(focusRequester),
        value = value,
        onValueChange = onValueChange,
        label = {
            LabelText(text = label)
        },
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                ErrorTextInputField(text = errorText)
            }
        },
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        shape = AppTheme.shapes.getTextFieldShape()
    )

}

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
    )
}