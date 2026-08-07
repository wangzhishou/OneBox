package com.wanbaohe.profile.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.webview.WebViewParams
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.liquidGlassRegular
import com.wanbaohe.profile.components.AppTextInfo
import com.wanbaohe.profile.ui.AboutCardDivider
import com.wanbaohe.profile.ui.AboutGlassCard
import com.wanbaohe.profile.ui.AboutGlassListItem
import com.wanbaohe.profile.ui.AboutSectionTitle
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos

@Composable
fun AdditionalInfo(
    showAdditionalInfo: MutableState<Boolean>,
    appComponent: AppComponent,
) {
    if (!showAdditionalInfo.value) return

    val deviceInfo = remember { appComponent.getDeviceInfo() }
    AnimatedVisibility(
        visible = showAdditionalInfo.value,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        SelectionContainer {
            AboutGlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 14.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "VersionName: ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "VersionCode: ${BuildConfig.VERSION_CODE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "DeviceInfo: ${deviceInfo.toString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "GitVersion: ${BuildConfig.GitVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "BuildType: ${BuildConfig.BUILD_TYPE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "Debug: ${BuildConfig.BUILD_TYPE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                    if (deviceInfo != null) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Channel: ${deviceInfo.channel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                        )
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "Flavor: ${BuildConfig.FLAVOR}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                    )
                }
            }
        }
    }
}

@Composable
fun AboutUsScreen(
    appComponent: AppComponent,
    onGoBack: () -> Unit = {},
    showContactUs: Boolean = false,
) {
    if (showContactUs) {
        ContactUsScreen(appComponent, onGoBack)
    } else {
        AboutUsContainer(appComponent, onGoBack)
    }
}

@Composable
fun AboutUsContainer(
    appComponent: AppComponent,
    onGoBack: () -> Unit = {},
) {
    val context = LocalComponentActivity.current
    val channelConfig: ChannelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    val showAdditionalInfo = remember { mutableStateOf(false) }

    val onNavigate = LocalOnNavigate.current

    BaseScreen(
        title = stringResource(id = R.string.profile_setting_about_us),
        onGoBack = onGoBack,
        supportGlassEffect = true
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                AboutHero(
                    showAdditionalInfo = showAdditionalInfo,
                    onOpenEasterEgg = { onNavigate(Screen.EasterEgg) },
                )
            }
            item {
                AdditionalInfo(showAdditionalInfo = showAdditionalInfo, appComponent = appComponent)
            }
            item {
                AppTextInfo(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }

            item {
                AboutGlassCard {
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.profile_item_about_model),
                        trailingText = "",
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        onClick = { onNavigate(Screen.AboutAIModel) },
                    )
                }
            }

            item {
                AboutSectionTitle(
                    text = stringResource(R.string.profile_group_about),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                )
            }
            item {
                AboutGlassCard {
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.user_agreement),
                        trailingText = "",
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        onClick = {
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(
                                        title = context.getString(R.string.user_agreement),
                                        url = channelConfig.userAgreementUrl,
                                    ),
                                ),
                            )
                        },
                    )
                    AboutCardDivider()
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.privacy_title),
                        trailingText = "",
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        onClick = {
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(
                                        title = context.getString(R.string.privacy_title),
                                        url = channelConfig.privacyPolicyUrl,
                                    ),
                                ),
                            )
                        },
                    )
                    // ICP 备案入口仅国内渠道显示,海外渠道无此监管要求
                    if (UrlConstants.SHOW_BEI_AN_ENTRY) {
                        AboutCardDivider()
                        AboutGlassListItem(
                            headlineText = stringResource(R.string.profile_item_beian),
                            trailingText = UrlConstants.BEI_AN_NUMBER,
                            trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                            onClick = {
                                Clipboard.copy(UrlConstants.BEI_AN_NUMBER)
                                onNavigate(
                                    Screen.WebView(
                                        WebViewParams(
                                            title = context.getString(R.string.profile_item_beian),
                                            url = UrlConstants.BEI_AN_QUERY,
                                        ),
                                    ),
                                )
                            },
                        )
                    }
                }
            }

            item {
                AboutSectionTitle(
                    text = stringResource(R.string.profile_group_support),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                )
            }
            item {
                AboutGlassCard {
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.contact_us),
                        trailingText = "",
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        onClick = {
                            onNavigate(Screen.AboutUs(showContactUs = true))
                        },
                    )
                    AboutCardDivider()
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.third_party),
                        trailingText = "",
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                        onClick = { onNavigate(Screen.LibrariesInfo) },
                    )
                }
            }

            item {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.app_copyright),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AboutHero(
    showAdditionalInfo: MutableState<Boolean>,
    onOpenEasterEgg: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { showAdditionalInfo.value = !showAdditionalInfo.value })
            },
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(100.dp)
                .liquidGlassRegular(
                    shape = RoundedCornerShape(28.dp),
                    borderWidth = 0.8.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.logo),
                contentDescription = "App Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(72.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .liquidGlassRegular(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    borderWidth = 0.8.dp,
                )
                .pointerInput(onOpenEasterEgg) {
                    detectTapGestures(onLongPress = { onOpenEasterEgg() })
                },
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                text = "V${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
