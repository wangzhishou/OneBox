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

package com.wanbaohe.core.ui.review

import androidx.activity.ComponentActivity

/**
 * 国内渠道无 Google Play 服务,应用内评分不可用,[maybePrompt] 为空实现。
 * 与 src/google 下的实现形成 flavor 隔离。
 */
object InAppReviewPrompt {

    fun maybePrompt(activity: ComponentActivity, trigger: ReviewPromptTrigger) = Unit
}
