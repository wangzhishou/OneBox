package com.shifenmiao.base.hilt

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.model.BuildConfig
import com.shifenmiao.model.DeviceInfo
import com.shifenmiao.model.Response
import com.shifenmiao.storage.DeviceInfoStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoModule @Inject constructor(@ApplicationContext private val context: Context) {

    private var info: DeviceInfo = DeviceInfo()
    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cleanupJob: Job? = null // To hold reference for cleanup

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (!CoreUtils.isShowPrivacyPolicyDialog()) {
                updateDeviceInfo()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            if (!CoreUtils.isShowPrivacyPolicyDialog()) {
                updateDeviceInfo()
            }
        }
    }

    init {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun updateDeviceInfo() {
        cleanupJob?.cancel() // Cancel previous job if exists
        cleanupJob = moduleScope.launch {
            getDeviceInfoFun()
            val networkType = getNetworkType(context)
            info.deviceNetType = networkType
            // Perform other background tasks here
        }
    }


    fun getQueryParameter(): DeviceInfo {
        if (info.deviceId?.isEmpty() == true) {
            return getDeviceInfoFun()
        }
        return info
    }


    fun getChannel(): String {
        return BuildConfig.FLAVOR
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceInfoFun(): DeviceInfo {
        try {
            val deviceInfo = DeviceInfoStorage.getDeviceInfoFromLocalStorage()
            if (deviceInfo != null && (deviceInfo.deviceId?.isNotEmpty() == true)) {
                info = deviceInfo
                return deviceInfo
            } else {
                val networkType = getNetworkType(context)
                val deviceModel = Build.MODEL
                val deviceBrand = Build.MANUFACTURER
                val deviceName = Build.PRODUCT
                val channel = getChannel()
                val softwareId: String = UUID.randomUUID().toString()
                info = DeviceInfo(
                    deviceModel = deviceModel,
                    deviceBrand = deviceBrand,
                    deviceId = softwareId,
                    deviceName = deviceName,
                    deviceSdkInt = Build.VERSION.SDK_INT,
                    deviceNetType = networkType,
                    channel = channel,
                )
                DeviceInfoStorage.saveDeviceInfoLocalStorage(info)
                return info
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return info
    }

    @SuppressLint("HardwareIds")
    fun getDeviceInfo(): Flow<Response<DeviceInfo>> = flow {
        val response = try {
            getDeviceInfoFun()
            Response.Success(info)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unexpected error occurred")
        }
        emit(response)
    }

    private fun getNetworkType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return "NONE"
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "UNKNOWN"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                    ) -> "CELLULAR_UNMETERED"

                    else -> "CELLULAR"
                }
            }

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "UNKNOWN"
        }
    }

    fun cleanup() {
        moduleScope.cancel() // Cancel the scope on cleanup to prevent memory leaks
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}