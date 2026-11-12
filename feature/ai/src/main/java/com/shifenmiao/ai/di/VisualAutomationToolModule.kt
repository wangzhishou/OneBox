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

package com.shifenmiao.ai.di

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.builtin.visual.ActOnUiTool
import com.shifenmiao.ai.agent.tool.builtin.visual.AutomateUiTaskTool
import com.shifenmiao.ai.agent.tool.builtin.visual.ScreenshotUiTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * UI 自动化三件套 AgentTool 的 Hilt 注册模块。
 *
 * 工具实现位于 feature/ai/agent/tool/builtin/visual/,业务逻辑委托给
 * feature/visual-automation/.../service/VisualAutomationService。
 *
 * 按 .claude/agents/ai-tool-dev.md 推荐做法:UI 自动化工具归属 AI 内置工具集,
 * 与 Browser*Tool / FetchWebpageTool 等保持同构,避免反向依赖导致的循环。
 */
@Module
@InstallIn(SingletonComponent::class)
object VisualAutomationToolModule {

    @Provides
    @IntoMap
    @StringKey("screenshot_ui")
    fun provideScreenshotUiTool(impl: ScreenshotUiTool): AgentTool = impl

    @Provides
    @IntoMap
    @StringKey("act_on_ui")
    fun provideActOnUiTool(impl: ActOnUiTool): AgentTool = impl

    @Provides
    @IntoMap
    @StringKey("automate_ui_task")
    fun provideAutomateUiTaskTool(impl: AutomateUiTaskTool): AgentTool = impl
}