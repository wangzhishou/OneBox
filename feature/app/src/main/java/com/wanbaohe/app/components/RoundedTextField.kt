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

package com.wanbaohe.app.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.text.RoundedTextField as CoreRoundedTextField
import com.wanbaohe.core.ui.widget.text.RoundedTextFieldColors as CoreRoundedTextFieldColors

@Composable
fun RoundedTextField(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    label: String = "",
    hint: String = "",
    shape: Shape = RoundedCornerShape(16.dp),
    startIcon: ImageVector? = null,
    value: String,
    isError: Boolean = false,
    loading: Boolean = false,
    supportingText: (@Composable (isError: Boolean) -> Unit)? = null,
    supportingTextVisible: Boolean = true,
    endIcon: (@Composable () -> Unit)? = null,
    formatText: String.() -> String = { this },
    textStyle: TextStyle = LocalTextStyle.current,
    onLoseFocusTransformation: String.() -> String = { this },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    colors: TextFieldColors = RoundedTextFieldColors(isError),
    enabled: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    minLines: Int = 1
) {
    val labelImpl = @Composable {
        Text(
            text = label,
            modifier = if (singleLine) Modifier else Modifier.offset(y = 4.dp)
        )
    }
    val hintImpl = @Composable {
        Text(text = hint, modifier = Modifier.padding(start = 4.dp))
    }
    val leadingIconImpl = @Composable {
        Icon(
            imageVector = startIcon!!,
            contentDescription = null
        )
    }

    CoreRoundedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it.formatText()) },
        textStyle = textStyle,
        colors = colors,
        shape = shape,
        singleLine = singleLine,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        endIcon = endIcon,
        startIcon = if (startIcon != null) leadingIconImpl else null,
        label = if (label.isNotEmpty()) labelImpl else null,
        hint = if (hint.isNotEmpty()) hintImpl else null,
        isError = isError,
        loading = loading,
        supportingText = supportingText,
        supportingTextVisible = supportingTextVisible,
        formatText = formatText,
        onLoseFocusTransformation = onLoseFocusTransformation,
        keyboardActions = keyboardActions,
        enabled = enabled,
        maxLines = maxLines,
        interactionSource = interactionSource,
        minLines = minLines
    )
}

@Composable
fun RoundedTextField(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    label: (@Composable () -> Unit)? = null,
    hint: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    startIcon: (@Composable () -> Unit)? = null,
    value: String,
    isError: Boolean = false,
    loading: Boolean = false,
    supportingText: (@Composable (isError: Boolean) -> Unit)? = null,
    supportingTextVisible: Boolean = true,
    endIcon: (@Composable () -> Unit)? = null,
    formatText: String.() -> String = { this },
    textStyle: TextStyle = LocalTextStyle.current,
    onLoseFocusTransformation: String.() -> String = { this },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    colors: TextFieldColors = RoundedTextFieldColors(isError),
    enabled: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    minLines: Int = 1
) {
    CoreRoundedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        hint = hint,
        shape = shape,
        startIcon = startIcon,
        isError = isError,
        loading = loading,
        supportingText = supportingText,
        supportingTextVisible = supportingTextVisible,
        endIcon = endIcon,
        formatText = formatText,
        textStyle = textStyle,
        onLoseFocusTransformation = onLoseFocusTransformation,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        readOnly = readOnly,
        colors = colors,
        enabled = enabled,
        maxLines = maxLines,
        interactionSource = interactionSource,
        minLines = minLines,
    )
}

@Composable
fun RoundedTextFieldColors(isError: Boolean): TextFieldColors = CoreRoundedTextFieldColors(isError)
