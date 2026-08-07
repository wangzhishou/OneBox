package com.wanbaohe.profile.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.WeChatConfirmDialog
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.core.constants.Strings
import com.shifenmiao.storage.RemoteConfigStorage
import com.wanbaohe.profile.ui.AboutCardDivider
import com.wanbaohe.profile.ui.AboutGlassCard
import com.wanbaohe.profile.ui.AboutGlassListItem
import com.wanbaohe.profile.ui.AboutSectionTitle
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy

@Composable
fun CommunityScreen(
    appComponent: AppComponent,
    onGoBack: () -> Unit = {},
) {
    val context = LocalContext.current

    val showWechatTipsDialog = remember { mutableStateOf(false) }
    WeChatConfirmDialog(showWechatTipsDialog)

    val remoteConfig = remember { RemoteConfigStorage.getRemoteConfig() }
    val wechatGroupQrcodeUrl = remoteConfig.wechatGroupQrcodeUrl

    BaseScreen(
        title = stringResource(id = R.string.community_title),
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
                        headlineText = stringResource(R.string.community_qq_group),
                        trailingText = stringResource(R.string.community_qq_group_subtitle),
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        onClick = {
                            ActionUtils.joinQQGroup(context, Constants.QQ_GROUP_KEY)
                        },
                    )
                    AboutCardDivider()
                    AboutGlassListItem(
                        headlineText = stringResource(R.string.community_wechat_public),
                        trailingText = Strings.WECHAT_ACCOUNT,
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        onClick = {
                            showWechatTipsDialog.value = true
                        },
                    )
                }
            }

            if (!wechatGroupQrcodeUrl.isNullOrBlank()) {
                item {
                    AboutSectionTitle(
                        text = stringResource(R.string.community_wechat_group),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                    )
                }

                item {
                    AboutGlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.community_wechat_group_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                textAlign = TextAlign.Center,
                            )

                            WeChatGroupQrcodeImage(
                                imageUrl = wechatGroupQrcodeUrl,
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WeChatGroupQrcodeImage(
    imageUrl: String,
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = stringResource(R.string.community_wechat_group),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentScale = ContentScale.FillWidth,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        text = stringResource(R.string.community_wechat_group_loading),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.community_wechat_group_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        success = {
            SubcomposeAsyncImageContent()
        },
    )
}
