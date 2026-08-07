package com.shifenmiao.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.net.toUri
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.login.ui.PhoneBody
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.text.HtmlText

@Composable
fun BindPhoneSheet(
    loginComponent: LoginComponent,
    appComponent: AppComponent
) {
    val loginState = LocalLoginState.current
    if (loginState.showBind) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = {
                loginComponent.hideBind()
            },
            dragHandle = {
                CenterAlignedTopAppBar(
                    title = {
                        IdentityVerificationText()
                    },
                    navigationIcon = {
                    },
                    actions = {
                    }
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .fillMaxSize()
                    .padding(horizontal = AppTheme.dimens.paddingNormal)
            ) {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
                Text(
                    text = stringResource(id = R.string.login_bind_phone),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingExtraSmall))
                val context = LocalComponentActivity.current
                HtmlText(
                    html = context.getString(R.string.login_bind_phone_desc),
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    hyperlinkStyle = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.None,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    enableAutoLinkify = false
                ) { uriString ->
                    val uri = uriString.toUri()
                    appComponent.showWebView(
                        WebViewParams(
                            title = uri.host ?: "",
                            url = uriString,
                            enableShare = false,
                            enableCustomTouch = false,
                        )
                    )
                }
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
                val phoneNumber = remember { mutableStateOf("") }
                val codeNumber = remember { mutableStateOf("") }
                PhoneBody(
                    phoneNumber = phoneNumber,
                    codeNumber = codeNumber,
                    loginComponent = loginComponent
                )
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
                Button(
                    onClick = {
                        if (StringUtils.isValidChinesePhoneNumber(phoneNumber.value)
                            && StringUtils.isValidCode(codeNumber.value)
                        ) {
                            loginComponent.bindPhone(
                                phone = phoneNumber.value,
                                code = codeNumber.value,
                                onSuccess = {
                                    loginComponent.hideBind()
                                },
                                onFail = {
                                    ActionUtils.showError(it)
                                }
                            )
                        }
                    },
                    colors = AppTheme.colors.getPrimaryButtonColors()
                ) {
                    Text(
                        text = stringResource(id = R.string.button_bind),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}


@Composable
fun IdentityVerificationText() {
    Text(
        text = stringResource(id = R.string.identity_verification),
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
