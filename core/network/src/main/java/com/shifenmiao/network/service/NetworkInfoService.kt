package com.shifenmiao.network.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络信息服务
 *
 * 获取当前设备的网络连接状态、类型、IP地址等信息
 */
@Singleton
class NetworkInfoService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 网络信息结果
     */
    data class NetworkInfo(
        val isConnected: Boolean,
        val connectionType: ConnectionType,
        val wifiInfo: WifiNetworkInfo?,
        val mobileInfo: MobileNetworkInfo?,
        val ipAddress: String?,
        val publicIpAddress: String? = null,
        val proxyInfo: String? = null
    )

    enum class ConnectionType(val displayName: String) {
        WIFI("WiFi"),
        MOBILE("移动数据"),
        ETHERNET("以太网"),
        VPN("VPN"),
        NONE("无连接"),
        UNKNOWN("未知")
    }

    data class WifiNetworkInfo(
        val ssid: String?,
        val bssid: String?,
        val signalStrength: Int?, // RSSI
        val linkSpeed: Int?, // Mbps
        val frequency: Int?, // MHz
        val ipAddress: String?
    )

    data class MobileNetworkInfo(
        val networkType: String?,
        val signalStrength: Int?,
        val carrierName: String?,
        val isRoaming: Boolean
    )

    /**
     * 获取网络信息
     */
    suspend fun getNetworkInfo(): Result<NetworkInfo> = withContext(Dispatchers.IO) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
            val linkProperties = network?.let { connectivityManager.getLinkProperties(it) }

            val isConnected = capabilities != null
            val connectionType = getConnectionType(capabilities)

            // WiFi 信息
            val wifiInfo = if (connectionType == ConnectionType.WIFI) {
                getWifiInfo()
            } else null

            // 移动网络信息
            val mobileInfo = if (connectionType == ConnectionType.MOBILE) {
                getMobileInfo()
            } else null

            // IP 地址
            val ipAddress = getLocalIpAddress()

            Result.success(
                NetworkInfo(
                    isConnected = isConnected,
                    connectionType = connectionType,
                    wifiInfo = wifiInfo,
                    mobileInfo = mobileInfo,
                    ipAddress = ipAddress
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取连接类型
     */
    private fun getConnectionType(capabilities: NetworkCapabilities?): ConnectionType {
        if (capabilities == null) return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            else -> ConnectionType.UNKNOWN
        }
    }

    /**
     * 获取 WiFi 信息
     */
    @Suppress("DEPRECATION")
    private fun getWifiInfo(): WifiNetworkInfo? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo

            WifiNetworkInfo(
                ssid = wifiInfo.ssid?.removePrefix("\"")?.removeSuffix("\""),
                bssid = wifiInfo.bssid,
                signalStrength = wifiInfo.rssi,
                linkSpeed = wifiInfo.linkSpeed,
                frequency = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wifiInfo.frequency
                } else null,
                ipAddress = formatIpAddress(wifiInfo.ipAddress)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取移动网络信息
     */
    @Suppress("DEPRECATION")
    private fun getMobileInfo(): MobileNetworkInfo? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val networkType = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    when (telephonyManager.dataNetworkType) {
                        TelephonyManager.NETWORK_TYPE_GPRS,
                        TelephonyManager.NETWORK_TYPE_EDGE,
                        TelephonyManager.NETWORK_TYPE_CDMA -> "2G"
                        TelephonyManager.NETWORK_TYPE_UMTS,
                        TelephonyManager.NETWORK_TYPE_EVDO_0,
                        TelephonyManager.NETWORK_TYPE_EVDO_A,
                        TelephonyManager.NETWORK_TYPE_HSDPA,
                        TelephonyManager.NETWORK_TYPE_HSUPA,
                        TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
                        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                        TelephonyManager.NETWORK_TYPE_NR -> "5G"
                        else -> "未知"
                    }
                }
                else -> "未知"
            }

            MobileNetworkInfo(
                networkType = networkType,
                signalStrength = null,
                carrierName = telephonyManager.networkOperatorName,
                isRoaming = telephonyManager.isNetworkRoaming
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取本地 IP 地址
     */
    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
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
     * 格式化 IP 地址
     */
    private fun formatIpAddress(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }
}
