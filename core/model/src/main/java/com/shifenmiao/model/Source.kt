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

package com.shifenmiao.model

/**
 * 统一的数据来源枚举，所有实体的 source 字段共用。
 * 数据库存储 Int 值，通过 SourceTypeConverter 自动转换。
 */
enum class Source(val value: Int) {
    /** 网络远程同步 */
    REMOTE(0),
    /** 用户本地创建（含 AI 创作 / 草稿落地） */
    LOCAL(1),
    /** 系统预置（如 AI 生成提示词） */
    SYSTEM(2),
    /** 创建页面临时预览，不进入正式资源列表 */
    PREVIEW(3);

    companion object {
        fun fromValue(value: Int): Source =
            entries.find { it.value == value } ?: REMOTE
    }
}
