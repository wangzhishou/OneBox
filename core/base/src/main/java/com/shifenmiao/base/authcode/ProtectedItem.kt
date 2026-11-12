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

package com.shifenmiao.base.authcode

/**
 * 需要密码保护的应用的轻量级 UI 模型。
 *
 * 与 Room 的 [com.shifenmiao.database.item.entity.ItemEntity] 解耦,UI 层不直接持有
 * 数据库实体,符合"高层只依赖抽象"的设计约定。
 */
data class ProtectedItem(
    val id: Int,
    val title: String,
    val iconName: String?,
)
