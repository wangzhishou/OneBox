/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import com.github.keelar.exprk.Expressions
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import java.math.BigDecimal
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalculate

@Composable
fun CalculatorDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    initialValue: BigDecimal?,
    onValueChange: (BigDecimal) -> Unit
) {
    var calculatorExpression by rememberSaveable(initialValue, visible) {
        mutableStateOf(initialValue?.toString() ?: "")
    }
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        confirmButton = {
            EnhancedButton(
                onClick = {
                    runCatching {
                        Expressions().eval(calculatorExpression)
                    }.onFailure {
                        AppToastHost.showFailureToast(it)
                    }.onSuccess {
                        onValueChange(it)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.calculate)
            )
        },
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate,
                contentDescription = null
            )
        },
        dismissButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        },
        text = {
            OneBoxOutlinedTextField(
                value = calculatorExpression,
                onValueChange = { expr ->
                    calculatorExpression = expr.filter { !it.isWhitespace() }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                placeholder = {
                    Text(
                        text = "(5+5)*10",
                        style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            )
        }
    )
}