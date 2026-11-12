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

package com.shifenmiao.model.color

import kotlinx.serialization.Serializable

/**
 * 一条图片取色记录。
 *
 * [argb] 是颜色本身（ARGB int），也是同色去重的唯一键：
 * 同一颜色再次被取到只更新时间与排序，不重复占位。
 * [isFavorite] 标记用户在取色器里点过爱心，历史页可以只看收藏。
 */
@Serializable
data class ColorPickHistoryEntry(
    val argb: Int,
    val pickedAtMillis: Long,
    val isFavorite: Boolean = false
)
