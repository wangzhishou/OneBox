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

package com.wanbaohe.decisionwheel.di

import android.content.Context
import com.shifenmiao.database.decision_wheel.dao.WheelDao
import com.shifenmiao.model.decisionwheel.WheelServiceInterface
import com.wanbaohe.decisionwheel.data.DecisionWheelPresetsProvider
import com.wanbaohe.decisionwheel.data.WheelRepository
import com.wanbaohe.decisionwheel.service.WheelServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DecisionWheelModule {

    @Provides
    @Singleton
    fun provideWheelRepository(wheelDao: WheelDao): WheelRepository {
        return WheelRepository(wheelDao)
    }

    @Provides
    @Singleton
    fun provideWheelService(impl: WheelServiceImpl): WheelServiceInterface = impl

    @Provides
    @Singleton
    fun provideDecisionWheelPresetsProvider(
        @ApplicationContext context: Context
    ): DecisionWheelPresetsProvider {
        return DecisionWheelPresetsProvider(context)
    }
}
