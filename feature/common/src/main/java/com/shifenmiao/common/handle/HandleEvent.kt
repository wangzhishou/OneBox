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

package com.shifenmiao.common.handle

import android.content.Context
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.database.item.entity.requiresAuth
import com.wanbaohe.core.ui.utils.navigation.Screen

object HandleEvent {

    /**
     * 卡片点击的统一收口。
     *
     * 授权拦截:
     * - 若 item.requiresAuth = true,先弹全局授权码锁屏,解锁成功才执行导航
     * - 取消或验证失败不导航
     * - 这一层拦截对所有调用方 (FavoriteComponent / Search / Online / MyCreated 等) 都生效,
     *   调用方无需重复实现
     */
    fun handleCardClick(
        context: Context,
        onNavigate: (Screen) -> Unit,
        itemWithRelation: ItemWithCategories,
        resource: ItemResourceRef = ItemResourceRef(),
    ) {
        if (itemWithRelation.requiresAuth) {
            ActionUtils.showAuthCode(
                source = "item_${itemWithRelation.item.id}",
                onSuccess = { navigate(context, onNavigate, itemWithRelation, resource) },
            )
            return
        }
        navigate(context, onNavigate, itemWithRelation, resource)
    }

    fun handleCardClick(
        context: Context,
        onNavigate: (Screen) -> Unit,
        itemWithStats: ItemWithCategoriesAndStats,
        resource: ItemResourceRef = ItemResourceRef(),
    ) = handleCardClick(context, onNavigate, itemWithStats.toItemWithCategories(), resource)

    /**
     * 直接导航到 item 对应页面,跳过授权码验证。
     * 用于已具备密码管理上下文的场景 (如授权码设置页内点击已保护 item)。
     */
    fun handleCardClickDirect(
        context: Context,
        onNavigate: (Screen) -> Unit,
        itemWithRelation: ItemWithCategories,
        resource: ItemResourceRef = ItemResourceRef(),
    ) = navigate(context, onNavigate, itemWithRelation, resource)

    private fun navigate(
        context: Context,
        onNavigate: (Screen) -> Unit,
        itemWithRelation: ItemWithCategories,
        resource: ItemResourceRef,
    ) {
        val resolution = ItemScreenResolver.resolveForOpen(
            itemWithRelation,
            agent = resource.agent,
            prompt = resource.prompt,
        )
        val screen = resolution.screen
        if (screen != null) {
            onNavigate(screen)
            return
        }
        ActionUtils.showToast(
            resolution.message ?: context.getString(R.string.need_update_new_version)
        )
    }

    fun onNavigateMessageEntity(
        messageEntity: MessageEntity,
        onNavigate: (Screen) -> Unit
    ) {
        onNavigate(
            AIConversationNavigation.buildHistoryDetailScreen(
                conversationId = messageEntity.conversationId,
                title = messageEntity.title,
                entryType = messageEntity.entryType,
                entryRefId = messageEntity.entryRefId,
            )
        )
    }
}
