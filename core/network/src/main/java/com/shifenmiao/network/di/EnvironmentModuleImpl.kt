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

package com.shifenmiao.network.di

import com.shifenmiao.core.common.EnvironmentModule
import com.shifenmiao.network.NetworkBuilder

class EnvironmentModuleImpl : EnvironmentModule {

    override fun getUrl(): String {
        return NetworkBuilder.getBaseUrl()
    }

    override fun setUrl(url: String) {
        NetworkBuilder.setBaseUrl(url)
    }

    override fun isDebug(): Boolean {
        return NetworkBuilder.isDebug()
    }

}