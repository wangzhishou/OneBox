package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.viewmodel.CityPickerDataHolder
import com.shifenmiao.base.ui.popup.MaskedPopup
import com.shifenmiao.model.ui.picker.SelectedCountryData
import com.shifenmiao.theme.AppTheme

val LocalCityPickerDataHolder = compositionLocalOf<CityPickerDataHolder> {
    error("No CityPickerDataHolder provided")
}

@Stable
interface CityPickerState {
    val isVisible: Boolean

    fun show(
        title: String? = null,
        initData: SelectedCountryData?,
        initialProvinceIndex: Int = 0,
        initialCityIndex: Int = 0,
        initialDistrictIndex: Int = 0,
        initLayer: Int = 2,
        onCancel: () -> Unit = {},
        onChange: (SelectedCountryData) -> Unit
    )

    fun hide()
}

private data class CityPickerProperties(
    val title: String?,
    val initData: SelectedCountryData?,
    val initialProvinceIndex: Int = 0,
    val initialCityIndex: Int = 0,
    val initialDistrictIndex: Int = 0,
    val initLayer: Int = 2,
    val onCancel: () -> Unit,
    val onChange: (SelectedCountryData) -> Unit
)

private class CityPickerStateImpl : CityPickerState {

    override var isVisible by mutableStateOf(false)
    var properties by mutableStateOf<CityPickerProperties?>(null)
        private set

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initialProvinceIndex: Int,
        initialCityIndex: Int,
        initialDistrictIndex: Int,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit
    ) {
        properties = CityPickerProperties(
            title,
            initData,
            initialProvinceIndex,
            initialCityIndex,
            initialDistrictIndex,
            initLayer,
            onCancel,
            onChange
        )
        isVisible = true
    }

    override fun hide() {
        isVisible = false
    }
}

@Composable
fun rememberCityPickerState(): CityPickerState {
    val state = remember { CityPickerStateImpl() }
    val cityPickerDataHolder = LocalCityPickerDataHolder.current

    state.properties?.let { props ->
        CityPicker(
            cityPickerDataHolder = cityPickerDataHolder,
            title = props.title,
            isVisible = state.isVisible,
            initData = props.initData,
            initialProvinceIndex = props.initialProvinceIndex,
            initialCityIndex = props.initialCityIndex,
            initialDistrictIndex = props.initialDistrictIndex,
            initLayer = props.initLayer,
            onCancel = {
                state.hide()
                props.onCancel()
            },
            onValuesChange = props.onChange
        )
    }
    return state
}

@Composable
fun CityPicker(
    cityPickerDataHolder: CityPickerDataHolder,
    isVisible: Boolean,
    initData: SelectedCountryData? = null,
    title: String? = null,
    onCancel: () -> Unit = {},
    onValuesChange: (SelectedCountryData) -> Unit = {},
    initialProvinceIndex: Int = 0,
    initialCityIndex: Int = 0,
    initialDistrictIndex: Int = 0,
    initLayer: Int = 2
) {
    val countryDataState = cityPickerDataHolder.countryData.collectAsState()
    val selectedData = remember { mutableStateOf(SelectedCountryData()) }
    var selectedProvinceIndex by remember { mutableIntStateOf(initialProvinceIndex) }
    var selectedCityIndex by remember { mutableIntStateOf(initialCityIndex) }
    var selectedDistrictIndex by remember { mutableIntStateOf(initialDistrictIndex) }

    val provinces = countryDataState.value.provinces

    LaunchedEffect(initData, initLayer, provinces) {
        if (provinces.isEmpty()) return@LaunchedEffect

        if (initData != null) {
            selectedProvinceIndex = if (initData.province != null) {
                provinces.indexOfFirst { it.name == initData.province }.takeIf { it >= 0 } ?: 0
            } else {
                0
            }
            if (initLayer > 1) {
                selectedCityIndex = if (initData.city != null) {
                    provinces.getOrNull(selectedProvinceIndex)?.cities?.indexOfFirst {
                        it.name == initData.city
                    }?.takeIf { it >= 0 } ?: 0
                } else {
                    0
                }
            }
            if (initLayer > 2) {
                selectedDistrictIndex = if (initData.district != null) {
                    provinces.getOrNull(selectedProvinceIndex)?.cities?.getOrNull(selectedCityIndex)
                        ?.districts?.indexOf(initData.district)?.takeIf { it >= 0 } ?: 0
                } else {
                    0
                }
            }
        }
        updateSelectedData(
            provinces = provinces,
            selectedProvinceIndex = selectedProvinceIndex,
            selectedCityIndex = selectedCityIndex,
            selectedDistrictIndex = selectedDistrictIndex,
            initLayer = initLayer,
            selectedData = selectedData.value,
        )
    }

    MaskedPopup(
        visible = isVisible,
        onDismissRequest = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        bottomStart = CornerSize(0.0.dp),
                        bottomEnd = CornerSize(0.0.dp)
                    )
                )
                .padding(
                    vertical = AppTheme.dimens.paddingNormal,
                    horizontal = AppTheme.dimens.paddingNormal
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppTheme.dimens.paddingNormal),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (provinces.isEmpty()) {
                return@Column
            }
            Box(
                modifier = Modifier
                    .height(280.dp)
                    .drawIndicator(
                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.5f)
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColumnItem(
                        options = provinces.map { it.name },
                        index = selectedProvinceIndex
                    ) {
                        selectedProvinceIndex = it
                        if (initLayer > 1) {
                            selectedCityIndex = 0
                        }
                        if (initLayer > 2) {
                            selectedDistrictIndex = 0
                        }
                        updateSelectedData(
                            provinces = provinces,
                            selectedProvinceIndex = selectedProvinceIndex,
                            selectedCityIndex = selectedCityIndex,
                            selectedDistrictIndex = selectedDistrictIndex,
                            initLayer = initLayer,
                            selectedData = selectedData.value,
                        )
                    }

                    if (initLayer > 1) {
                        if (selectedProvinceIndex < provinces.size) {
                            ColumnItem(
                                options = provinces[selectedProvinceIndex].cities.map { it.name },
                                index = selectedCityIndex
                            ) {
                                selectedCityIndex = it
                                if (initLayer > 2) {
                                    selectedDistrictIndex = 0
                                }
                                updateSelectedData(
                                    provinces = provinces,
                                    selectedProvinceIndex = selectedProvinceIndex,
                                    selectedCityIndex = selectedCityIndex,
                                    selectedDistrictIndex = selectedDistrictIndex,
                                    initLayer = initLayer,
                                    selectedData = selectedData.value,
                                )
                            }
                        }
                    }

                    if (initLayer > 2) {
                        if (selectedProvinceIndex < provinces.size &&
                            selectedCityIndex < provinces[selectedProvinceIndex].cities.size
                        ) {
                            ColumnItem(
                                options = provinces[selectedProvinceIndex].cities[selectedCityIndex].districts,
                                index = selectedDistrictIndex
                            ) {
                                selectedDistrictIndex = it
                                updateSelectedData(
                                    provinces = provinces,
                                    selectedProvinceIndex = selectedProvinceIndex,
                                    selectedCityIndex = selectedCityIndex,
                                    selectedDistrictIndex = selectedDistrictIndex,
                                    initLayer = initLayer,
                                    selectedData = selectedData.value,
                                )
                            }
                        }
                    }
                }
                Mask()
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            ActionBar(onCancel) {
                onValuesChange(selectedData.value)
                onCancel()
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        }
    }
}

private fun updateSelectedData(
    provinces: List<com.shifenmiao.model.ui.picker.ProvinceData>,
    selectedProvinceIndex: Int,
    selectedCityIndex: Int,
    selectedDistrictIndex: Int,
    initLayer: Int,
    selectedData: SelectedCountryData,
) {
    if (selectedProvinceIndex < provinces.size) {
        val province = provinces[selectedProvinceIndex]
        selectedData.province = province.name

        if (selectedCityIndex < province.cities.size) {
            val city = province.cities[selectedCityIndex]
            if (initLayer > 1) {
                selectedData.city = city.name
            }

            if (selectedDistrictIndex < city.districts.size && initLayer > 2) {
                selectedData.district = city.districts[selectedDistrictIndex]
            } else {
                selectedData.district = null
            }
        } else {
            selectedData.city = null
            selectedData.district = null
        }
    } else {
        selectedData.province = null
        selectedData.city = null
        selectedData.district = null
    }
}
