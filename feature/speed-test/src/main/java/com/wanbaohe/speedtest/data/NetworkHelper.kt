package com.wanbaohe.speedtest.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.wanbaohe.speedtest.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 网络辅助工具：检测当前网络类型，提供 Wi-Fi 设置入口 */
@Singleton
class NetworkHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** 返回当前网络类型的可读描述，如 "5G移动网络"、"WiFi" */
    fun getCurrentNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return context.getString(R.string.speed_test_network_unknown)
        val network = cm.activeNetwork
            ?: return context.getString(R.string.speed_test_network_none)
        val caps = cm.getNetworkCapabilities(network)
            ?: return context.getString(R.string.speed_test_network_unknown)
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    caps.linkDownstreamBandwidthKbps >= 20_000 ->
                        context.getString(R.string.speed_test_network_mobile_5g)

                    caps.linkDownstreamBandwidthKbps >= 5_000 ->
                        context.getString(R.string.speed_test_network_mobile_4g)

                    else -> context.getString(R.string.speed_test_network_mobile)
                }
            }

            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                context.getString(R.string.speed_test_network_ethernet)

            else -> context.getString(R.string.speed_test_network_unknown)
        }
    }

    /** 打开 Wi-Fi 设置 */
    fun openWifiSettings() {
        context.startActivity(
            Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

