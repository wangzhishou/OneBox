package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.unlock

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.PdfPreviewItem
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.unlock.screenLogic.UnlockPdfToolComponent

@Composable
fun UnlockPdfToolContent(
    component: UnlockPdfToolComponent,
) {
    var hidePassword by rememberSaveable { mutableStateOf(true) }

    BasePdfToolContent(
        component = component,
        contentPicker = rememberFilePicker(
            mimeType = MimeType.Pdf,
            onSuccess = component::setUri,
        ),
        isPickedAlready = component.initialUri != null,
        canShowScreenData = component.uri != null,
        title = stringResource(R.string.unlock_pdf),
        canSave = component.locked == true &&
            component.password.isNotEmpty() &&
            !component.passwordRejected &&
            !component.cannotOpen,
        controls = {
            component.previewUri?.let {
                PdfPreviewItem(
                    uri = it,
                    onRemove = { component.setUri(null) },
                )
                Spacer(Modifier.height(16.dp))
            }
            if (component.cannotOpen) {
                Text(
                    text = stringResource(R.string.pdf_cannot_be_opened),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else when (component.locked) {
                false -> Text(
                    text = stringResource(R.string.pdf_has_no_password),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                true -> {
                    if (component.previewUri == null) {
                        Text(
                            text = stringResource(R.string.pdf_enter_password_to_preview),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    RoundedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.password,
                        onValueChange = component::updatePassword,
                        label = stringResource(R.string.password),
                        singleLine = true,
                        isError = component.passwordRejected,
                        supportingText = if (component.passwordRejected) {
                            { _ -> Text(stringResource(R.string.wrong_password)) }
                        } else {
                            null
                        },
                        visualTransformation = if (hidePassword) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        endIcon = {
                            EnhancedIconButton(onClick = { hidePassword = !hidePassword }) {
                                Icon(
                                    imageVector = if (hidePassword) {
                                        Icons.Outlined.LineVisibility
                                    } else {
                                        Icons.Outlined.LineVisibilityOff
                                    },
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
                null -> Unit
            }
        },
        onFilledPassword = {},
    )
}
