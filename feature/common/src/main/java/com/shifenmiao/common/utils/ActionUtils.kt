package com.shifenmiao.common.utils

import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.interfaces.singleton.AppContext
import dagger.hilt.android.EntryPointAccessors

object ActionUtils {

    private val resourceProvider: ResourceProvider

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        resourceProvider = entryPoint.getResourceProvider()
    }
}