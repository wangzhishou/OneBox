package com.wanbaohe.dsh.connection

import android.os.Build

/**
 * 本机设备名(P6,对齐 Flutter device_identity.dart):
 * 配对/登录时上报给网关的 `device` 字段,宿主「已配对设备」表展示。
 *
 * Android 的 localHostname 通常只剩 'localhost'(系统不暴露设备名),零权限
 * 方案落到 Build.MODEL → 'Android-<model>';清洗:去控制符/折叠空白/≤32 码点;
 * 空或泛称(localhost/127.0.0.1/::1)视为无效。持久化在 [CredentialsStore]。
 */
object DeviceIdentity {

    /** 设备名清洗:去控制符、折叠空白、剥域名尾巴、≤32 码点 */
    fun sanitize(raw: String): String {
        val cleaned = raw
            .replace(Regex("[\\u0000-\\u001f\\u007f]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replaceFirst(Regex("\\.(local|lan|home)$"), "")
        return cleaned.take(32)
    }

    /** 泛称判定:空 / localhost / 回环地址 */
    fun isGeneric(name: String): Boolean =
        name.isEmpty() || name == "localhost" || name == "127.0.0.1" || name == "::1"

    /** 默认设备名:Build.MODEL(无需任何权限)→ 'Android-<model>';空 → 'Android' */
    fun defaultName(): String {
        val model = sanitize(Build.MODEL.orEmpty())
        return if (model.isEmpty() || isGeneric(model)) "Android" else "Android-$model"
    }
}
