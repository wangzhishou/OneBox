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

package com.shifenmiao.ai.request

import android.content.Context
import com.shifenmiao.base.longtask.LongTaskCenter
import com.shifenmiao.base.longtask.LongTaskKind
import com.shifenmiao.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 生成期间的前台保活。
 *
 * 解决的问题：锁屏或退后台后，国内 ROM 会把进程冻结、Doze 会把非前台应用的网络
 * 请求 defer 掉，SSE 流随之停摆，用户回来只看到"连接中断"。
 * 挂一个 dataSync 前台服务能保住进程优先级和网络策略豁免（它保进程，不保网络本身）。
 *
 * 实现：全部任务状态收敛到 [LongTaskCenter]（引用计数、聚合通知都在那里），
 * 本类只是 AI 流场景的固定入口（固定 id/标题），保留给聊天、双人对话、回合级
 * 持有等所有 AI 流式场景复用。
 *
 * 设计约束：
 * - 引用计数：双人对话等场景会并发多条流，必须等最后一条结束才停服务；
 * - 通知只写"AI 正在回复"，不携带任何问题/回答内容（锁屏通知栏可见 = 隐私泄露）；
 * - 启动失败（例如系统按后台启动限制拒绝）只降级为不保活，绝不影响生成本身。
 */
@Singleton
class AiStreamKeepAlive @Inject constructor(
    @ApplicationContext private val context: Context,
    private val longTaskCenter: LongTaskCenter,
) {

    fun begin() {
        longTaskCenter.begin(
            id = TASK_ID,
            kind = LongTaskKind.AI_STREAM,
            title = context.getString(R.string.ai_stream_foreground_title),
        )
    }

    fun end() {
        longTaskCenter.end(TASK_ID)
    }

    private companion object {
        const val TASK_ID = "ai.stream"
    }
}
