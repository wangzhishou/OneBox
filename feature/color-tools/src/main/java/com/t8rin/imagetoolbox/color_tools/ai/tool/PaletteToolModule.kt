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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.color_tools.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 调色板 AI 工具注册模块。
 *
 * 四个工具一组：列 / 生成 / 保存 / 删除。都通过 @IntoMap + @StringKey
 * 注册进 AgentToolRegistry，不会进入首轮 prompt，等 discover_tools 或
 * 用户显式绑定后再放开。
 */
@Module
@InstallIn(SingletonComponent::class)
object PaletteToolModule {

    @Provides
    @IntoMap
    @StringKey("palette_list")
    fun provideListPalettesTool(tool: ListColorPalettesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("palette_generate")
    fun provideGeneratePaletteTool(tool: GeneratePaletteTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("palette_save")
    fun provideSavePaletteTool(tool: SavePaletteTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("palette_delete")
    fun provideDeletePaletteTool(tool: DeletePaletteTool): AgentTool = tool
}
