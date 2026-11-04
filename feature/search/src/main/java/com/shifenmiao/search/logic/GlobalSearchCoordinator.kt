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
