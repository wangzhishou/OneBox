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

package com.shifenmiao.model.item


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 路由跳转 / 临时预览用的条目参数（与 DB 实体解耦）。
 * - DB 侧的真实数据走 ItemWithRelation 加载
 * - 这里只携带"打开该屏所需的最小字段"
 */
@Parcelize
@Serializable
data class ItemEntityParams(
    val id: Int? = null,
    val title: String = "",
    val description: String = "",
    val url: String? = null,
    val data: String? = null,
    val listType: Int = 0
) : Parcelable
