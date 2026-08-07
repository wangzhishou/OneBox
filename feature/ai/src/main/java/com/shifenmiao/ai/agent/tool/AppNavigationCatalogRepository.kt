package com.shifenmiao.ai.agent.tool

import com.shifenmiao.common.handle.ItemScreenAction
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationResolvedTarget
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.ItemScreenResolver
import com.shifenmiao.common.handle.navigation.ItemDeeplinkResolver
import com.shifenmiao.model.ListItemType
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNavigationCatalogRepository @Inject constructor(
    private val itemScreenCatalogRepository: ItemScreenCatalogRepository
) {

    suspend fun searchTargets(
        query: String,
        keywords: List<String> = emptyList(),
        listItemType: ListItemType? = null,
        limit: Int = 8
    ): List<NavigationCatalogEntry> {
        val normalizedLimit = limit.coerceIn(1, 20)
        val searchTerms = buildSearchTerms(query = query, keywords = keywords)
        val perTermLimit = (normalizedLimit * 2).coerceIn(normalizedLimit, 20)

        val itemEntries = mutableListOf<NavigationCatalogEntry>()
        searchTerms.forEach { term ->
            itemScreenCatalogRepository.searchItems(
                query = term,
                listItemType = listItemType,
                limit = perTermLimit
            ).forEach { item ->
                itemEntries += toItemEntry(item)
            }
        }

        val staticEntries = mutableListOf<NavigationCatalogEntry>()
        if (listItemType == null) {
            searchTerms.forEach { term ->
                AppNavigationRegistry.search(query = term, limit = perTermLimit)
                    .forEach { target ->
                        staticEntries += toStaticEntry(target)
                    }
            }
        }

        return (itemEntries + staticEntries)
            .dedupe()
            .sortedWith(
                compareByDescending<NavigationCatalogEntry> { score(searchTerms, it) }
                    .thenBy { it.title }
            )
            .take(normalizedLimit)
    }

    suspend fun resolveTarget(
        action: ItemScreenAction,
        deeplink: String
    ): NavigationTargetResolution? {
        val normalizedDeeplink = deeplink.trim()
        if (normalizedDeeplink.isBlank()) return null

        ItemDeeplinkResolver.parse(normalizedDeeplink)?.let { deeplinkRequest ->
            resolveItemTarget(
                action = action,
                itemId = deeplinkRequest.itemId,
                stableKey = deeplinkRequest.routeKey
            )?.let { return it }
        }
        if (action == ItemScreenAction.OPEN) {
            resolveStaticOrAction(normalizedDeeplink)?.let { return it }
        }
        return null
    }

    private suspend fun resolveItemTarget(
        action: ItemScreenAction,
        itemId: Int? = null,
        stableKey: String? = null
    ): NavigationTargetResolution? {
        itemId?.let { id ->
            val item = itemScreenCatalogRepository.getItemById(id) ?: return null
            return buildItemTargetResolution(item = item, action = action)
        }
        val normalizedStableKey = stableKey?.trim().orEmpty()
        if (normalizedStableKey.isBlank()) return null
        val item = itemScreenCatalogRepository.findItemByStableKey(normalizedStableKey) ?: return null
        return buildItemTargetResolution(item = item, action = action)
    }

    private suspend fun buildItemTargetResolution(
        item: com.shifenmiao.database.item.entity.ItemWithCategories,
        action: ItemScreenAction
    ): NavigationTargetResolution? {
        val resolution = itemScreenCatalogRepository.resolve(item, action)
        val screen = resolution.screen ?: return null
        return NavigationTargetResolution(
            targetType = NavigationCatalogTargetType.ITEM,
            itemId = item.item.id,
            title = item.item.title,
            description = item.item.description,
            listType = ListItemType.fromId(item.item.listType)?.name,
            routeKey = resolution.routeKey.orEmpty(),
            canonicalName = resolution.canonicalName.orEmpty(),
            deeplink = ItemScreenResolver.buildDeeplink(item.item, action),
            screen = screen,
            supportsResultCallback = itemScreenCatalogRepository.supportsResultCallback(
                item.item,
                action
            )
        )
    }

    private fun resolveStaticOrAction(identifier: String): NavigationTargetResolution? {
        val resolved = AppNavigationRegistry.resolveDeeplink(identifier)
            ?: AppNavigationRegistry.resolve(identifier)?.let(::AppNavigationResolvedTarget)
            ?: AppNavigationRegistry.resolveByTitle(identifier)?.let(::AppNavigationResolvedTarget)
            ?: return null

        return NavigationTargetResolution(
            targetType = when (resolved.targetType) {
                AppNavigationTargetType.SCREEN -> NavigationCatalogTargetType.SCREEN
                AppNavigationTargetType.ACTION -> NavigationCatalogTargetType.ACTION
            },
            itemId = null,
            title = resolved.title,
            description = resolved.description,
            listType = resolved.targetType.name,
            routeKey = resolved.routeKey,
            canonicalName = resolved.canonicalName,
            deeplink = resolved.deeplink,
            screen = resolved.buildScreen(),
            supportsResultCallback = resolved.supportsResultCallback,
            callbackScreenBuilder = resolved.target.callbackScreenBuilder?.let { builder ->
                { onResult -> builder(resolved.params, onResult) }
            }
        )
    }

    private fun score(searchTerms: List<String>, entry: NavigationCatalogEntry): Int {
        val normalizedTerms = searchTerms.map(String::trim).filter { it.isNotBlank() }
        if (normalizedTerms.isEmpty()) {
            return when (entry.targetType) {
                NavigationCatalogTargetType.ACTION -> 80
                NavigationCatalogTargetType.SCREEN -> 70
                NavigationCatalogTargetType.ITEM -> 60
            }
        }
        val bestScore = normalizedTerms.maxOf { term -> scoreSingle(term, entry) }
        val matchedTermCount = normalizedTerms.count { term -> scoreSingle(term, entry) > 0 }
        return bestScore + matchedTermCount * 24
    }

    private fun scoreSingle(query: String, entry: NavigationCatalogEntry): Int {
        val normalized = query.trim()
        return buildList {
            add(if (entry.title.equals(normalized, ignoreCase = true)) 420 else 0)
            add(if (entry.routeKey.equals(normalized, ignoreCase = true)) 400 else 0)
            add(if (entry.canonicalName.equals(normalized, ignoreCase = true)) 390 else 0)
            add(if (entry.title.contains(normalized, ignoreCase = true)) 280 else 0)
            add(if (entry.description.contains(normalized, ignoreCase = true)) 160 else 0)
            add(if (entry.routeKey.contains(normalized, ignoreCase = true)) 240 else 0)
            add(if (entry.canonicalName.contains(normalized, ignoreCase = true)) 220 else 0)
            add(if (entry.deeplink.contains(normalized, ignoreCase = true)) 180 else 0)
        }.maxOrNull() ?: 0
    }

    private fun buildSearchTerms(query: String, keywords: List<String>): List<String> {
        val terms = buildList {
            addAll(extractSearchTerms(query))
            keywords.forEach { keyword -> addAll(extractSearchTerms(keyword)) }
        }.distinct()
        return terms.ifEmpty { listOf("") }
    }

    private fun extractSearchTerms(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        val normalized = text.trim()
        val parts = normalized.split(Regex("[\\s,，。；;、/|]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return (listOf(normalized) + parts).distinct()
    }

    private fun List<NavigationCatalogEntry>.dedupe(): List<NavigationCatalogEntry> {
        return distinctBy { entry ->
            entry.deeplink.ifBlank {
                entry.routeKey.ifBlank {
                    entry.canonicalName.ifBlank { "${entry.targetType}:${entry.itemId}:${entry.title}" }
                }
            }
        }
    }

    private suspend fun toItemEntry(item: com.shifenmiao.database.item.entity.ItemWithCategories): NavigationCatalogEntry {
        val resolution = itemScreenCatalogRepository.resolve(item, ItemScreenAction.OPEN)
        val type = ListItemType.fromId(item.item.listType)
        // NOTE / HTML / PROMPT 走"我的"创作流可编辑；其他远端条目默认不可编辑
        val canEdit = when (type) {
            ListItemType.NOTE, ListItemType.HTML, ListItemType.PROMPT -> true
            else -> false
        }
        return NavigationCatalogEntry(
            targetType = NavigationCatalogTargetType.ITEM,
            itemId = item.item.id,
            title = item.item.title,
            description = item.item.description,
            listType = type?.name ?: "UNKNOWN",
            routeKey = resolution.routeKey.orEmpty(),
            canonicalName = resolution.canonicalName.orEmpty(),
            deeplink = ItemScreenResolver.buildDeeplink(item.item),
            canEdit = canEdit,
            supportsOpenResultCallback = itemScreenCatalogRepository.supportsResultCallback(
                item.item,
                ItemScreenAction.OPEN
            ),
            supportsEditResultCallback = canEdit,
            supportedActions = buildList {
                if (resolution.isSupported) add(ItemScreenAction.OPEN)
                if (canEdit) add(ItemScreenAction.EDIT)
            }
        )
    }

    private fun toStaticEntry(target: com.shifenmiao.common.handle.navigation.AppNavigationTarget): NavigationCatalogEntry {
        return NavigationCatalogEntry(
            targetType = when (target.targetType) {
                AppNavigationTargetType.SCREEN -> NavigationCatalogTargetType.SCREEN
                AppNavigationTargetType.ACTION -> NavigationCatalogTargetType.ACTION
            },
            itemId = null,
            title = target.title,
            description = target.description,
            listType = target.targetType.name,
            routeKey = target.routeKey,
            canonicalName = target.canonicalName,
            deeplink = target.deeplink,
            canEdit = false,
            supportsOpenResultCallback = target.supportsResultCallback,
            supportsEditResultCallback = false,
            supportedActions = listOf(ItemScreenAction.OPEN)
        )
    }
}

enum class NavigationCatalogTargetType {
    ITEM,
    SCREEN,
    ACTION
}

data class NavigationCatalogEntry(
    val targetType: NavigationCatalogTargetType,
    val itemId: Int?,
    val title: String,
    val description: String,
    val listType: String,
    val routeKey: String,
    val canonicalName: String,
    val deeplink: String,
    val canEdit: Boolean,
    val supportsOpenResultCallback: Boolean,
    val supportsEditResultCallback: Boolean,
    val supportedActions: List<ItemScreenAction>
)

data class NavigationTargetResolution(
    val targetType: NavigationCatalogTargetType,
    val itemId: Int?,
    val title: String,
    val description: String,
    val listType: String?,
    val routeKey: String,
    val canonicalName: String,
    val deeplink: String,
    val screen: Screen,
    val supportsResultCallback: Boolean,
    val callbackScreenBuilder: (((ScreenCallback) -> Screen))? = null
)

