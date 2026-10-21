package com.wanbaohe.devicestatus.domain

enum class TransportKind {
    VPN,
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    USB,
    OTHER,
}

sealed interface PrivateDns {
    data object Off : PrivateDns
    data object Automatic : PrivateDns
    data class Host(val name: String) : PrivateDns
}

enum class SimState {
    ABSENT,
    READY,
    PIN,
    PUK,
    NETWORK_LOCKED,
    NOT_READY,
    RESTRICTED,
    UNKNOWN,
}

enum class PhoneType {
    GSM,
    CDMA,
    SIP,
    NONE,
    UNKNOWN,
}

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN,
}

enum class PowerSource {
    AC,
    USB,
    WIRELESS,
    DOCK,
    UNPLUGGED,
    UNKNOWN,
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    FAILURE,
    COLD,
    UNKNOWN,
}

enum class StorageKind {
    INTERNAL,
    SHARED,
}

data class WifiFacts(
    val rssiDbm: Int?,
    val linkSpeedMbps: Int?,
    val rxLinkSpeedMbps: Int?,
    val txLinkSpeedMbps: Int?,
    val frequencyMhz: Int?,
    val standardName: String?,
)

data class NetworkSlice(
    val transports: List<TransportKind>,
    val isActive: Boolean,
    val validated: Boolean?,
    val metered: Boolean?,
    val captivePortal: Boolean,
    val interfaceName: String?,
    val ipv4: List<String>,
    val ipv6: List<String>,
    val gateways: List<String>,
    val dns: List<String>,
    val mtu: Int?,
    val privateDns: PrivateDns?,
    val proxy: String?,
    val downlinkKbps: Int?,
    val uplinkKbps: Int?,
    val wifi: WifiFacts?,
)

data class CarrierFacts(
    val networkOperatorName: String?,
    val simOperatorName: String?,
    val operatorCode: String?,
    val countryIso: String?,
    val simState: SimState?,
    val roaming: Boolean?,
    val phoneType: PhoneType?,
)

data class DeviceFacts(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String?,
    val product: String?,
    val hardware: String?,
    val board: String?,
    val soc: String?,
    val abis: List<String>,
    val deviceName: String?,
    val androidId: String?,
    val baseband: String?,
    val fingerprint: String?,
    val bootloader: String?,
    val glEsVersion: String?,
)

data class SystemFacts(
    val release: String,
    val sdk: Int,
    val securityPatch: String?,
    val buildDisplay: String?,
    val incremental: String?,
    val kernel: String?,
    val languageName: String,
    val languageTag: String,
    val timeZoneId: String,
    val timeZoneOffsetMinutes: Int,
    val uptimeMillis: Long,
    val webViewVersion: String?,
    val bootCount: Int?,
)

data class DisplayFacts(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val density: Float,
    val fontScale: Float,
    val refreshHz: Float?,
    val smallestWidthDp: Int,
)

data class MemoryFacts(
    val totalBytes: Long,
    val availableBytes: Long,
    val lowMemory: Boolean,
)

data class StorageFacts(
    val kind: StorageKind,
    val totalBytes: Long,
    val availableBytes: Long,
)

data class BatteryFacts(
    val percent: Int?,
    val status: BatteryStatus?,
    val source: PowerSource?,
    val health: BatteryHealth?,
    val temperatureC: Float?,
    val voltageMv: Int?,
    val technology: String?,
    val currentMicroAmps: Int?,
    val chargeTimeRemainingMs: Long?,
)

data class AppFacts(
    val versionName: String,
    val versionCode: Long,
    val flavor: String,
    val buildType: String,
    val packageName: String,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val targetSdk: Int,
)

data class PhoneStatus(
    val networks: List<NetworkSlice>,
    val carrier: CarrierFacts?,
    val device: DeviceFacts,
    val system: SystemFacts,
    val display: DisplayFacts,
    val memory: MemoryFacts?,
    val storage: List<StorageFacts>,
    val battery: BatteryFacts?,
    val app: AppFacts,
    val dataSaver: Boolean?,
    val airplaneMode: Boolean?,
    val powerSave: Boolean?,
)
