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

package com.wanbaohe.recordcenter.registry.di

import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.registry.definitions.BloodGlucoseRecordType
import com.wanbaohe.recordcenter.registry.definitions.BloodLipidRecordType
import com.wanbaohe.recordcenter.registry.definitions.BloodOxygenRecordType
import com.wanbaohe.recordcenter.registry.definitions.BloodPressureRecordType
import com.wanbaohe.recordcenter.registry.definitions.BmiRecordType
import com.wanbaohe.recordcenter.registry.definitions.BodyFatRecordType
import com.wanbaohe.recordcenter.registry.definitions.BodyTemperatureRecordType
import com.wanbaohe.recordcenter.registry.definitions.HeartRateRecordType
import com.wanbaohe.recordcenter.registry.definitions.SleepDurationRecordType
import com.wanbaohe.recordcenter.registry.definitions.UricAcidRecordType
import com.wanbaohe.recordcenter.registry.definitions.WaistCircumferenceRecordType
import com.wanbaohe.recordcenter.registry.definitions.WeightRecordType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 记录类型注册模块 — 通过 @IntoMap + @StringKey 把 12 种类型注册进 RecordTypeCatalog。
 */
@Module
@InstallIn(SingletonComponent::class)
object RecordTypeModule {

    @Provides
    @IntoMap
    @StringKey("heart_rate")
    fun provideHeartRateType(): RecordTypeDefinition = HeartRateRecordType

    @Provides
    @IntoMap
    @StringKey("blood_pressure")
    fun provideBloodPressureType(): RecordTypeDefinition = BloodPressureRecordType

    @Provides
    @IntoMap
    @StringKey("blood_glucose")
    fun provideBloodGlucoseType(): RecordTypeDefinition = BloodGlucoseRecordType

    @Provides
    @IntoMap
    @StringKey("weight")
    fun provideWeightType(): RecordTypeDefinition = WeightRecordType

    @Provides
    @IntoMap
    @StringKey("bmi")
    fun provideBmiType(): RecordTypeDefinition = BmiRecordType

    @Provides
    @IntoMap
    @StringKey("body_fat")
    fun provideBodyFatType(): RecordTypeDefinition = BodyFatRecordType

    @Provides
    @IntoMap
    @StringKey("uric_acid")
    fun provideUricAcidType(): RecordTypeDefinition = UricAcidRecordType

    @Provides
    @IntoMap
    @StringKey("blood_lipid")
    fun provideBloodLipidType(): RecordTypeDefinition = BloodLipidRecordType

    @Provides
    @IntoMap
    @StringKey("blood_oxygen")
    fun provideBloodOxygenType(): RecordTypeDefinition = BloodOxygenRecordType

    @Provides
    @IntoMap
    @StringKey("body_temperature")
    fun provideBodyTemperatureType(): RecordTypeDefinition = BodyTemperatureRecordType

    @Provides
    @IntoMap
    @StringKey("sleep_duration")
    fun provideSleepDurationType(): RecordTypeDefinition = SleepDurationRecordType

    @Provides
    @IntoMap
    @StringKey("waist_circumference")
    fun provideWaistCircumferenceType(): RecordTypeDefinition = WaistCircumferenceRecordType
}
