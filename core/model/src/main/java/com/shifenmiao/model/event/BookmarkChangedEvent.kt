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

package com.shifenmiao.model.event

/**
 * 网址收藏条目被写入/删除后广播。
 *
 * 常驻在返回栈里的浏览器（收藏查看器）靠它感知「刚在编辑页改过的这条收藏」，
 * 从而重载快照而不是继续显示旧内容；[itemId] 传 0 表示整库有变更（如删除），
 * 订阅方按需全量刷新。
 */
data class BookmarkChangedEvent(
    val itemId: Int = 0,
    val url: String = "",
    val isDeleted: Boolean = false,
)
