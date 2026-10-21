package com.wanbaohe.devicestatus.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import android.webkit.WebView
import com.wanbaohe.devicestatus.BuildConfig
import com.wanbaohe.devicestatus.domain.AppFacts
import com.wanbaohe.devicestatus.domain.BatteryFacts
import com.wanbaohe.devicestatus.domain.BatteryHealth
import com.wanbaohe.devicestatus.domain.BatteryStatus
import com.wanbaohe.devicestatus.domain.CarrierFacts
import com.wanbaohe.devicestatus.domain.DeviceFacts
import com.wanbaohe.devicestatus.domain.DisplayFacts
import com.wanbaohe.devicestatus.domain.MemoryFacts
import com.wanbaohe.devicestatus.domain.NetworkSlice
import com.wanbaohe.devicestatus.domain.PhoneStatus
import com.wanbaohe.devicestatus.domain.PhoneStatusSource
import com.wanbaohe.devicestatus.domain.PhoneType
import com.wanbaohe.devicestatus.domain.PowerSource
import com.wanbaohe.devicestatus.domain.PrivateDns
import com.wanbaohe.devicestatus.domain.SimState
import com.wanbaohe.devicestatus.domain.StorageFacts
import com.wanbaohe.devicestatus.domain.StorageKind
import com.wanbaohe.devicestatus.domain.SystemFacts
import com.wanbaohe.devicestatus.domain.TransportKind
import com.wanbaohe.devicestatus.domain.WifiFacts
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 读取本机状态。只用安装时就有的普通权限（网络状态、Wi-Fi 状态），
 * 不申请定位、电话、附近设备。系统因此不会给出 Wi-Fi 名称、路由器 MAC、
 * 手机号、IMEI；双卡也只能看到默认的那张卡。
 */
class AndroidPhoneStatusSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhoneStatusSource {

    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun snapshots(): Flow<PhoneStatus> = callbackFlow {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(read())
            }

            override fun onLost(network: Network) {
                trySend(read())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(read())
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                trySend(read())
            }
        }
        if (manager != null) {
            runCatching { manager.registerDefaultNetworkCallback(callback) }
        }
        trySend(read())
        val refreshJob = launch {
            refreshes.collect { trySend(read()) }
        }
        awaitClose {
            refreshJob.cancel()
            if (manager != null) {
                runCatching { manager.unregisterNetworkCallback(callback) }
            }
        }
    }.debounce(200).distinctUntilChanged()

    override fun refresh() {
        refreshes.tryEmit(Unit)
    }

    private fun read(): PhoneStatus {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        return PhoneStatus(
            networks = readNetworks(manager),
            carrier = runCatching { readCarrier() }.getOrNull(),
            device = readDevice(),
            system = readSystem(),
            display = readDisplay(),
            memory = runCatching { readMemory() }.getOrNull(),
            storage = runCatching { readStorage() }.getOrDefault(emptyList()),
            battery = runCatching { readBattery() }.getOrNull(),
            app = runCatching { readApp() }.getOrElse { fallbackApp() },
            dataSaver = manager?.let { dataSaver(it) },
            airplaneMode = airplaneMode(),
            powerSave = powerSave(),
        )
    }

    @Suppress("DEPRECATION") // 需要同时看到 VPN 和底下的局域网，没有替代的一次性查询
    private fun readNetworks(manager: ConnectivityManager?): List<NetworkSlice> {
        if (manager == null) return fallback(emptySet())
        return runCatching {
            val active = manager.activeNetwork
            val ordered = buildList {
                if (active != null) add(active)
                manager.allNetworks.forEach { network ->
                    if (network != active) add(network)
                }
            }
            val slices = ordered.mapNotNull { network ->
                val caps = manager.getNetworkCapabilities(network)
                val link = manager.getLinkProperties(network)
                if (caps == null && link == null) {
                    null
                } else {
                    toSlice(network == active, caps, link)
                }
            }
            val names = slices.mapNotNull { it.interfaceName }.toSet()
            slices + fallback(names)
        }.getOrElse { fallback(emptySet()) }
    }

    private fun toSlice(
        active: Boolean,
        caps: NetworkCapabilities?,
        link: LinkProperties?,
    ): NetworkSlice {
        val (ipv4, ipv6) = splitHosts(link?.linkAddresses?.map { it.address }.orEmpty(), interfaceOnly = true)
        return NetworkSlice(
            transports = if (caps == null) listOf(TransportKind.OTHER) else transports(caps),
            isActive = active,
            validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
            metered = caps?.let { !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) },
            captivePortal = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) == true,
            interfaceName = link?.interfaceName?.takeIf { it.isNotBlank() },
            ipv4 = ipv4,
            ipv6 = ipv6,
            gateways = gateways(link),
            dns = dns(link),
            mtu = link?.mtu?.takeIf { it > 0 },
            privateDns = link?.let { privateDns(it) },
            proxy = link?.let { proxy(it) },
            downlinkKbps = caps?.linkDownstreamBandwidthKbps?.takeIf { it > 0 },
            uplinkKbps = caps?.linkUpstreamBandwidthKbps?.takeIf { it > 0 },
            wifi = caps?.let { wifiFacts(it) },
        )
    }

    private fun transports(caps: NetworkCapabilities): List<TransportKind> {
        val found = mutableListOf<TransportKind>()
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) found += TransportKind.VPN
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) found += TransportKind.WIFI
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) found += TransportKind.CELLULAR
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) found += TransportKind.ETHERNET
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) found += TransportKind.BLUETOOTH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            caps.hasTransport(NetworkCapabilities.TRANSPORT_USB)
        ) {
            found += TransportKind.USB
        }
        if (found.isEmpty()) found += TransportKind.OTHER
        return found
    }

    private fun gateways(link: LinkProperties?): List<String> {
        if (link == null) return emptyList()
        return link.routes.mapNotNull { route ->
            if (!route.isDefaultRoute || !route.hasGateway()) return@mapNotNull null
            route.gateway?.let { textHost(it) }
        }.distinct().sorted()
    }

    private fun dns(link: LinkProperties?): List<String> {
        if (link == null) return emptyList()
        return link.dnsServers.mapNotNull { textHost(it) }.distinct().sorted()
    }

    private fun privateDns(link: LinkProperties): PrivateDns? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        if (!link.isPrivateDnsActive) return PrivateDns.Off
        val host = link.privateDnsServerName
        return if (host.isNullOrBlank()) PrivateDns.Automatic else PrivateDns.Host(host)
    }

    private fun proxy(link: LinkProperties): String? {
        val proxy = link.httpProxy ?: return null
        val host = proxy.host?.takeIf { it.isNotBlank() } ?: return null
        return if (proxy.port > 0) "$host:${proxy.port}" else host
    }

    private fun wifiFacts(caps: NetworkCapabilities): WifiFacts? {
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val info = wifiInfo(caps) ?: return null
        val facts = readWifiInfo(info)
        val empty = facts.rssiDbm == null &&
            facts.linkSpeedMbps == null &&
            facts.rxLinkSpeedMbps == null &&
            facts.txLinkSpeedMbps == null &&
            facts.frequencyMhz == null &&
            facts.standardName == null
        return if (empty) null else facts
    }

    private fun wifiInfo(caps: NetworkCapabilities): WifiInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return caps.transportInfo as? WifiInfo
        }
        return legacyWifiInfo()
    }

    @Suppress("DEPRECATION")
    private fun legacyWifiInfo(): WifiInfo? {
        val manager = context.applicationContext.getSystemService(WifiManager::class.java) ?: return null
        val info = runCatching { manager.connectionInfo }.getOrNull() ?: return null
        if (info.networkId < 0) return null
        return info
    }

    private fun readWifiInfo(info: WifiInfo): WifiFacts {
        val rssi = info.rssi.takeIf { it in (UNKNOWN_RSSI + 1)..-1 }
        val link = info.linkSpeed.takeIf { it > 0 }
        val rx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            info.rxLinkSpeedMbps.takeIf { it > 0 }
        } else {
            null
        }
        val tx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            info.txLinkSpeedMbps.takeIf { it > 0 }
        } else {
            null
        }
        val frequency = info.frequency.takeIf { it > 0 }
        return WifiFacts(
            rssiDbm = rssi,
            linkSpeedMbps = link,
            rxLinkSpeedMbps = rx,
            txLinkSpeedMbps = tx,
            frequencyMhz = frequency,
            standardName = wifiStandard(info),
        )
    }

    private fun wifiStandard(info: WifiInfo): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return when (info.wifiStandard) {
            ScanResult.WIFI_STANDARD_LEGACY -> "802.11a/b/g"
            ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
            ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
            ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
            else -> wifi7(info)
        }
    }

    private fun wifi7(info: WifiInfo): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return if (info.wifiStandard == ScanResult.WIFI_STANDARD_11BE) "Wi-Fi 7" else null
    }

    private fun fallback(skipNames: Set<String>): List<NetworkSlice> {
        val interfaces = runCatching {
            Collections.list(NetworkInterface.getNetworkInterfaces())
        }.getOrDefault(emptyList())
        return interfaces.mapNotNull { nif ->
            if (!nif.isUp || nif.isLoopback) return@mapNotNull null
            val name = nif.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            if (name in skipNames) return@mapNotNull null
            val (ipv4, ipv6) = splitHosts(Collections.list(nif.inetAddresses), interfaceOnly = true)
            if (ipv4.isEmpty() && ipv6.isEmpty()) return@mapNotNull null
            NetworkSlice(
                transports = listOf(TransportKind.OTHER),
                isActive = false,
                validated = null,
                metered = null,
                captivePortal = false,
                interfaceName = name,
                ipv4 = ipv4,
                ipv6 = ipv6,
                gateways = emptyList(),
                dns = emptyList(),
                mtu = nif.mtu.takeIf { it > 0 },
                privateDns = null,
                proxy = null,
                downlinkKbps = null,
                uplinkKbps = null,
                wifi = null,
            )
        }
    }

    private fun readCarrier(): CarrierFacts? {
        val telephony = context.getSystemService(TelephonyManager::class.java) ?: return null
        val simState = runCatching { telephony.simState }.getOrNull()?.let(::mapSim)
        val absent = simState == SimState.ABSENT
        val networkName = if (absent) null else known(runCatching { telephony.networkOperatorName }.getOrNull())
        val simName = if (absent) null else known(runCatching { telephony.simOperatorName }.getOrNull())
        val operatorCode = if (absent) {
            null
        } else {
            known(runCatching { telephony.networkOperator }.getOrNull())
                ?: known(runCatching { telephony.simOperator }.getOrNull())
        }
        val country = if (absent) {
            null
        } else {
            known(runCatching { telephony.networkCountryIso }.getOrNull())
                ?: known(runCatching { telephony.simCountryIso }.getOrNull())
        }?.uppercase(Locale.US)
        val roaming = if (absent) null else runCatching { telephony.isNetworkRoaming }.getOrNull()
        val phoneType = runCatching { telephony.phoneType }.getOrNull()?.let(::mapPhoneType)
        if (simState == null && networkName == null && simName == null &&
            operatorCode == null && country == null && roaming == null && phoneType == null
        ) {
            return null
        }
        return CarrierFacts(
            networkOperatorName = networkName,
            simOperatorName = simName?.takeIf { it != networkName },
            operatorCode = operatorCode,
            countryIso = country,
            simState = simState,
            roaming = roaming,
            phoneType = phoneType,
        )
    }

    private fun readDevice(): DeviceFacts {
        return DeviceFacts(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            brand = Build.BRAND.orEmpty(),
            model = Build.MODEL.orEmpty(),
            device = known(Build.DEVICE),
            product = known(Build.PRODUCT),
            hardware = known(Build.HARDWARE),
            board = known(Build.BOARD),
            soc = soc(),
            abis = Build.SUPPORTED_ABIS.toList(),
            deviceName = deviceName(),
            androidId = androidId(),
            baseband = known(runCatching { Build.getRadioVersion() }.getOrNull()),
            fingerprint = known(Build.FINGERPRINT),
            bootloader = known(Build.BOOTLOADER),
            glEsVersion = glEs(),
        )
    }

    private fun soc(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val maker = known(Build.SOC_MANUFACTURER)
        val model = known(Build.SOC_MODEL)
        return when {
            maker != null && model != null && !model.startsWith(maker, ignoreCase = true) -> "$maker $model"
            model != null -> model
            else -> maker
        }
    }

    private fun deviceName(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return null
        return runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    @SuppressLint("HardwareIds")
    private fun androidId(): String? = runCatching {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun glEs(): String? {
        val manager = context.getSystemService(ActivityManager::class.java) ?: return null
        return manager.deviceConfigurationInfo?.glEsVersion
            ?.takeIf { it.isNotBlank() && it != "0.0" }
    }

    private fun readSystem(): SystemFacts {
        val locale = Locale.getDefault()
        val zone = TimeZone.getDefault()
        val offsetMinutes = zone.getOffset(System.currentTimeMillis()) / 60_000
        return SystemFacts(
            release = Build.VERSION.RELEASE.orEmpty(),
            sdk = Build.VERSION.SDK_INT,
            securityPatch = known(Build.VERSION.SECURITY_PATCH),
            buildDisplay = known(Build.DISPLAY),
            incremental = known(Build.VERSION.INCREMENTAL)?.takeIf { it != Build.DISPLAY },
            kernel = known(System.getProperty("os.version")),
            languageName = locale.getDisplayName(locale),
            languageTag = locale.toLanguageTag(),
            timeZoneId = zone.id,
            timeZoneOffsetMinutes = offsetMinutes,
            uptimeMillis = SystemClock.elapsedRealtime(),
            webViewVersion = webViewVersion(),
            bootCount = bootCount(),
        )
    }

    private fun webViewVersion(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return runCatching { WebView.getCurrentWebViewPackage()?.versionName }.getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private fun bootCount(): Int? = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
    }.getOrNull()?.takeIf { it >= 0 }

    @Suppress("DEPRECATION")
    private fun readDisplay(): DisplayFacts {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(WindowManager::class.java)
        val display = windowManager?.defaultDisplay
        if (display != null) {
            display.getRealMetrics(metrics)
        } else {
            metrics.setTo(context.resources.displayMetrics)
        }
        val refresh = display?.mode?.refreshRate?.takeIf { it > 1f }
        val config = context.resources.configuration
        return DisplayFacts(
            widthPx = metrics.widthPixels,
            heightPx = metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            fontScale = config.fontScale,
            refreshHz = refresh,
            smallestWidthDp = config.smallestScreenWidthDp,
        )
    }

    private fun readMemory(): MemoryFacts? {
        val manager = context.getSystemService(ActivityManager::class.java) ?: return null
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        if (info.totalMem <= 0L) return null
        return MemoryFacts(
            totalBytes = info.totalMem,
            availableBytes = info.availMem,
            lowMemory = info.lowMemory,
        )
    }

    private fun readStorage(): List<StorageFacts> {
        val internalDir = android.os.Environment.getDataDirectory()
        val sharedDir = android.os.Environment.getExternalStorageDirectory()
        val internal = stat(internalDir) ?: return emptyList()
        val result = mutableListOf(
            StorageFacts(StorageKind.INTERNAL, internal.first, internal.second),
        )
        val samePath = runCatching { internalDir.canonicalPath == sharedDir.canonicalPath }
            .getOrDefault(false)
        val shared = stat(sharedDir)
        if (shared != null && !samePath) {
            result += StorageFacts(StorageKind.SHARED, shared.first, shared.second)
        }
        return result
    }

    private fun stat(dir: File): Pair<Long, Long>? {
        if (!dir.exists()) return null
        val stat = runCatching { StatFs(dir.absolutePath) }.getOrNull() ?: return null
        if (stat.totalBytes <= 0L) return null
        return stat.totalBytes to stat.availableBytes
    }

    private fun readBattery(): BatteryFacts? {
        val intent = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull() ?: return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else null
        val manager = context.getSystemService(BatteryManager::class.java)
        val current = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            ?.takeIf { it != Int.MIN_VALUE }
        val chargeMs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && manager != null) {
            manager.computeChargeTimeRemaining().takeIf { it > 0L }
        } else {
            null
        }
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        return BatteryFacts(
            percent = percent,
            status = mapBatteryStatus(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)),
            source = mapPowerSource(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)),
            health = mapBatteryHealth(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)),
            temperatureC = if (tempTenths == Int.MIN_VALUE) null else tempTenths / 10f,
            voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).takeIf { it > 0 },
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() },
            currentMicroAmps = current,
            chargeTimeRemainingMs = chargeMs,
        )
    }

    @Suppress("DEPRECATION")
    private fun readApp(): AppFacts {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }
        return AppFacts(
            versionName = info.versionName?.takeIf { it.isNotBlank() } ?: BuildConfig.VersionName,
            versionCode = code,
            flavor = BuildConfig.FLAVOR,
            buildType = BuildConfig.BUILD_TYPE,
            packageName = context.packageName,
            firstInstallTime = info.firstInstallTime,
            lastUpdateTime = info.lastUpdateTime,
            targetSdk = context.applicationInfo.targetSdkVersion,
        )
    }

    private fun fallbackApp(): AppFacts = AppFacts(
        versionName = BuildConfig.VersionName,
        versionCode = BuildConfig.VersionCode.toLongOrNull() ?: 0L,
        flavor = BuildConfig.FLAVOR,
        buildType = BuildConfig.BUILD_TYPE,
        packageName = context.packageName,
        firstInstallTime = 0L,
        lastUpdateTime = 0L,
        targetSdk = context.applicationInfo.targetSdkVersion,
    )

    private fun dataSaver(manager: ConnectivityManager): Boolean =
        manager.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED

    private fun airplaneMode(): Boolean? = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }.getOrNull()

    private fun powerSave(): Boolean? =
        context.getSystemService(PowerManager::class.java)?.isPowerSaveMode

    private fun splitHosts(
        addresses: List<InetAddress>,
        interfaceOnly: Boolean,
    ): Pair<List<String>, List<String>> {
        val ipv4 = mutableListOf<String>()
        val ipv6 = mutableListOf<String>()
        addresses.forEach { address ->
            val host = if (interfaceOnly) interfaceHost(address) else textHost(address) ?: return@forEach
            if (host == null) return@forEach
            if (address is Inet4Address) ipv4 += host else ipv6 += host
        }
        return ipv4.distinct().sorted() to ipv6.distinct().sorted()
    }

    private fun interfaceHost(address: InetAddress): String? {
        if (address.isLoopbackAddress || address.isLinkLocalAddress) return null
        return textHost(address)
    }

    private fun textHost(address: InetAddress): String? {
        if (address.isAnyLocalAddress || address.isMulticastAddress) return null
        val host = address.hostAddress?.substringBefore('%')?.takeIf { it.isNotBlank() } ?: return null
        if (host == "0.0.0.0" || host == "::") return null
        return host
    }

    private fun known(value: String?): String? =
        value?.trim()?.takeIf { it.isNotEmpty() && !it.equals("unknown", ignoreCase = true) }

    private fun mapSim(value: Int): SimState = when (value) {
        TelephonyManager.SIM_STATE_ABSENT -> SimState.ABSENT
        TelephonyManager.SIM_STATE_READY -> SimState.READY
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> SimState.PIN
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> SimState.PUK
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> SimState.NETWORK_LOCKED
        // 6/7/8/9 从 API 26 才有具名字段，这里用数值，避免旧系统加载字段失败
        6 -> SimState.NOT_READY
        7, 8, 9 -> SimState.RESTRICTED
        else -> SimState.UNKNOWN
    }

    @Suppress("DEPRECATION") // PHONE_TYPE_CDMA 仍是系统会返回的值
    private fun mapPhoneType(value: Int): PhoneType = when (value) {
        TelephonyManager.PHONE_TYPE_GSM -> PhoneType.GSM
        TelephonyManager.PHONE_TYPE_CDMA -> PhoneType.CDMA
        TelephonyManager.PHONE_TYPE_SIP -> PhoneType.SIP
        TelephonyManager.PHONE_TYPE_NONE -> PhoneType.NONE
        else -> PhoneType.UNKNOWN
    }

    private fun mapBatteryStatus(value: Int): BatteryStatus? = when (value) {
        BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
        BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
        BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
        BatteryManager.BATTERY_STATUS_UNKNOWN -> BatteryStatus.UNKNOWN
        else -> null
    }

    private fun mapPowerSource(value: Int): PowerSource? = when (value) {
        BatteryManager.BATTERY_PLUGGED_AC -> PowerSource.AC
        BatteryManager.BATTERY_PLUGGED_USB -> PowerSource.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> PowerSource.WIRELESS
        // BATTERY_PLUGGED_DOCK，API 33 才有具名字段
        8 -> PowerSource.DOCK
        0 -> PowerSource.UNPLUGGED
        else -> null
    }

    private fun mapBatteryHealth(value: Int): BatteryHealth? = when (value) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.FAILURE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
        else -> null
    }

    private companion object {
        const val UNKNOWN_RSSI = -127
    }
}
