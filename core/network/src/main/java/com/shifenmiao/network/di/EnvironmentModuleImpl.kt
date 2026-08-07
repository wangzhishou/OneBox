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