package com.shifenmiao.common.handle.navigation

import android.content.Context
import androidx.core.net.toUri
import com.shifenmiao.common.handle.ItemResourceResolver
import com.shifenmiao.common.handle.ItemScreenAction
import com.shifenmiao.common.handle.ItemScreenResolver
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavigationResolverEntryPoint {
    fun appDatabase(): AppDatabase
    fun dataDraftHelper(): DataDraftHelper
}

object ItemDeeplinkResolver {

    fun canHandle(url: String): Boolean {
        val uri = runCatching { url.toUri() }.getOrNull() ?: return false
        return uri.scheme?.lowercase(Locale.ROOT) in setOf("onebox", "app") &&
            uri.host.equals(UrlConstants.DEEP_LINK_HOST_ITEM, ignoreCase = true)
    }

    fun resolve(url: String, context: Context): Screen? {
        val request = parse(url) ?: return null
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NavigationResolverEntryPoint::class.java
        )
        return runBlocking {
            resolveScreen(
                request = request,
                appDatabase = entryPoint.appDatabase(),
                dataDraftHelper = entryPoint.dataDraftHelper()
            )
        }
    }

    private suspend fun resolveScreen(
        request: ParsedItemDeeplink,
        appDatabase: AppDatabase,
        dataDraftHelper: DataDraftHelper
    ): Screen? {
        val itemDao = appDatabase.itemEntityDao()
        val itemWithCategories = when {
            request.itemId != null -> itemDao.getItemWithCategoriesById(request.itemId).firstOrNull()
            !request.routeKey.isNullOrBlank() -> {
                val normalized = request.routeKey.trim()
                itemDao.getItemsByCategoryIdFlow().firstOrNull().orEmpty().firstOrNull { candidate ->
                    val open = ItemScreenResolver.resolveForOpen(candidate)
                    val edit = runCatching {
                        ItemScreenResolver.resolveForEdit(candidate.item, dataDraftHelper)
                    }.getOrNull()
                    normalized.equals(open.routeKey, ignoreCase = true) ||
                        normalized.equals(open.canonicalName, ignoreCase = true) ||
                        normalized.equals(open.slug, ignoreCase = true) ||
                        normalized.equals(edit?.routeKey, ignoreCase = true) ||
                        normalized.equals(edit?.canonicalName, ignoreCase = true) ||
                        normalized.equals(edit?.slug, ignoreCase = true)
                }
            }
            else -> null
        } ?: return null

        // PROMPT / AGENT 必须预查资源，否则 resolveForOpen 会因为 prompt/agent == null
        // 而返回 "该提示词条目缺少有效的 promptId..." 的错误，导致 deep link 直接失败。
        val resource = ItemResourceResolver.resolve(
            appDatabase = appDatabase,
            itemId = itemWithCategories.item.id,
            listType = itemWithCategories.item.listType,
        )

        return when (request.action) {
            ItemScreenAction.OPEN -> ItemScreenResolver.resolveForOpen(
                itemWithCategories,
                agent = resource.agent,
                prompt = resource.prompt,
            ).screen
            ItemScreenAction.EDIT -> ItemScreenResolver.resolveForEdit(itemWithCategories.item, dataDraftHelper).screen
        }
    }

    fun parse(url: String): ParsedItemDeeplink? {
        if (!canHandle(url)) return null
        val uri = runCatching { url.toUri() }.getOrNull() ?: return null
        val pathSegments = uri.pathSegments
        val queryAction = uri.getQueryParameter(UrlConstants.DEEP_LINK_QUERY_ACTION)
            ?.let(::parseAction)
        val routeKey = uri.getQueryParameter(UrlConstants.DEEP_LINK_QUERY_ROUTE_KEY)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val firstSegment = pathSegments.firstOrNull().orEmpty()
        val secondSegment = pathSegments.getOrNull(1).orEmpty()

        return when {
            firstSegment.equals(ItemScreenAction.OPEN.name, ignoreCase = true) ||
                firstSegment.equals(ItemScreenAction.EDIT.name, ignoreCase = true) -> {
                ParsedItemDeeplink(
                    itemId = secondSegment.toIntOrNull() ?: uri.getQueryParameter(UrlConstants.DEEP_LINK_QUERY_ITEM_ID)?.toIntOrNull(),
                    routeKey = routeKey,
                    action = parseAction(firstSegment) ?: ItemScreenAction.OPEN
                )
            }

            else -> ParsedItemDeeplink(
                itemId = firstSegment.toIntOrNull() ?: uri.getQueryParameter(UrlConstants.DEEP_LINK_QUERY_ITEM_ID)?.toIntOrNull(),
                routeKey = routeKey,
                action = queryAction ?: ItemScreenAction.OPEN
            )
        }
    }

    private fun parseAction(value: String?): ItemScreenAction? {
        return value
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.ROOT)
            ?.let { normalized -> runCatching { ItemScreenAction.valueOf(normalized) }.getOrNull() }
    }

    data class ParsedItemDeeplink(
        val itemId: Int?,
        val routeKey: String?,
        val action: ItemScreenAction
    )
}

