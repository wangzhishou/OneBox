package com.shifenmiao.model

sealed class Response<out T> {
    data class Error(val error: String) : Response<Nothing>()
    data class Success<out T>(val data: T) : Response<T>()
}