package com.wanbaohe.app.update

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.remote.AppUpdateConfig
import com.shifenmiao.network.update.OpenSourceRelease
import com.shifenmiao.network.update.OpenSourceReleaseChecker
import com.shifenmiao.network.update.OpenSourceReleaseEntryPoint
import com.shifenmiao.storage.RemoteConfigStorage
import dagger.hilt.android.EntryPointAccessors

/**
 * 开源版更新提醒的公共入口。
 *
 * 提醒有三个层次，从弱到强：
 * 1. "我的"tab 上的小红点（[rememberUpdateAvailable]）——默认就有，任何页面都能看见；
 * 2. "我的 → 关于 → 开源项目"那一行的彩色副标题——滚动到就看见；
 * 3. 启动弹窗（[AppUpdateConfig.dialogEnabled]）——远程配置打开才弹，用来催特别老的版本。
 *
 * 检查本身由 [OpenSourceReleaseChecker] 完成（6 小时内存缓存、失败静默），
 * 这里只负责把版本号、远程配置和"稍后"记录拼成判定输入。
 */

/** 本机 versionName；取不到时返回空串（版本比较对空串安全，任何 tag 都会被视为更新）。 */
fun currentVersionName(context: Context): String = runCatching {
    @Suppress("DEPRECATION")
    context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
}.getOrDefault("")

/** 本机 longVersionCode；取不到时返回 0。 */
fun currentVersionCode(context: Context): Long = runCatching {
    @Suppress("DEPRECATION")
    context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
}.getOrDefault(0L)

/** 按渠道取开源仓库地址：海外（google / foss）走 GitHub，国内渠道走 GitCode（国内直连不上 GitHub）。 */
fun defaultRepoUrl(): String =
    if (FlavorType.fromName().isOverseas) UrlConstants.GITHUB_REPO else UrlConstants.GITCODE_REPO

/** 更新页地址：优先 release 自带页面地址，其次远程配置覆盖，最后按渠道回退到开源仓库。 */
fun resolveUpdateUrl(release: OpenSourceRelease?, config: AppUpdateConfig?): String =
    release?.url?.takeIf { it.isNotBlank() }
        ?: config?.updateUrl?.takeIf { it.isNotBlank() }
        ?: defaultRepoUrl()

/** 用系统浏览器打开更新页；失败静默（例如国内没有可用浏览器）。 */
fun openUpdatePage(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

/** 取单例检查器（Hilt EntryPoint，与设置页用的是同一个实例，共享 6 小时缓存）。 */
fun openSourceReleaseChecker(context: Context): OpenSourceReleaseChecker =
    EntryPointAccessors.fromApplication(
        context = context.applicationContext,
        entryPoint = OpenSourceReleaseEntryPoint::class.java,
    ).openSourceReleaseChecker()

/**
 * 弹窗判定（纯函数，便于推理）：
 * - 确实有新版本；
 * - 远程配置 [AppUpdateConfig.dialogEnabled] 为 true；
 * - 配了 [AppUpdateConfig.dialogForVersionBelow] 时，本机 versionCode 必须小于它（只催老版本）；
 * - 同一个 tag 在冷却期内点过"稍后"就不再打扰；出了更新的 tag 会重新弹。
 */
fun shouldShowUpdateDialog(
    isNewer: Boolean,
    tag: String,
    localVersionCode: Long,
    config: AppUpdateConfig?,
    dismissedTag: String,
    dismissedAtMs: Long,
    nowMs: Long,
): Boolean {
    if (!isNewer || tag.isBlank()) return false
    if (config?.dialogEnabled != true) return false
    val threshold = config.dialogForVersionBelow
    if (threshold != null && localVersionCode >= threshold) return false
    if (dismissedTag != tag) return true
    val cooldownHours = config.dialogCooldownHours ?: AppUpdateConfig.DEFAULT_DIALOG_COOLDOWN_HOURS
    if (cooldownHours <= 0) return true
    return nowMs - dismissedAtMs >= cooldownHours * 60L * 60L * 1000L
}

/** 是否有新版本可用（供底部导航角标 / 侧边栏角标订阅）。 */
@Composable
fun rememberUpdateAvailable(): Boolean {
    val context = LocalContext.current
    val checker = remember(context) { openSourceReleaseChecker(context) }
    val release by checker.latestRelease.collectAsState()
    return release?.isNewer == true
}

/** 当前生效的更新提醒配置；远程配置下发后自动刷新。 */
@Composable
fun rememberAppUpdateConfig(): AppUpdateConfig? {
    val configVersion = remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        RemoteConfigStorage.rulesChanged.collect { configVersion.intValue++ }
    }
    return remember(configVersion.intValue) { RemoteConfigStorage.getRemoteConfig().appUpdate }
}
