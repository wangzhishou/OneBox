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

package com.shifenmiao.database.ai

/**
 * AI 记忆的容量与注入策略常量（feature/ai 与 feature/settings 共用）。
 */
object MemoryLimits {

    /** log 条目滚动淘汰上限（profile 不参与） */
    const val MAX_LOG_ENTRIES = 500

    /** 关键词搜索返回上限 */
    const val SEARCH_RESULT_LIMIT = 60

    /** 无关键词 dump 返回上限 */
    const val DUMP_RESULT_LIMIT = 500

    /**
     * 搜索 / dump 总输出字节上限（UTF-8，含条目标题与截断提示）。
     * 与工具 maxResultLength(16384) 对齐并留提示余量，避免
     * "仓库给 30KB → Registry 截 16KB → Runner 再截"的三层截断。
     */
    const val MAX_OUTPUT_BYTES = 15 * 1024

    /** 每条结果在预算中计入的标题/结构开销（字节） */
    const val PER_ENTRY_OVERHEAD_BYTES = 32

    /** 注入的 log 日期桶数量（最近 N 个有内容的日期，不是近 N 天） */
    const val PROMPT_LOG_BUCKETS = 3

    /** 记忆 fragment 占 prompt 预算的比例上限 */
    const val PROMPT_BUDGET_FRACTION = 0.15

    /** 记忆 fragment 的 token 预算下限（小窗口模型也保留基本注入能力） */
    const val MIN_PROMPT_BUDGET_TOKENS = 800

    /** 单条记忆内容字符上限（memory_write 工具与管理页手动新增统一） */
    const val MAX_ENTRY_CHARS = 2000
}
