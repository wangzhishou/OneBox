package com.wanbaohe.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

@Composable
fun ProfileLogin(
    onClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
) {
    val loginState = LocalLoginState.current
    val onNavigate = LocalOnNavigate.current
    Row(
        modifier = Modifier
            .clickable {
                onClick.invoke()
                if (loginState.isLogin) {
                    onNavigate(Screen.UserInfo())
                } else {
                    onNavigate(Screen.Login())
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Avatar(
            username = loginState.username,
            avatar = loginState.avatar,
            isLogin = loginState.isLogin
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = BaseUtils.getDisplayName(loginState.nickname, loginState.username),
                color = AppTheme.colors.getOnPrimaryColor(),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_user_info_points) + ":" + StringUtils.formatNumber(
                        loginState.points
                    ),
                    color = AppTheme.colors.getOnPrimaryColor().copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        trailingContent?.invoke()
    }
}

