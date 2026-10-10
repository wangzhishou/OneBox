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
