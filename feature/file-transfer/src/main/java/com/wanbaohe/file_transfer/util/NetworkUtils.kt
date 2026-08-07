package com.wanbaohe.file_transfer.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/**
 * 网络工具类
 */
object NetworkUtils {

    /**
     * 获取设备IP地址
     * 优先返回WiFi IP地址
     */
    fun getLocalIpAddress(context: Context): String? {
        // 首先尝试通过WifiManager获取
        val wifiIp = getWifiIpAddress(context)
        if (wifiIp != null && wifiIp != "0.0.0.0") {
            return wifiIp
        }

        // 如果WiFi IP获取失败，遍历网络接口
        return getIpFromNetworkInterfaces()
    }

    /**
     * 通过 ConnectivityManager 获取 WiFi IP 地址
     */
    private fun getWifiIpAddress(context: Context): String? {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return null
            val network = connectivityManager.activeNetwork ?: return null
            val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
            linkProperties.linkAddresses
                .map { it.address }
                .filterIsInstance<Inet4Address>()
                .firstOrNull()
                ?.hostAddress
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 遍历网络接口获取IP地址
     */
    private fun getIpFromNetworkInterfaces(): String? {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * 检查是否连接到WiFi
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * 获取网络类型描述
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return "Unknown"

        val network = connectivityManager.activeNetwork ?: return "No Network"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }

    /**
     * 构建访问URL
     */
    fun buildAccessUrl(ipAddress: String, port: Int): String {
        return "http://$ipAddress:$port"
    }
}

