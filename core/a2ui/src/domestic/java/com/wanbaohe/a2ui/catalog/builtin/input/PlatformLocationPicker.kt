package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifenmiao.base.ui.picker.CityPickerState
import com.shifenmiao.base.ui.picker.rememberCityPickerState
import com.shifenmiao.model.ui.picker.SelectedCountryData

private class DomesticLocationPickerState(
    private val delegate: CityPickerState,
) : PlatformLocationPickerState {

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit,
    ) {
        delegate.show(
            title = title,
            initData = initData,
            initLayer = initLayer,
            onCancel = onCancel,
            onChange = onChange,
        )
    }

    override fun hide() = delegate.hide()
}

@Composable
fun rememberPlatformLocationPickerState(): PlatformLocationPickerState {
    val cityPicker = rememberCityPickerState()
    return remember(cityPicker) { DomesticLocationPickerState(cityPicker) }
}
