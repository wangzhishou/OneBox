package com.shifenmiao.base.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.shifenmiao.interfaces.singleton.AppContext


object NetWorkUtils {
    /**
     * 检查网络连接是否可用
     *
     * @param context 上下文，必须提供，避免使用全局静态Context
     * @return 网络可用返回true，否则返回false
     */
    fun isNetworkAvailable(context: Context = AppContext.getContext()): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}