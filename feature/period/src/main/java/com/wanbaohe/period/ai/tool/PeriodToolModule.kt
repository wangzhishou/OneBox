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

package com.wanbaohe.period.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
object PeriodToolModule {

    @Provides
    @IntoMap
    @StringKey("add_period_record")
    fun provideAddPeriodRecordTool(tool: AddPeriodRecordTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_period_records")
    fun provideQueryPeriodRecordsTool(tool: QueryPeriodRecordsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("update_period_record")
    fun provideUpdatePeriodRecordTool(tool: UpdatePeriodRecordTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("delete_period_record")
    fun provideDeletePeriodRecordTool(tool: DeletePeriodRecordTool): AgentTool = tool
}
