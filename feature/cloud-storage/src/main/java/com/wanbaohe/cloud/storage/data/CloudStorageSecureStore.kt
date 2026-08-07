package com.wanbaohe.cloud.storage.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.model.RemoteProtocol
import com.wanbaohe.cloud.storage.model.S3Vendor
import org.json.JSONArray
import org.json.JSONObject

/**
 * 远程存储连接的加密持久化。
 *
 * 序列化策略：每条连接顶层带 `proto` 字段 (`s3` / `webdav` / `smb`)，其余字段按协议分别持久化。
 * 向后兼容旧 `vendor` 单一枚举数据。
 */
internal class CloudStorageSecureStore(
    context: Context,
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun loadConnections(): List<CloudStorageConnection> {
        val raw = prefs.getString(KEY_CONNECTIONS, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    parseConnection(item)?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveConnections(items: List<CloudStorageConnection>) {
        val array = JSONArray()
        items.forEach { connection ->
            array.put(serializeConnection(connection))
        }
        prefs.edit().putString(KEY_CONNECTIONS, array.toString()).apply()
    }

    private fun serializeConnection(c: CloudStorageConnection): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("displayName", c.displayName)
        put("isDefault", c.isDefault)
        when (c) {
            is CloudStorageConnection.S3Compat -> {
                put("proto", RemoteProtocol.S3_COMPAT.name)
                put("vendor", c.vendor.name)
                put("endpoint", c.endpoint)
                put("region", c.region)
                put("bucket", c.bucket)
                put("accessKeyId", c.accessKeyId)
                put("secretAccessKey", c.secretAccessKey)
            }
            is CloudStorageConnection.WebDav -> {
                put("proto", RemoteProtocol.WEB_DAV.name)
                put("baseUrl", c.baseUrl)
                put("username", c.username)
                put("password", c.password)
                put("rootPath", c.rootPath)
            }
            is CloudStorageConnection.Smb -> {
                put("proto", RemoteProtocol.SMB.name)
                put("host", c.host)
                put("port", c.port)
                put("share", c.share)
                put("domain", c.domain)
                put("username", c.username)
                put("password", c.password)
            }
        }
    }

    private fun parseConnection(item: JSONObject): CloudStorageConnection? {
        val id = item.optString("id")
        val displayName = item.optString("displayName")
        val isDefault = item.optBoolean("isDefault")
        val protoName = item.optString("proto").ifBlank {
            // 旧格式：无 proto 字段，默认按 S3 解析
            RemoteProtocol.S3_COMPAT.name
        }
        val proto = runCatching { RemoteProtocol.valueOf(protoName) }
            .getOrDefault(RemoteProtocol.S3_COMPAT)
        return when (proto) {
            RemoteProtocol.S3_COMPAT -> parseS3(item, id, displayName, isDefault)
            RemoteProtocol.WEB_DAV -> parseWebDav(item, id, displayName, isDefault)
            RemoteProtocol.SMB -> parseSmb(item, id, displayName, isDefault)
        }
    }

    private fun parseS3(
        item: JSONObject,
        id: String,
        displayName: String,
        isDefault: Boolean,
    ): CloudStorageConnection.S3Compat? = runCatching {
        val vendor = runCatching { S3Vendor.valueOf(item.optString("vendor")) }
            .getOrDefault(S3Vendor.S3_COMPATIBLE)
        CloudStorageConnection.S3Compat(
            id = id,
            displayName = displayName,
            vendor = vendor,
            endpoint = item.optString("endpoint"),
            region = item.optString("region"),
            bucket = item.optString("bucket"),
            accessKeyId = item.optString("accessKeyId"),
            secretAccessKey = item.optString("secretAccessKey"),
            isDefault = isDefault,
        )
    }.getOrNull()

    private fun parseWebDav(
        item: JSONObject,
        id: String,
        displayName: String,
        isDefault: Boolean,
    ): CloudStorageConnection.WebDav = CloudStorageConnection.WebDav(
        id = id,
        displayName = displayName,
        baseUrl = item.optString("baseUrl"),
        username = item.optString("username"),
        password = item.optString("password"),
        rootPath = item.optString("rootPath", "/").ifBlank { "/" },
        isDefault = isDefault,
    )

    private fun parseSmb(
        item: JSONObject,
        id: String,
        displayName: String,
        isDefault: Boolean,
    ): CloudStorageConnection.Smb = CloudStorageConnection.Smb(
        id = id,
        displayName = displayName,
        host = item.optString("host"),
        port = item.optInt("port", 445),
        share = item.optString("share"),
        domain = item.optString("domain"),
        username = item.optString("username"),
        password = item.optString("password"),
        isDefault = isDefault,
    )

    private companion object {
        private const val PREFS_NAME = "cloud_storage_secure_store"
        private const val KEY_CONNECTIONS = "connections"
    }
}
