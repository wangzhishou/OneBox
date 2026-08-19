package com.wanbaohe.dsh.connection

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wanbaohe.dsh.wire.DshJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private val Context.dshCredentialsStore by preferencesDataStore(name = "dsh_host_book")

/**
 * 凭证存取(P6,对齐 Flutter credentials.dart + host_book.dart):
 * DataStore 持久化主机簿(地址/hostRef/令牌)+ 设备名。**密码永不落盘**。
 *
 * 职责:
 * - [book]:主机簿流(v2 JSON;旧 P1 纯地址列表读入时自动迁移为一元条目);
 * - [adopt]:配对/登录成功 → 复合键 upsert 并激活(同宿主原地刷新令牌;
 *   旧裸网关键条目在网关开始返回 hostRef 后被取代移除);
 * - [remove]:删除条目 + best-effort 吊销网关令牌(POST /auth/revoke {jti},
 *   Bearer 自证;失败不阻断删除 —— 令牌仍有 30 天自然过期 + 管理面手工吊销);
 * - [deviceName]:设备名流(未设置时回落 [DeviceIdentity.defaultName])。
 */
@Singleton
class CredentialsStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("DshOkHttpClient") okHttpClient: OkHttpClient
) {

    /** 吊销专用 client:5s 超时(best-effort,不拖慢删除) */
    private val revokeClient: OkHttpClient = okHttpClient.newBuilder()
        .callTimeout(RevokeTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    /** 吊销/持久化后台 scope(独立于组件生命周期) */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 主机簿流:v2 优先;缺席时把旧 P1 纯地址列表迁移为一元簿(内存态,首次写时落 v2) */
    val book: Flow<HostBookData> = context.dshCredentialsStore.data
        .map { prefs ->
            parseHostBook(prefs[BookKey]) ?: migrateLegacy(prefs[LegacyAddressesKey])
        }
        .distinctUntilChanged()

    /** 设备名流:未设置/泛称时回落默认生成值(不落盘,等用户显式改名) */
    val deviceName: Flow<String> = context.dshCredentialsStore.data
        .map { prefs ->
            val saved = DeviceIdentity.sanitize(prefs[DeviceNameKey].orEmpty())
            if (DeviceIdentity.isGeneric(saved)) DeviceIdentity.defaultName() else saved
        }
        .distinctUntilChanged()

    /**
     * 配对/登录成功 → upsert 条目并激活,返回激活条目。
     * 复合键迁移:同网关存在旧形态条目(id = 裸网关地址)且本次带 hostRef 时,
     * 旧条目被取代移除(旧条目只可能是本机旧形态或陈旧令牌,保留只会重复)。
     */
    suspend fun adopt(success: RemoteLoginSuccess): StoredHost {
        val entry = StoredHost(
            id = hostIdFor(success.baseUri, success.hostRef),
            baseUri = success.baseUri,
            token = success.token,
            hostLabel = success.hostLabel,
            hostRef = success.hostRef
        )
        context.dshCredentialsStore.edit { prefs ->
            val current = parseHostBook(prefs[BookKey]) ?: migrateLegacy(prefs[LegacyAddressesKey])
            val legacyId = hostIdForBase(success.baseUri)
            val migrated = if (entry.id != legacyId) current.remove(legacyId) else current
            persist(prefs, migrated.upsert(entry))
        }
        return entry
    }

    /** LAN 直连就绪登记(纯地址条目,无令牌;同地址原地刷新并激活) */
    suspend fun adoptLan(baseUri: String) {
        val entry = StoredHost(id = hostIdForBase(baseUri), baseUri = baseUri)
        context.dshCredentialsStore.edit { prefs ->
            val current = parseHostBook(prefs[BookKey]) ?: migrateLegacy(prefs[LegacyAddressesKey])
            persist(prefs, current.upsert(entry))
        }
    }

    /**
     * 云端中继就绪登记(P7):baseUri = {backend}/dsh/relay,无令牌
     * (App JWT 每次现取,不落盘);id 带 "cloud" hostRef,与同地址其他形态条目区分。
     * [e2eKey] 为扫码邀请的 E2E 密钥 base64url(旧插件明文形态为 null)。
     */
    suspend fun adoptCloud(baseUri: String, e2eKey: String? = null) {
        val entry = StoredHost(
            id = hostIdFor(baseUri, StoredHost.KindCloud),
            baseUri = baseUri,
            kind = StoredHost.KindCloud,
            e2eKey = e2eKey
        )
        context.dshCredentialsStore.edit { prefs ->
            val current = parseHostBook(prefs[BookKey]) ?: migrateLegacy(prefs[LegacyAddressesKey])
            persist(prefs, current.upsert(entry))
        }
    }

    /** 切换活动主机;id 不命中任何条目时簿不变(防误切) */
    suspend fun switchTo(id: String) {
        context.dshCredentialsStore.edit { prefs ->
            val current = parseHostBook(prefs[BookKey]) ?: HostBookData()
            persist(prefs, current.withActive(id))
        }
    }

    /**
     * 删除主机条目;落盘成功后 best-effort 吊销网关令牌(否则网关侧「已配对
     * 设备」行永久留存)。吊销失败不阻断删除。
     */
    suspend fun remove(id: String) {
        var removed: StoredHost? = null
        context.dshCredentialsStore.edit { prefs ->
            val current = parseHostBook(prefs[BookKey]) ?: migrateLegacy(prefs[LegacyAddressesKey])
            removed = current.hosts.firstOrNull { it.id == id }
            persist(prefs, current.remove(id))
        }
        val token = removed?.token
        val baseUri = removed?.baseUri
        if (!token.isNullOrEmpty() && baseUri != null) {
            scope.launch { revokeToken(baseUri, token) }
        }
    }

    /** 设置设备名(清洗后落盘);空/泛称拒绝(返回 false) */
    suspend fun setDeviceName(raw: String): Boolean {
        val next = DeviceIdentity.sanitize(raw)
        if (DeviceIdentity.isGeneric(next)) return false
        context.dshCredentialsStore.edit { prefs ->
            prefs[DeviceNameKey] = next
        }
        return true
    }

    /**
     * 通知网关吊销令牌(POST {base}/auth/revoke {jti},Bearer 自证)。
     * jti 从本地 JWT 载荷解析(不验签);载荷无 jti(非网关签发)则跳过网络调用。
     */
    private fun revokeToken(baseUri: String, token: String) {
        val jti = jtiFromJwt(token) ?: return
        runCatching {
            val body = """{"jti":"$jti"}""".toRequestBody(JsonMediaType)
            val request = Request.Builder()
                .url("$baseUri/auth/revoke")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()
            // 同步执行(已在 IO scope);响应体只作状态判断,非 200 不重试
            revokeClient.newCall(request).execute().use { it.code }
        }
    }

    /** 落 v2 并清旧键(迁移收口) */
    private fun persist(prefs: MutablePreferences, book: HostBookData) {
        prefs[BookKey] = encodeHostBook(book)
        prefs.remove(LegacyAddressesKey)
    }

    /** 旧 P1 形状(纯地址 JSON 数组)→ 一元/多元簿(无令牌 = LAN 直连条目) */
    private fun migrateLegacy(json: String?): HostBookData {
        if (json.isNullOrEmpty()) return HostBookData()
        val addresses = runCatching {
            DshJson.decodeFromString<List<String>>(json)
        }.getOrDefault(emptyList())
        val hosts = addresses.map { address ->
            val normalized = DshConnectionController.normalizeBaseUri(address)
            StoredHost(id = hostIdForBase(normalized), baseUri = normalized)
        }
        return HostBookData(hosts = hosts, activeId = hosts.firstOrNull()?.id)
    }

    private companion object {
        val BookKey = stringPreferencesKey("dsh_host_book_v2")
        val DeviceNameKey = stringPreferencesKey("dsh_device_name")
        /** 旧 P1 键(纯地址列表);读入即迁移,写入时清除 */
        val LegacyAddressesKey = stringPreferencesKey("dsh_addresses")
        const val RevokeTimeoutSeconds = 5L
        val JsonMediaType = "application/json".toMediaType()
    }
}
