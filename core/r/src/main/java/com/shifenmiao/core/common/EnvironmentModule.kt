package com.shifenmiao.core.common

interface EnvironmentModule {
    fun getUrl(): String
    fun setUrl(url: String)
    fun isDebug(): Boolean
}