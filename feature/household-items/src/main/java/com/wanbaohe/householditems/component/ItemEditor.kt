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

package com.wanbaohe.householditems.component

import com.wanbaohe.householditems.model.HouseholdItemUi
import com.wanbaohe.householditems.model.HouseholdItemsUiState
import com.wanbaohe.householditems.service.HouseholdService
import java.time.LocalDate
import java.time.ZoneId

/**
 * 物品编辑表单状态机 — 接管名称/分类/位置/保质期/图片/备注的输入。
 *
 * 内部不持有 StateFlow,纯函数式:外部传入旧 state,返回新 state。
 */
internal class ItemEditor {

    fun startAdd(state: HouseholdItemsUiState, defaultLocationId: String?): HouseholdItemsUiState {
        return state.copy(
            showItemEditor = true,
            editingItemId = null,
            editorName = "",
            editorCategory = "",
            editorLocationId = defaultLocationId,
            editorExpireDate = null,
            editorPhotoUri = null,
            editorPhotoPath = null,
            editorNote = "",
        )
    }

    fun startEdit(state: HouseholdItemsUiState, item: HouseholdItemUi): HouseholdItemsUiState {
        return state.copy(
            showItemEditor = true,
            editingItemId = item.id,
            editorName = item.name,
            editorCategory = item.category,
            editorLocationId = item.locationId,
            editorExpireDate = item.expireDate,
            editorPhotoUri = null,
            editorPhotoPath = item.photoPath,
            editorNote = item.note,
        )
    }

    fun hide(state: HouseholdItemsUiState): HouseholdItemsUiState =
        state.copy(showItemEditor = false, editingItemId = null)

    fun changeName(state: HouseholdItemsUiState, value: String): HouseholdItemsUiState =
        state.copy(editorName = value)

    fun changeCategory(state: HouseholdItemsUiState, value: String): HouseholdItemsUiState =
        state.copy(editorCategory = value)

    fun changeLocation(state: HouseholdItemsUiState, locationId: String?): HouseholdItemsUiState =
        state.copy(editorLocationId = locationId)

    fun changeExpireDate(state: HouseholdItemsUiState, date: LocalDate?): HouseholdItemsUiState =
        state.copy(editorExpireDate = date)

    fun changePhoto(state: HouseholdItemsUiState, uri: String?): HouseholdItemsUiState =
        state.copy(editorPhotoUri = uri)

    fun removePhoto(state: HouseholdItemsUiState): HouseholdItemsUiState =
        state.copy(editorPhotoUri = null, editorPhotoPath = null)

    fun changeNote(state: HouseholdItemsUiState, value: String): HouseholdItemsUiState =
        state.copy(editorNote = value)

    /** 名称非空时返回提交所需 input,否则 null。 */
    fun buildInput(state: HouseholdItemsUiState): HouseholdService.ItemInput? {
        val name = state.editorName.trim()
        if (name.isEmpty()) return null
        val editing = state.editingItemId != null
        return HouseholdService.ItemInput(
            name = name,
            category = state.editorCategory.trim().takeIf { it.isNotEmpty() },
            locationId = state.editorLocationId,
            expireAt = state.editorExpireDate
                ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            note = state.editorNote.trim().takeIf { it.isNotEmpty() },
            photoUri = state.editorPhotoUri,
            // 编辑态:没有新图且旧图也被移除时才是真正的删除
            removePhoto = editing && state.editorPhotoUri == null && state.editorPhotoPath == null,
        )
    }
}
