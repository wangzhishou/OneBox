package com.shifenmiao.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.MarkdownContent
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable(onClick = onRetry)
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun CenterErrorBox(
    modifier: Modifier = Modifier,
    errorMessage: String = stringResource(id = R.string.error_message),
    onRetry: () -> Unit = {},
    onGoBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(horizontal = AppTheme.dimens.paddingNormal)
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ErrorBox(
            errorMessage = errorMessage,
            onRetry = {
                onRetry.invoke()
            },
            onGoBack = onGoBack
        )
    }

}

@Composable
fun ErrorBox(
    modifier: Modifier = Modifier,
    errorMessage: String,
    onRetry: () -> Unit = {},
    onGoBack: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .padding(horizontal = AppTheme.dimens.paddingNormal)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(AppTheme.dimens.paddingExtraLarge))
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(160.dp)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
        ) {
            // 第一层图层，使用第一个 Drawable 资源
            Icon(
                painter = painterResource(R.drawable.svg_404),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                tint = AppTheme.colors.getPrimaryColor()
            )

            // 第二层图层，使用第二个 Drawable 资源并居中对齐
            Icon(
                painter = painterResource(R.drawable.sorry),
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .align(Alignment.Center),
                tint = AppTheme.colors.getOnPrimaryColor()
            )
        }
        Spacer(modifier = Modifier.padding(top = AppTheme.dimens.paddingSmall))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .width(160.dp)
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally)
        ) {
            TextButton(
                onClick = {
                    onGoBack.invoke()
                },
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    modifier = Modifier.size(12.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(id = R.string.back_button),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = {
                    onRetry.invoke()
                },
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    modifier = Modifier.size(12.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(id = R.string.button_retry),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.padding(vertical = AppTheme.dimens.paddingNormal))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = AppTheme.shapes.getMediumShape()
        ) {
            SelectionContainer {
                MarkdownContent(
                    message = errorMessage,
                    paddingValues = PaddingValues(AppTheme.dimens.paddingNormal)
                )
            }
        }

    }
}