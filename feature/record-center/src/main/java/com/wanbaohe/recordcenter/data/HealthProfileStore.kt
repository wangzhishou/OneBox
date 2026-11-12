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

package com.wanbaohe.recordcenter.data

import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 健康基础信息(性别/年龄/身高/体重),供 AI 解读拼装上下文。
 * 全部字段可选;[HealthProfile.isSet] 为 false 表示从未填写,
 * AI 解读前会引导用户先完善。
 */
data class HealthProfile(
    val gender: Gender = Gender.UNSET,
    val age: Int? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
) {
    val isSet: Boolean
        get() = gender != Gender.UNSET || age != null || heightCm != null || weightKg != null

    enum class Gender { UNSET, MALE, FEMALE }
}

/** MMKV 持久化,进程内 StateFlow 即写即读 */
@Singleton
class HealthProfileStore @Inject constructor() {

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(STORAGE_ID) }

    private val _profile = MutableStateFlow(load())
    val profile: StateFlow<HealthProfile> = _profile.asStateFlow()

    fun save(profile: HealthProfile) {
        mmkv.encode(KEY_GENDER, profile.gender.ordinal)
        profile.age?.let { mmkv.encode(KEY_AGE, it) } ?: mmkv.removeValueForKey(KEY_AGE)
        profile.heightCm?.let { mmkv.encode(KEY_HEIGHT, it) } ?: mmkv.removeValueForKey(KEY_HEIGHT)
        profile.weightKg?.let { mmkv.encode(KEY_WEIGHT, it) } ?: mmkv.removeValueForKey(KEY_WEIGHT)
        _profile.value = profile
    }

    private fun load(): HealthProfile {
        val gender = HealthProfile.Gender.entries
            .getOrElse(mmkv.decodeInt(KEY_GENDER, 0)) { HealthProfile.Gender.UNSET }
        return HealthProfile(
            gender = gender,
            age = if (mmkv.containsKey(KEY_AGE)) mmkv.decodeInt(KEY_AGE) else null,
            heightCm = if (mmkv.containsKey(KEY_HEIGHT)) mmkv.decodeFloat(KEY_HEIGHT) else null,
            weightKg = if (mmkv.containsKey(KEY_WEIGHT)) mmkv.decodeFloat(KEY_WEIGHT) else null,
        )
    }

    private companion object {
        const val STORAGE_ID = "health_profile"
        const val KEY_GENDER = "gender"
        const val KEY_AGE = "age"
        const val KEY_HEIGHT = "height_cm"
        const val KEY_WEIGHT = "weight_kg"
    }
}
