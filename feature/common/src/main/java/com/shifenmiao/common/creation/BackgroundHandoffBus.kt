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
