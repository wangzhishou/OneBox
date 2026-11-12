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

package com.shifenmiao.ai.agent.tool.predicate

import com.shifenmiao.ai.agent.tool.ToolFilterContext
import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import javax.inject.Inject

/**
 * 协议维度谓词：按当前引擎的请求协议筛选工具。
 *
 * 云端协议一律放行 —— 保证云端引擎最终工具集与引入谓词链前完全一致；
 * 端侧（LOCAL_ON_DEVICE）为 fail-closed 语义：只放行 [OnDeviceToolAllowlist]
 * 中列出的工具名，名单外一律不可见 —— 包括目录里查不到元数据
 * （catalogItem 为 null）的情况。理由：本地小模型能力有限，拿到跑不了的
 * 联网/登录类工具定义只会浪费本已紧张的上下文预算，并诱发必然失败的调用；
 * 因此端侧宁可误伤（漏放某个本可执行的本地工具），也不放行任何未经验证的工具。
 */
class ProtocolToolPredicate @Inject constructor() : ToolPredicate {

    override suspend fun isToolVisible(
        toolName: String,
        catalogItem: ToolCatalogItem?,
        context: ToolFilterContext
    ): Boolean {
        if (context.protocol != AiRequestProtocol.LOCAL_ON_DEVICE) return true
        return toolName in OnDeviceToolAllowlist.toolNames
    }
}
