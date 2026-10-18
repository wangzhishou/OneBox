package com.shifenmiao.common.handle.navigation

import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 深链打开功能时补记「最近访问」。
 *
 * 卡片点击会写 item_click_stat；onebox:// / app:// 以前只导航，最近列表就缺这一条。
 * 写入在 IO 上，调用本身立即返回，不挡跳转。
 *
 * - 条目深链：用解析时已经拿到的 itemId，不再查表。
 * - 屏幕深链：按屏幕 id 点查一条应用目录项（mini_program_id）。对不上就不写。
 *   详情页 id 对不上时才调用 [entryScreenId]，用入口屏再查一次。
 * 同一条目 1 秒内只写一次，避免同一次跳转被投递两遍时刷两次最近列表。
 */
object DeeplinkRecentRecorder {

    private const val DEDUPE_WINDOW_MS = 1_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val recentWriteAt = ConcurrentHashMap<Int, Long>()

    fun recordItem(itemId: Int) {
        if (itemId <= 0) return
        val now = System.currentTimeMillis()
        if (!claim(itemId, now)) return
        scope.launch {
            runCatching { dao()?.recordClick(itemId, now) }
        }
    }

    /**
     * @param entryScreenId 仅在 [screenId] 对不上目录项时才调用，用来拿入口屏 id。
     * 命中时不会执行，避免在点击线程上多构建一次页面。
     */
    fun recordScreen(screenId: Int, entryScreenId: () -> Int = { 0 }) {
        if (screenId <= 0) return
        scope.launch {
            runCatching {
                val itemDao = dao() ?: return@runCatching
                val itemId = findCatalogItemId(itemDao, screenId)
                    ?: findCatalogItemId(
                        itemDao,
                        runCatching(entryScreenId).getOrDefault(0).takeIf { it != screenId } ?: 0,
                    )
                    ?: return@runCatching
                val now = System.currentTimeMillis()
                if (!claim(itemId, now)) return@runCatching
                itemDao.recordClick(itemId, now)
            }
        }
    }

    private suspend fun findCatalogItemId(itemDao: ItemEntityDao, screenId: Int): Int? {
        if (screenId <= 0) return null
        return itemDao.findItemIdByMiniProgramId(
            listType = ListItemType.NORMAL.id,
            miniProgramId = screenId.toString(),
        )
    }

    private fun claim(itemId: Int, now: Long): Boolean {
        val previous = recentWriteAt.put(itemId, now)
        return previous == null || now - previous >= DEDUPE_WINDOW_MS
    }

    private fun dao(): ItemEntityDao? {
        val context = runCatching { AppContext.getContext() }.getOrNull() ?: return null
        return EntryPointAccessors.fromApplication(
            context,
            NavigationResolverEntryPoint::class.java,
        ).appDatabase().itemEntityDao()
    }
}
