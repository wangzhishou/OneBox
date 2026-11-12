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

package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DeviceInfo(
    val deviceName: String? = null,
    val deviceBrand: String? = null,
    val deviceModel: String? = null,
    val deviceId: String? = null,
    val deviceSdkInt: Int? = null,
    var deviceNetType: String? = null,
    var channel: String? = null
) : Parcelable

fun DeviceInfo.toQueryString(): String {
    val parts = listOfNotNull(
        "clientInfo=android_${BuildConfig.VersionName}_${BuildConfig.BUILD_TYPE}_${BuildConfig.FLAVOR}",
        "clientVersion=${BuildConfig.VersionCode}",
        "os=android",
        "osVersion=${deviceSdkInt}",
        "netType=${deviceNetType}",
        "customInfo=${deviceBrand}_${deviceModel}_${deviceName}_$deviceId",
        "channel=${channel}",
    )
    return parts.joinToString("&")
}