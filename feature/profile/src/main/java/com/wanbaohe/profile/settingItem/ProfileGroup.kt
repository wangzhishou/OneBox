package com.wanbaohe.profile.settingItem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem

/**
 * 「我的」页设置分组卡片。
 *
 * 与页面底部「备份 / 恢复」按钮、胶囊按钮共用 glassMedium + surfaceContainerLow，
 * 保证卡片与按钮是同一套玻璃灰度。
 */
@Composable
fun ProfileGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = OneBoxDesignSystem.sectionCardShape
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape)
            .glassMedium(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = shape,
            )
            .padding(PaddingValues(horizontal = 0.dp, vertical = 16.dp))
    ) {
        content()
    }
}
