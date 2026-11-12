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

package com.shifenmiao.search.logic

import com.shifenmiao.common.search.GlobalSearchProvider
import com.shifenmiao.common.search.GlobalSearchResultPage
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class GlobalSearchCoordinator @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards GlobalSearchProvider>,
) {
    fun search(query: String, limit: Int): Flow<GlobalSearchUpdate> = channelFlow {
        providers.forEach { provider ->
            launch {
                val page = try {
                    provider.search(query, limit)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    GlobalSearchResultPage(emptyList())
                }
                send(GlobalSearchUpdate(provider.sourceId, page))
            }
        }
    }
}

data class GlobalSearchUpdate(
    val sourceId: String,
    val page: GlobalSearchResultPage,
)
