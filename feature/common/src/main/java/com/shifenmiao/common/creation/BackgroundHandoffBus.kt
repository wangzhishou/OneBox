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

package com.shifenmiao.common.creation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class BackgroundHandoffBus @Inject constructor() {

    data class Request(
        val id: String,
        val width: Int,
        val height: Int,
    )

    data class Result(
        val id: String,
        val uri: String,
    )

    private val lock = Any()
    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request.asStateFlow()
    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result.asStateFlow()

    fun begin(request: Request) {
        require(request.id.isNotBlank()) { "Handoff id must not be blank" }
        require(request.width > 0 && request.height > 0) { "Handoff dimensions must be positive" }
        synchronized(lock) {
            _result.value = null
            _request.value = request
        }
    }

    fun complete(id: String, uri: String): Boolean {
        if (id.isBlank() || uri.isBlank()) return false
        return synchronized(lock) {
            if (_request.value?.id != id) {
                false
            } else {
                _result.value = Result(id = id, uri = uri)
                true
            }
        }
    }

    fun consumeResult(id: String): String? = synchronized(lock) {
        val result = _result.value
        if (_request.value?.id != id || result?.id != id) {
            null
        } else {
            _result.value = null
            _request.value = null
            result.uri
        }
    }

    fun cancel(id: String) {
        synchronized(lock) {
            if (_request.value?.id != id) return
            if (_result.value?.id == id) _result.value = null
            _request.value = null
        }
    }
}
