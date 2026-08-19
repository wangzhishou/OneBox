package com.wanbaohe.dsh.connection

import android.util.Base64
import com.wanbaohe.dsh.wire.DshJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * 主机簿条目(P6 升级,对齐 Flutter credentials.dart):
 * 一条 = 一台已配对宿主。多宿主网关下同一网关地址可有多条 —— 各带不同
 * [hostRef](宿主稳定标识:rust 网关 = 隧道端口字符串;CF Worker = 隧道主机名)。
 *
 * @property id 条目 id:网关地址归一化([hostIdForBase])+ '#' + 宿主标识复合
 *   ([hostIdFor]);同 id 配对 = 原地刷新令牌,同网关不同宿主 = 各自独立条目
 * @property baseUri 主机/网关地址(已归一化,含 scheme、无尾斜杠)
 * @property token 远程网关的设备令牌(30 天 JWT);LAN 直连条目恒为 null(纯地址);
 *   云端中继条目也恒为 null —— App JWT 每次现取,不落盘
 * @property hostLabel 配对来源机器名快照(展示用;密码登录/旧凭证为空)
 * @property hostRef 来源宿主稳定标识;非空时参与条目 id 复合
 * @property kind 形态标记:空 = 旧语义(按 token 区分 LAN/远程网关);
 *   [KindCloud] = 云端中继(P7,经 App 自有后端隧道,鉴权走 App JWT)
 * @property e2eKey 云端条目的 E2E 密钥 base64url(仅 cloud 形态有值;
 *   扫码邀请的 k 参数,解码 32 字节作 AES-256-GCM 密钥;旧插件明文形态为 null)
 */
@Serializable
data class StoredHost(
    val id: String,
    val baseUri: String,
    val token: String? = null,
    val hostLabel: String = "",
    val hostRef: String = "",
    val kind: String = "",
    val e2eKey: String? = null
) {
    /** 远程网关形态(持令牌)= true;LAN 直连(纯地址)= false */
    val isRemote: Boolean get() = token != null

    /** 云端中继形态(App 自有后端隧道 + App JWT)= true */
    val isCloud: Boolean get() = kind == KindCloud

    companion object {
        /** 云端中继形态标记(见 [kind]) */
        const val KindCloud = "cloud"
    }
}

/**
 * 网关地址归一化(条目去重键):scheme/host 小写,默认端口显式化,
 * path/query 不参与(网关永远部署在根路径)。
 * https://a.com ≡ https://a.com:443;http://a.com:80 ≡ http://a.com。
 * 无法解析时退回原文 trim(防损:坏地址不崩,只不产生有意义的去重键)。
 */
fun hostIdForBase(baseUri: String): String {
    val url = baseUri.toHttpUrlOrNull() ?: return baseUri.trim().lowercase()
    return "${url.scheme}://${url.host.lowercase()}:${url.port}"
}

/**
 * 条目 id:同网关多宿主 = 地址归一化 + '#' + 宿主标识复合。
 * [hostRef] 为空(旧网关/密码登录/LAN 直连)= 裸网关地址(旧语义,同网关原地刷新)。
 */
fun hostIdFor(baseUri: String, hostRef: String): String =
    if (hostRef.isEmpty()) hostIdForBase(baseUri) else "${hostIdForBase(baseUri)}#$hostRef"

/**
 * 主机簿:全部已配对主机 + 活动指针。不可变,变更经 [upsert]/[remove]/[withActive] 拷贝。
 * 单活动语义:同一时刻只连一台;切换由组件层整代重装实现,簿本身只维护数据一致性。
 */
@Serializable
data class HostBookData(
    val hosts: List<StoredHost> = emptyList(),
    val activeId: String? = null
) {
    /** 活动条目。指针缺失/失效时回落首条(迁移与防损语义) */
    val active: StoredHost?
        get() {
            val id = activeId
            if (id != null) hosts.firstOrNull { it.id == id }?.let { return it }
            return hosts.firstOrNull()
        }

    /** upsert:同 id 原地替换(保位),新 id 追加尾部;默认激活 */
    fun upsert(host: StoredHost, activate: Boolean = true): HostBookData {
        val next = mutableListOf<StoredHost>()
        var replaced = false
        for (h in hosts) {
            if (h.id == host.id) {
                next.add(host)
                replaced = true
            } else {
                next.add(h)
            }
        }
        if (!replaced) next.add(host)
        return HostBookData(
            hosts = next,
            activeId = if (activate || activeId == host.id) host.id else activeId
        )
    }

    /** 删除条目;若删的是活动条目,指针滑到剩余首条 */
    fun remove(id: String): HostBookData {
        val next = hosts.filter { it.id != id }
        return HostBookData(
            hosts = next,
            activeId = if (activeId == id) next.firstOrNull()?.id else activeId
        )
    }

    /** 指定活动条目;id 不命中任何条目时原样返回(防误切) */
    fun withActive(id: String): HostBookData =
        if (hosts.any { it.id == id }) copy(activeId = id) else this
}

/** 主机簿 JSON 编码(v2 形状,对齐 Flutter encodeHostBookJson) */
fun encodeHostBook(book: HostBookData): String = DshJson.encodeToString(book)

/** 主机簿 JSON 解析:null = 不可解析(损坏,调用方按空簿处理) */
fun parseHostBook(raw: String?): HostBookData? {
    if (raw.isNullOrEmpty()) return null
    return runCatching { DshJson.decodeFromString<HostBookData>(raw) }.getOrNull()
}

/**
 * 从 JWT 载荷段解析 jti(纯解码,不验签 —— 网关验;非三段式/解析失败返回 null)。
 * 吊销入口用:POST /auth/revoke {jti}。
 */
fun jtiFromJwt(token: String): String? {
    val parts = token.split('.')
    if (parts.size != 3) return null
    var payload = parts[1]
    // base64url 去填充编码,解码前补齐
    when (payload.length % 4) {
        2 -> payload += "=="
        3 -> payload += "="
    }
    return runCatching {
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE), Charsets.UTF_8)
        val element = DshJson.parseToJsonElement(decoded)
        val jti = (element as? JsonObject)?.get("jti") as? JsonPrimitive
        jti?.contentOrNull?.takeIf { it.isNotEmpty() }
    }.getOrNull()
}
