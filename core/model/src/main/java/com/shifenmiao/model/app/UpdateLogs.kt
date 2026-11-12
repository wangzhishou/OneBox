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

package com.shifenmiao.model.app

import android.os.Parcelable
import com.shifenmiao.model.common.Meta
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * {
 *     "data": [
 *         {
 *             "id": 1,
 *             "versionName": "V1.0.1",
 *             "versionCode": 101,
 *             "changeLog": "nuxt: Flag async data promise as cancelled only if defined (#27690)\n",
 *             "channel": 1,
 *             "createdAt": "2024-07-08T06:10:11.718Z",
 *             "updatedAt": "2024-07-08T06:13:30.784Z",
 *             "publishedAt": "2024-07-08T06:13:30.740Z"
 *         }
 *     ],
 *     "meta": {
 *         "pagination": {
 *             "page": 1,
 *             "pageSize": 25,
 *             "pageCount": 1,
 *             "total": 1
 *         }
 *     }
 * }
 */

@Parcelize
@Serializable
data class UpdateLogs(
    val data: List<UpdateLog>,
    val meta: Meta
) : Parcelable


@Parcelize
@Serializable
data class UpdateLog(
    val id: Int,
    val versionName: String = "",
    val versionCode: Int = 100,
    val changeLog: String = "",
    val channel: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val publishedAt: String = "",
    val downloadUrl: String? = null
) : Parcelable