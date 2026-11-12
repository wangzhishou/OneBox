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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.webview.WebViewParams
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.glass.GlassCheckbox
import com.wanbaohe.core.ui.widget.text.HtmlText
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
        Spacer(modifier = Modifier.width(8.dp))
        HtmlText(
            html = getString(R.string.user_privacy).trimIndent(),
            style = MaterialTheme.typography.labelSmall.copy(
                textDecoration = TextDecoration.None,
                color = MaterialTheme.colorScheme.onSurface
            ),
            enableAutoLinkify = false,
            hyperlinkStyle = MaterialTheme.typography.labelSmall.copy(
                textDecoration = TextDecoration.None,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
    GlassCheckbox(
        checked = isUserAgreementChecked.value,
        onCheckedChange = {
            isUserAgreementChecked.value = it
        },
    )
}