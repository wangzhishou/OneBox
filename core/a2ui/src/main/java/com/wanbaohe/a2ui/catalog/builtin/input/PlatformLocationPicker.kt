package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.runtime.Stable
import com.shifenmiao.model.ui.picker.SelectedCountryData

/**
 * 平台位置选择器,按渠道 flavor 隔离实现(同名工厂函数 `rememberPlatformLocationPickerState`,
 * sourceSet 隔离,同 core/r、core/ui 的做法):
 * - 国内渠道(src/domestic): 行政区滚轮 CityPicker,选省/市/区
 * - Google 渠道(src/google): Google Places Autocomplete,全球地点搜索
 */
@Stable
interface PlatformLocationPickerState {

    fun show(
        title: String? = null,
        initData: SelectedCountryData? = null,
        initLayer: Int = 2,
        onCancel: () -> Unit = {},
        onChange: (SelectedCountryData) -> Unit = {},
    )

    fun hide()
}
