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

package com.wanbaohe.passwordvault.list.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.passwordvault.model.PasswordVaultListEvent
import com.wanbaohe.passwordvault.model.PasswordVaultListUiState
import com.wanbaohe.passwordvault.service.PasswordVaultService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PasswordVaultListComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val service: PasswordVaultService,
    private val authCodeStateHolder: AuthorizationCodeStateHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PasswordVaultListUiState())
    val uiState: StateFlow<PasswordVaultListUiState> = _uiState.asStateFlow()

    private val selectedCategoryId = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")

    init {
        componentScope.launch { service.ensureDefaultCategories() }
        observeData()
    }

    fun handleEvent(event: PasswordVaultListEvent) {
        when (event) {
            is PasswordVaultListEvent.OpenEntry -> onNavigateToType(
                Screen.PasswordVault.Type.Detail(event.entryId)
            )
            PasswordVaultListEvent.CreateEntry -> onCreateEntry()
            is PasswordVaultListEvent.SelectCategory -> {
                selectedCategoryId.value = event.categoryId
            }
            is PasswordVaultListEvent.QueryChanged -> {
                query.value = event.query
            }
            PasswordVaultListEvent.ClearFilters -> {
                query.value = ""
                selectedCategoryId.value = null
            }
        }
    }

    private fun observeData() {
        combine(
            service.observeEntrySummaries(),
            service.observeCategories(),
            selectedCategoryId,
            query,
        ) { entries, categories, filter, search ->
            ListFilterResult(entries, categories, filter, search.trim())
        }
            .onEach { result ->
                val filtered = result.entries.asSequence()
                    .filter { entry ->
                        result.filter == null || entry.categoryId == result.filter
                    }
                    .filter { entry ->
                        if (result.search.isEmpty()) {
                            true
                        } else {
                            val lower = result.search.lowercase()
                            entry.title.lowercase().contains(lower) ||
                                entry.account?.lowercase()?.contains(lower) == true
                        }
                    }
                    .toList()
                _uiState.value = _uiState.value.copy(
                    entries = filtered,
                    categories = result.categories,
                    selectedCategoryId = result.filter,
                    query = result.search,
                )
            }
            .launchIn(componentScope)
    }

    private fun onCreateEntry() {
        if (authCodeStateHolder.isAuthorized) {
            onNavigateToType(Screen.PasswordVault.Type.Add)
            return
        }
        ActionUtils.showAuthCode(
            source = "vault_list_create",
            onSuccess = { onNavigateToType(Screen.PasswordVault.Type.Add) },
        )
    }

    private data class ListFilterResult(
        val entries: List<com.wanbaohe.passwordvault.model.VaultEntry>,
        val categories: List<com.wanbaohe.passwordvault.model.PasswordVaultCategoryUi>,
        val filter: String?,
        val search: String,
    )

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigateToType: (Screen.PasswordVault.Type) -> Unit,
        ): PasswordVaultListComponent
    }
}
