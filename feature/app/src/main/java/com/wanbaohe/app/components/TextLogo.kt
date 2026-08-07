package com.wanbaohe.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.modifier.scaleOnTap

@Composable
fun TextLogo() {
    val layoutDirection = LocalLayoutDirection.current
    val localUrlNavigator = LocalUrlNavigator.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column {
            val titleText = remember {
                BuildConfig.VERSION_NAME
            }
            Row(
                modifier = if (BuildConfig.DEBUG) {
                    Modifier.clickable {
                        localUrlNavigator.navigate(
                            Screen.Demo()
                        )
                    }
                } else {
                    Modifier
                }, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(
                        start = WindowInsets
                            .displayCutout
                            .asPaddingValues()
                            .calculateStartPadding(layoutDirection)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Badge(
                    content = {
                        Text(
                            text = if (BuildConfig.DEBUG) "$titleText-" + BuildConfig.FLAVOR.uppercase(
                                LocalLocale.current.platformLocale
                            ) else titleText,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.68f),
                            fontSize = 9.sp
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(0.68f),
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .padding(bottom = 12.dp)
                        .scaleOnTap(
                            onRelease = {
                                if (BuildConfig.DEBUG) AppToastHost.showConfetti()
                            }
                        )
                )
            }
            Text(
                text = stringResource(R.string.app_slogan),
                modifier = Modifier.padding(
                    start = WindowInsets
                        .displayCutout
                        .asPaddingValues()
                        .calculateStartPadding(layoutDirection)
                ),

                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}