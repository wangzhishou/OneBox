package com.wanbaohe.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.WeChatConfirmDialog
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Strings
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.wechat.Wechat
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.wanbaohe.profile.ui.AboutCardDivider
import com.wanbaohe.profile.ui.AboutGlassCard
import com.wanbaohe.profile.ui.AboutGlassListItem
import com.wanbaohe.profile.ui.AboutSectionTitle
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos

@Composable
fun ContactUsScreen(
    appComponent: AppComponent,
    onGoBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val channelConfig: ChannelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    val enableWechat = channelConfig.enableWechat

    val showWechatTipsDialog = remember { mutableStateOf(false) }
    if (enableWechat) {
        WeChatConfirmDialog(showWechatTipsDialog)
    }

    val showWechatAccountTipsDialog = remember { mutableStateOf(false) }
    if (enableWechat) {
        WeChatConfirmDialog(
            showDialogState = showWechatAccountTipsDialog,
            copValue = Strings.WECHAT_ACCOUNT,
            message = stringResource(R.string.my_wechat_account_tips),
        )
    }

    val onNavigate = LocalOnNavigate.current
    BaseScreen(
        title = stringResource(id = R.string.contact_us),
        onGoBack = onGoBack,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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
                        headlineText = stringResource(R.string.profile_item_official_website),
                        trailingText = UrlConstants.OFFICIAL_WEBSITE,
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                        onClick = {
                            onNavigate(
                                Screen.WebView(
                                    WebViewParams(
                                        title = AppContext.getString(R.string.profile_item_official_website),
                                        url = UrlConstants.OFFICIAL_WEBSITE,
                                    ),
                                ),
                            )
                        },
                    )
                    AboutCardDivider()
                    if (enableWechat) {
                        AboutGlassListItem(
                            headlineText = stringResource(R.string.profile_item_wechat),
                            trailingText = Strings.WECHAT_ACCOUNT,
                            trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            onClick = {
                                showWechatTipsDialog.value = true
                            },
                        )
                        AboutCardDivider()
                        AboutGlassListItem(
                            headlineText = stringResource(R.string.my_wechat),
                            trailingText = Strings.WECHAT_ACCOUNT_ID,
                            trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            onClick = {
                                showWechatAccountTipsDialog.value = true
                            },
                        )
                        AboutCardDivider()
                    }
                    if (enableWechat) {
                        AboutGlassListItem(
                            headlineText = stringResource(R.string.qq_group),
                            trailingText = Strings.QQ_GROUP_NUMBER,
                            trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            onClick = {
                                Clipboard.copy(Strings.QQ_GROUP_NUMBER)
                                com.shifenmiao.base.utils.ActionUtils.joinQQGroup(
                                    context,
                                    "1JOfn5KCue56UhXT1fRe6NgCLJB5sHFO",
                                )
                            },
                        )
                        AboutCardDivider()
                    }
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.email),
                        trailingText = UrlConstants.EMAIL,
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        onClick = {
                            Clipboard.copy(UrlConstants.EMAIL)
                        },
                    )
                }
            }

            if (enableWechat) {
                item {
                    AboutGlassCard {
                        AboutGlassListItem(
                            headlineText = stringResource(R.string.profile_item_wechat_service),
                            trailingText = "",
                            trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                            onClick = {
                                Wechat.launchCustomerService()
                            },
                        )
                    }
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}