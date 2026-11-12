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

package com.shifenmiao.model.ai

/**
 * 流式问答页（Screen.AIStreamAnswer）的回答缓存策略。
 *
 * 缓存以「问题 + systemPrompt + 引擎/模型」为内容键，复用 message 表中已持久化的回答：
 * 命中时直接展示历史回答，不再发起网络请求。
 */
enum class StreamAnswerCachePolicy {
    /** 不缓存，每次进入都重新请求（默认，保持现状） */
    NONE,

    /** 同内容永久命中；问题/引擎/模型/prompt 任一变化即自动失效 */
    PERMANENT,

    /** 仅当天内同内容命中，跨天重新生成（适合"每日"类场景） */
    TODAY,
}
