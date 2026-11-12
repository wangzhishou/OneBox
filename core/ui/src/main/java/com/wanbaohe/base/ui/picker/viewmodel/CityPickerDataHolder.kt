/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.base.ui.picker.viewmodel

import android.content.Context
import com.wanbaohe.base.ui.utils.BaseUIUtils
import com.shifenmiao.model.ui.picker.CountryData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityPickerDataHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _countryData = MutableStateFlow(CountryData(emptyList()))
    val countryData: StateFlow<CountryData> = _countryData

    init {
        initData(context)
    }

    private fun initData(context: Context) {
        scope.launch {
            val jsonString = BaseUIUtils.loadJsonFromAssets(context, "picker/pca.json")
            jsonString?.let {
                _countryData.value = BaseUIUtils.parseProvinces(it)
            }
        }
    }

    fun getDistricts(province: String, city: String): List<String> {
        return countryData.value.provinces
            .find { it.name == province }
            ?.cities
            ?.find { it.name == city }
            ?.districts ?: emptyList()
    }

    fun getCities(province: String): List<String> {
        return countryData.value.provinces
            .find { it.name == province }
            ?.cities
            ?.map { it.name } ?: emptyList()
    }

    fun getProvinces(): List<String> {
        return countryData.value.provinces.map { it.name }
    }
}

