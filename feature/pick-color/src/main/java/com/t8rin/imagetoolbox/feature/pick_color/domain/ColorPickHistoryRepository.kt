/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pick_color.domain

import com.shifenmiao.model.color.ColorPickHistoryEntry

/**
 * 取色历史的读写契约。
 *
 * 变更类操作统一返回变更后的完整列表，调用方（Component）据此刷新状态，
 * 不再自己拼一份“预期结果”，避免存储与 UI 两份状态漂移。
 */
interface ColorPickHistoryRepository {
    fun load(): List<ColorPickHistoryEntry>

    fun record(argb: Int): List<ColorPickHistoryEntry>

    fun setFavorite(
        argb: Int,
        isFavorite: Boolean
    ): List<ColorPickHistoryEntry>

    fun remove(argb: Int): List<ColorPickHistoryEntry>

    fun clear()
}
