package com.wanbaohe.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.StringUtils.formatPriceWithUnit
import com.shifenmiao.core.R
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.profile.components.modifier.pagerAnimation
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
internal fun AnimatedViewPager(
    modifier: Modifier = Modifier,
    pageSize: Dp,
    priceList: List<PayPrice>,
    onPayPriceSelected: (PayPrice) -> Unit,
) {
    // 防止空列表导致崩溃
    if (priceList.isEmpty()) {
        return
    }

    // 确保 initialPage 在有效范围内
    val safeInitialPage = PayPrice.START_INDEX.coerceIn(0, priceList.size - 1)

    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        initialPageOffsetFraction = 0f,
        pageCount = { priceList.size },
    )
    val coroutineScope = rememberCoroutineScope()
    var currentPageIndex by remember { mutableIntStateOf(safeInitialPage) }
    val hapticFeedback = LocalHapticFeedback.current

    // 初始化时触发选中回调
    LaunchedEffect(Unit) {
        onPayPriceSelected(priceList[safeInitialPage])
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            if (currentPageIndex != currentPage) {
                hapticFeedback.performHapticFeedback(
                    hapticFeedbackType = HapticFeedbackType.LongPress,
                )
                currentPageIndex = currentPage
                // 页面变化时通知选中
                if (currentPage in priceList.indices) {
                    onPayPriceSelected(priceList[currentPage])
                }
            }
        }
    }

    HorizontalPager(
        modifier = modifier.height(180.dp), // 添加固定高度确保 pager 可见
        state = pagerState,
        contentPadding = PaddingValues(horizontal = pageSize),
        verticalAlignment = Alignment.CenterVertically,
    ) { thisPageIndex ->
        // 移除在渲染时调用 onPayPriceSelected，避免重复调用和重组问题
        PageLayout(
            modifier = Modifier
                .pagerAnimation(
                    pagerState = pagerState,
                    thisPageIndex = thisPageIndex,
                ),
            payPrice = priceList[thisPageIndex],
        )
    }
    Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
    Text(
        modifier = Modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingSmall
        ),
        text = stringResource(id = R.string.donate_prices),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )

    // Display the TabRow with tabs
    SecondaryScrollableTabRow(
        selectedTabIndex = currentPageIndex,
        indicator = {},
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,
        divider = {
        },
        edgePadding = 16.dp
    ) {
        priceList.forEachIndexed { index, price ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .glassRegular(
                        shape = RoundedCornerShape(8.dp),
                        color = if (pagerState.currentPage == index) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ),
                text = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (pagerState.currentPage == index) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = formatPriceWithUnit(price.price) + stringResource(id = R.string.unit),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                }
            )
        }
    }
}
