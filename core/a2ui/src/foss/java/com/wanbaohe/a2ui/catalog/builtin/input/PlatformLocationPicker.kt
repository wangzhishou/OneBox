package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifenmiao.model.ui.picker.SelectedCountryData

/**
 * foss(F-Droid)实现:与 Google 渠道同一逻辑——位置输入以可编辑 TextField 为主,
 * 尾部图标唤起的平台选择器在海外不可用(foss 无 GMS/Places),
 * 因此 show() 直接 onCancel,行为等同 Google 渠道 API key 缺失时的降级:
 * 图标为空操作,用户手输地点。不再向海外用户展示中国行政区 CityPicker。
 */
private class FossLocationPickerState : PlatformLocationPickerState {

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit,
    ) = onCancel()

    override fun hide() = Unit
}

@Composable
fun rememberPlatformLocationPickerState(): PlatformLocationPickerState {
    return remember { FossLocationPickerState() }
}
