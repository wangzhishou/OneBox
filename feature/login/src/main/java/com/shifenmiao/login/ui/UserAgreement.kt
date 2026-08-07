package com.shifenmiao.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import com.shifenmiao.core.R
import com.shifenmiao.model.webview.WebViewParams
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.text.HtmlText
import com.t8rin.imagetoolbox.core.utils.getString

@Composable
fun UserAgreement(
    isUserAgreementChecked: MutableState<Boolean>,
) {
    val onNavigate = LocalOnNavigate.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        UserAgreementCheckbox(
            isUserAgreementChecked = isUserAgreementChecked,
        )
        HtmlText(
            html = getString(R.string.user_privacy).trimIndent(),
            style = MaterialTheme.typography.labelSmall.copy(
                textDecoration = TextDecoration.None,
                color = MaterialTheme.colorScheme.onSurface
            ),
            enableAutoLinkify = false,
            hyperlinkStyle = MaterialTheme.typography.labelSmall.copy(
                textDecoration = TextDecoration.None,
                color = MaterialTheme.colorScheme.primary
            ),
        ) { uri ->
            onNavigate(
                Screen.WebView(
                    WebViewParams(
                        title = getString(R.string.privacy_title),
                        url = uri,
                    )
                )
            )
        }
    }
}

@Composable
fun UserAgreementCheckbox(
    isUserAgreementChecked: MutableState<Boolean>,
) {
    Checkbox(
        checked = isUserAgreementChecked.value,
        onCheckedChange = {
            isUserAgreementChecked.value = it
        },
        colors = CheckboxDefaults.colors(
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            checkedColor = MaterialTheme.colorScheme.primary,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}