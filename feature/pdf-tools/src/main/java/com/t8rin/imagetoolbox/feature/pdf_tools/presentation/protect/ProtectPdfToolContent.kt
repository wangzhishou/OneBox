package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.protect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShuffle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.PdfPreviewItem
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.protect.screenLogic.ProtectPdfToolComponent

@Composable
fun ProtectPdfToolContent(
    component: ProtectPdfToolComponent,
) {
    var hidePassword by rememberSaveable { mutableStateOf(true) }
    val passwordsMatch = component.password == component.confirmPassword
    val transformation = if (hidePassword) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    BasePdfToolContent(
        component = component,
        contentPicker = rememberFilePicker(
            mimeType = MimeType.Pdf,
            onSuccess = component::setUri,
        ),
        isPickedAlready = component.initialUri != null,
        canShowScreenData = component.uri != null,
        title = stringResource(R.string.protect_pdf),
        canSave = component.password.isNotEmpty() && passwordsMatch,
        controls = {
            component.uri?.let {
                PdfPreviewItem(
                    uri = it,
                    onRemove = { component.setUri(null) },
                )
                Spacer(Modifier.height(16.dp))
            }
            RoundedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = component.password,
                onValueChange = component::updatePassword,
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = transformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                supportingText = if (component.passwordCopied) {
                    { _ -> Text(stringResource(R.string.password_copied)) }
                } else {
                    null
                },
                startIcon = {
                    EnhancedIconButton(onClick = component::fillGeneratedPassword) {
                        Icon(
                            imageVector = Icons.Outlined.LineShuffle,
                            contentDescription = stringResource(R.string.generate_password),
                        )
                    }
                },
                endIcon = {
                    PasswordVisibilityButton(
                        hidden = hidePassword,
                        onClick = { hidePassword = !hidePassword },
                    )
                },
            )
            Spacer(Modifier.height(8.dp))
            RoundedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = component.confirmPassword,
                onValueChange = component::updateConfirmPassword,
                label = { Text(stringResource(R.string.confirm_password)) },
                singleLine = true,
                isError = component.confirmPassword.isNotEmpty() && !passwordsMatch,
                visualTransformation = transformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                supportingText = if (component.confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { _ -> Text(stringResource(R.string.passwords_do_not_match)) }
                } else {
                    null
                },
                endIcon = {
                    PasswordVisibilityButton(
                        hidden = hidePassword,
                        onClick = { hidePassword = !hidePassword },
                    )
                },
            )
        },
        onFilledPassword = { component.setUri(component.uri) },
    )
}

@Composable
private fun PasswordVisibilityButton(
    hidden: Boolean,
    onClick: () -> Unit,
) {
    EnhancedIconButton(onClick = onClick) {
        Icon(
            imageVector = if (hidden) {
                Icons.Outlined.LineVisibility
            } else {
                Icons.Outlined.LineVisibilityOff
            },
            contentDescription = null,
        )
    }
}
