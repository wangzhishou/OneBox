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

package com.shifenmiao.model.activity

import java.util.Date

/**
 * 活动日志领域模型 — 从 Entity 映射后供 UI / Component 层使用。
 *
 * @param id            自增主键
 * @param category      活动分类，见 [ActivityCategory]
 * @param title         简短标题（如 "AI 对话"、"裁剪图片"、屏幕名称）
 * @param description   可包含 HTML 的描述文本
 * @param screenRoute   序列化的 Screen ID，用于点击跳转（如 "123"）
 * @param payload       各类型专属 JSON 数据，由写入方决定格式
 * @param thumbnailUri  可选的缩略图 URI（图片编辑时可存结果图路径）
 * @param dedupKey      去重键：相同 dedupKey 只保留最新一条（如 AI 对话的 conversationId）
 * @param createdAt     创建时间
 */
data class ActivityLogEntry(
    val id: Long = 0,
    val category: ActivityCategory = ActivityCategory.OTHER,
    val appTitle: String = "",
    val title: String = "",
    val description: String = "",
    val screenRoute: String = "",
    val payload: String = "",
    val thumbnailUri: String? = null,
    val dedupKey: String = "",
    val createdAt: Date = Date()
)

