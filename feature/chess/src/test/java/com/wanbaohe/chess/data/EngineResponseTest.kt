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

package com.wanbaohe.chess.data

import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.Request
import okio.Timeout
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineResponseTest {
    @Test
    fun cancellationCancelsTheActualHttpCallWithoutWaitingForALateResponse() = runBlocking {
        val call = ControlledCall()
        val waiting = async { call.awaitEngineResponse() }
        yield()
        waiting.cancelAndJoin()
        assertTrue(call.isCanceled())
        call.callback.onResponse(call, Response.success("late"))
        assertTrue(waiting.isCancelled)
    }

    @Test
    fun aSuccessfulResponseStillReachesTheEngineChooser() = runBlocking {
        val call = ControlledCall()
        val waiting = async { call.awaitEngineResponse() }
        yield()
        call.callback.onResponse(call, Response.success("e2e4"))
        assertEquals("e2e4", waiting.await().body())
    }

    private class ControlledCall : Call<String> {
        lateinit var callback: Callback<String>
        private var cancelled = false
        private var executed = false
        override fun enqueue(callback: Callback<String>) { executed = true; this.callback = callback }
        override fun cancel() { cancelled = true }
        override fun isCanceled(): Boolean = cancelled
        override fun isExecuted(): Boolean = executed
        override fun clone(): Call<String> = ControlledCall()
        override fun execute(): Response<String> = error("Only the cancellable asynchronous path may be used")
        override fun request(): Request = Request.Builder().url("https://example.invalid/").build()
        override fun timeout(): Timeout = Timeout()
    }
}
