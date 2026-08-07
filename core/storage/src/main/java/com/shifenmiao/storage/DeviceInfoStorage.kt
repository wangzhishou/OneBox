package com.shifenmiao.storage

import com.shifenmiao.model.DeviceInfo
import com.tencent.mmkv.MMKV

object DeviceInfoStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.DEVICE)
    private var uniqueId: String = ""

    private const val KEY_UNIQUE_ID = "key_unique_id"

    fun saveDeviceInfoLocalStorage(deviceInfo: DeviceInfo) {
        mmkv.encode(KEY_UNIQUE_ID, deviceInfo)
    }

    fun getDeviceInfoFromLocalStorage(): DeviceInfo? {
        val deviceInfo = mmkv.decodeParcelable(KEY_UNIQUE_ID, DeviceInfo::class.java)
        if (deviceInfo != null) {
            uniqueId = deviceInfo.deviceId.toString()
        }
        return deviceInfo
    }

    fun getUniqueId(): String? {
        if (uniqueId.isNotEmpty()) {
            return uniqueId
        }
        val deviceInfo = getDeviceInfoFromLocalStorage()
        if (deviceInfo != null) {
            return deviceInfo.deviceId
        }
        return null
    }

    fun clearDeviceInfo() {
        mmkv.remove(KEY_UNIQUE_ID)
    }
}