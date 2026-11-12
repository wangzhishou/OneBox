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

package com.shifenmiao.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DataList<T : Parcelable>(
    val data: List<T>,
    val meta: Meta
) : Parcelable

@Parcelize
@Serializable
data class DataObject<T : Parcelable>(
    val data: T,
    val meta: Meta
) : Parcelable

@Parcelize
@Serializable
data class Meta(
    val pagination: Pagination = Pagination(),
    val serverTime: String? = null,
) : Parcelable

/**
 * page: 当前页码，从 1 开始。
 * pageSize: 每页显示的数据条数。
 * pageCount: 总页数。
 * total: 数据的总条数。
 */
@Parcelize
@Serializable
data class Pagination(
    val page: Int = 0,
    val pageSize: Int = 0,
    val pageCount: Int = 0,
    val total: Int = 0
) : Parcelable