package com.shifenmiao.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.clearFocusOnKeyboardDismiss
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Close

@Composable
fun TopSearchBar(
    queryValue: MutableState<String>,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchFocusChange: (Boolean) -> Unit = {},
    appComponent: AppComponent,
    isShowBack: Boolean = true,
    autoFocus: Boolean = true,
) {
    val focus = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    // Launch effect for initial focus if needed
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    LaunchedEffect(focused) {
        onSearchFocusChange(focused)
    }
    val isShowEndIcon by remember { derivedStateOf { queryValue.value.isNotEmpty() } }
    GlassOutlinedTextField(
        colors = AppTheme.colors.getOutlinedTextFieldColors(),
        visualPreset = GlassTextFieldVisualPreset.Expressive,
        shape = MaterialTheme.shapes.large,
        maxLines = 1,
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_hint),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        textStyle = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clearFocusOnKeyboardDismiss()
            .padding(
                horizontal = AppTheme.dimens.paddingNormal,
                vertical = AppTheme.dimens.paddingNormal
            )
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search,
            autoCorrectEnabled = null
        ),
        value = queryValue.value,
        onValueChange = {
            onQueryChange(it)
        },
        leadingIcon = {
            if (isShowBack) {
                IconButton(
                    onClick = {
                        queryValue.value = ""
                        onQueryChange("")
                        focus.clearFocus()
                        onSearchFocusChange(false)
                        appComponent.onGoBack()
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        null
                    )
                }
            }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = isShowEndIcon,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    },
                    modifier = Modifier
                        .padding(1.dp)
                        .width(16.dp)
                        .height(16.dp)
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close, null)
                }
            }
        },
        interactionSource = interactionSource
    )
}