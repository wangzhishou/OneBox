/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.web

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

class PersistentWebContentOwner : ViewModelStoreOwner, InstanceKeeper.Instance {

    private var store = ViewModelStore()
    private var cachedSession: PersistentWebContentViewModel? = null

    override val viewModelStore: ViewModelStore
        get() = store

    val session: PersistentWebContentViewModel
        get() = cachedSession ?: ViewModelProvider(this)[PersistentWebContentViewModel::class.java]
            .also { cachedSession = it }

    @MainThread
    fun reset() {
        store.clear()
        cachedSession = null
        store = ViewModelStore()
    }

    override fun onDestroy() {
        store.clear()
        cachedSession = null
    }
}
