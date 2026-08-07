package com.shifenmiao.webview.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import coil3.compose.SubcomposeAsyncImage
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear

@Composable
fun BrowserTopBar(
    addressText: String,
    onAddressTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    faviconUrl: String,
    onGoBack: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        containerAlpha = 0.2f,
        borderWidth = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = !isSearchFocused,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                IconButton(onClick = onGoBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.browser_back),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (!isSearchFocused) {
                Spacer(modifier = Modifier.width(4.dp))
            }

            GlassOutlinedTextField(
                value = addressText,
                onValueChange = onAddressTextChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onSearchFocusChange(it.isFocused) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                style = GlassStyle.Thin,
                visualPreset = GlassTextFieldVisualPreset.Expressive,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                leadingIcon = {
                    SubcomposeAsyncImage(
                        model = faviconUrl.takeIf { it.isNotEmpty() },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        loading = { FallbackSearchIcon() },
                        error = { FallbackSearchIcon() }
                    )
                },
                placeholder = {
                    Text(text = stringResource(R.string.browser_search_or_url), style = MaterialTheme.typography.bodyMedium)
                },
                trailingIcon = {
                    if (isSearchFocused && addressText.isNotBlank() && addressText != "about:blank") {
                        IconButton(
                            onClick = { onAddressTextChange("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClear,
                                contentDescription = stringResource(R.string.browser_clear),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onSubmit() })
            )

            if (!isSearchFocused) {
                Spacer(modifier = Modifier.width(4.dp))
            }

            AnimatedVisibility(
                visible = !isSearchFocused,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                IconButton(
                    onClick = if (isLoading) onStop else onReload,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLoading) Icons.Outlined.Stop else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = stringResource(if (isLoading) R.string.browser_stop else R.string.browser_refresh),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackSearchIcon() {
    Icon(
        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
    )
}
