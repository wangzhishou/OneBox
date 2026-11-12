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

package com.shifenmiao.storage

import com.shifenmiao.model.DeviceInfo
import com.tencent.mmkv.MMKV

object DeviceInfoStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.DEVICE)
    private var uniqueId: String = ""

    private const val KEY_UNIQUE_ID = "key_unique_id"

    fun saveDeviceInfoLocalStorage(deviceInfo: DeviceInfo) {
        mmkv.encode(KEY_UNIQUE_ID, deviceInfo)
    }

    fun getDeviceInfoFromLocalStorage(): DeviceInfo? {
        val deviceInfo = mmkv.decodeParcelable(KEY_UNIQUE_ID, DeviceInfo::class.java)
        if (deviceInfo != null) {
            uniqueId = deviceInfo.deviceId.toString()
        }
        return deviceInfo
    }

    fun getUniqueId(): String? {
        if (uniqueId.isNotEmpty()) {
            return uniqueId
        }
        val deviceInfo = getDeviceInfoFromLocalStorage()
        if (deviceInfo != null) {
            return deviceInfo.deviceId
        }
        return null
    }

    fun clearDeviceInfo() {
        mmkv.remove(KEY_UNIQUE_ID)
    }
}