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

package com.shifenmiao.base.manager

import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.database.ClearDatabaseHelper
import com.shifenmiao.interfaces.singleton.AppContext
import dagger.hilt.android.EntryPointAccessors

class DataBaseManager private constructor() {

    private var clearDatabaseHelper: ClearDatabaseHelper

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        clearDatabaseHelper = entryPoint.getClearDatabaseHelper()
    }

    fun clearAll() {
        clearDatabaseHelper.clearAll()
    }

    companion object {
        val instance: DataBaseManager by lazy { DataBaseManager() }
    }
}