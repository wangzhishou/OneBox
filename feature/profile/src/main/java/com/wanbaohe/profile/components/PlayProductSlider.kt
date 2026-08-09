package com.wanbaohe.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.pay.google.PlayProduct
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.profile.components.modifier.pagerAnimation
import kotlinx.coroutines.launch

/**
 * Google Play Billing 商品选择器(google 渠道充值页用):
 * 展示后端商品目录的积分点数 + Play 本地化价格, 交互与国内渠道的人民币档位滑动选择一致
 */
@Composable
internal fun PlayProductSlider(
    modifier: Modifier = Modifier,
    pageSize: Dp,
    products: List<PlayProduct>,
    loadError: String? = null,
    onProductSelected: (PlayProduct) -> Unit,
) {
    if (products.isEmpty()) {
        // 加载中给转圈占位; 加载失败(含设备不支持结算)展示失败原因, 避免一直转圈
        Row(
            modifier = modifier.height(180.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loadError != null) {
                Text(
                    text = loadError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingNormal),
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { products.size },
    )
    val coroutineScope = rememberCoroutineScope()
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        onProductSelected(products[0])
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            if (currentPageIndex != currentPage) {
                hapticFeedback.performHapticFeedback(
                    hapticFeedbackType = HapticFeedbackType.LongPress,
                )
                currentPageIndex = currentPage
                if (currentPage in products.indices) {
                    onProductSelected(products[currentPage])
                }
            }
        }
    }

    HorizontalPager(
        modifier = modifier.height(180.dp),
        state = pagerState,
        contentPadding = PaddingValues(horizontal = pageSize),
        verticalAlignment = Alignment.CenterVertically,
    ) { thisPageIndex ->
        PlayProductCard(
            modifier = Modifier.pagerAnimation(
                pagerState = pagerState,
                thisPageIndex = thisPageIndex,
            ),
            product = products[thisPageIndex],
        )
    }
    Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))

    SecondaryScrollableTabRow(
        selectedTabIndex = currentPageIndex,
        indicator = {},
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,
        divider = {},
        edgePadding = 16.dp
    ) {
        products.forEachIndexed { index, product ->
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
                    Row(horizontalArrangement = Arrangement.Center) {
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
                            text = product.formattedPrice,
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

@Composable
private fun PlayProductCard(
    modifier: Modifier = Modifier,
    product: PlayProduct,
) {
    GlassCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(AppTheme.dimens.paddingNormal)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = product.formattedPrice,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.play_points_desc, product.points),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
