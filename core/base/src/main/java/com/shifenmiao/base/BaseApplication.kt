package com.shifenmiao.base

import android.app.Application
import com.shifenmiao.interfaces.singleton.AppContext

/**
 * Created by zhengxiaobo in 2020-01-05
 */
open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.init(applicationContext)
    }
}