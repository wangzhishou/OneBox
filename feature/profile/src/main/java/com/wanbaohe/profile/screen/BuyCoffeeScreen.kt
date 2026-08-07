package com.wanbaohe.profile.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalScreenSize
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.wanbaohe.profile.components.AnimatedViewPager
import com.wanbaohe.profile.components.AppTextInfo
import com.wanbaohe.profile.viewmodel.PayComponent
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkspacePremium
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShoppingCheckout

@Composable
fun BuyCoffeeScreen(
    appComponent: AppComponent,
    loginComponent: LoginComponent,
    payComponent: PayComponent,
    onGoBack: () -> Unit = {},
    onNavigateToVipLevel: () -> Unit = {},
) {
    BuyCoffeeContainer(
        loginComponent = loginComponent,
        payComponent = payComponent,
        onGoBack = onGoBack,
        onNavigateToVipLevel = onNavigateToVipLevel,
    )
    BackHandler {
        appComponent.onGoBack()
    }
}

@Composable
fun BuyCoffeeContainer(
    loginComponent: LoginComponent,
    payComponent: PayComponent,
    onGoBack: () -> Unit,
    onNavigateToVipLevel: () -> Unit = {},
) {
    BaseScreen(
        title = stringResource(id = R.string.buy_coffee_title),
        onGoBack = onGoBack,
        supportGlassEffect = true,
        isBackHandler = false,
    ) {
        BuyCoffeeBody(
            loginComponent = loginComponent,
            payComponent = payComponent,
            onNavigateToVipLevel = onNavigateToVipLevel,
        )
    }
}

@Composable
fun ColumnScope.BuyCoffeeBody(
    modifier: Modifier = Modifier,
    loginComponent: LoginComponent,
    payComponent: PayComponent,
    onNavigateToVipLevel: () -> Unit = {},
    topContent: @Composable LazyItemScope.() -> Unit = {
        AppTextInfo()
    }
) {
    val context = LocalContext.current
    val payPriceState = remember { mutableStateOf<PayPrice>(PayPrice.WuMaoPrice) }
    val selectedPayment = payComponent.selectedPayment
    val coroutineScope = rememberCoroutineScope()
    val loginState = LocalLoginState.current
    val payInfoError by payComponent.errorState.collectAsState()
    LaunchedEffect(payInfoError) {
        if (payInfoError.isNotEmpty()) {
            payInfoError.forEach { tips ->
                coroutineScope.launch {
                    AppToastHost.showToast(tips)
                }
            }
            payComponent.clearErrors()
        }
    }
    val isLoading = payComponent.loadingState
    LazyColumn(
        modifier
            .weight(1f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            if (loginState.isLogin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AppTheme.dimens.paddingNormal,
                            end = AppTheme.dimens.paddingNormal,
                            bottom = AppTheme.dimens.paddingSmall
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.current_points, loginState.points),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    VipLevelBadge(
                        vipLevel = loginState.vipLevel,
                        modifier = Modifier.clickable { onNavigateToVipLevel() },
                    )
                }
            } else {
                Text(
                    modifier = Modifier.padding(
                        start = AppTheme.dimens.paddingNormal,
                        end = AppTheme.dimens.paddingNormal,
                        bottom = AppTheme.dimens.paddingSmall
                    ),
                    text = stringResource(id = R.string.current_points, loginState.points),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        item {
            topContent()
        }
        item {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingLarge))
            BuildLoginSlider(
                modifier = Modifier.fillMaxWidth(),
                onPayPriceSelected = { payPrice ->
                    payPriceState.value = payPrice
                },
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        }
        item {
            Text(
                modifier = Modifier.padding(
                    horizontal = AppTheme.dimens.paddingNormal,
                    vertical = AppTheme.dimens.paddingSmall
                ),
                text = stringResource(id = R.string.donate_mode),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                PaymentMethodSelector(payComponent)
            }
        }
        item {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                GlassButton(
                    onClick = {
                        val handlePayment = {
                            payPriceState.value.userId = loginState.userId
                            payComponent.prePayInfo(
                                context,
                                selectedPayment,
                                payPriceState.value,
                                loginComponent,
                            )
                        }
                        if (loginState.isLogin) {
                            handlePayment.invoke()
                        } else {
                            ActionUtils.showLogin(
                                source = "BuyCoffeeScreen",
                                onSuccess = {
                                    handlePayment.invoke()
                                },
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .requiredHeight(AppTheme.dimens.normalButtonHeight)
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShoppingCheckout,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.donate_confirm),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    payComponent: PayComponent
) {
    val context = LocalContext.current
    if (context is Activity) {
        PaymentOptionsSelector(payComponent.getPaymentList()) { paymentMethod ->
            payComponent.setPaymentMethod(paymentMethod)
        }
    }
}

@Composable
fun PaymentOptionsSelector(
    paymentOptions: List<PaymentMethod<PrePayResponse>>,
    onOptionSelected: (PaymentMethod<PrePayResponse>) -> Unit
) {
    var selectedPayment by remember { mutableStateOf(paymentOptions.firstOrNull()) }
    if (selectedPayment == null) return
    Row(
        Modifier
            .selectableGroup()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        paymentOptions.forEach { option ->
            Row(
                modifier = Modifier.clickable {
                    selectedPayment = option
                    onOptionSelected(option)
                },
            ) {
                RadioButton(
                    selected = selectedPayment?.id == option.id,
                    onClick = {
                        selectedPayment = option
                        onOptionSelected(option)
                    },
                )
                Icon(
                    painter = painterResource(id = option.getIcon()),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.graphicsLayer(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
fun BuildLoginSlider(
    modifier: Modifier = Modifier,
    onPayPriceSelected: (PayPrice) -> Unit,
) {
    val screenWidth = LocalScreenSize.current.width
    val priceList = PayPrice.priceEntries.toMutableList()

    val pageWidth = remember(screenWidth) {
        if (screenWidth > 0.dp) {
            screenWidth / 3f
        } else {
            100.dp
        }
    }

    if (priceList.isNotEmpty() && pageWidth > 0.dp) {
        AnimatedViewPager(
            modifier = modifier,
            pageSize = pageWidth,
            priceList = priceList,
            onPayPriceSelected = onPayPriceSelected,
        )
    }
}

@Composable
fun VipLevelBadge(
    vipLevel: Int,
    modifier: Modifier = Modifier,
) {
    val levelInfo = vipLevelList.getOrNull(vipLevel) ?: vipLevelList[0]
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkspacePremium,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "VIP $vipLevel · ${stringResource(levelInfo.nameRes)}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
