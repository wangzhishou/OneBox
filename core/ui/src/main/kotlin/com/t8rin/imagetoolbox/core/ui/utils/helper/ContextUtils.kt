/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.core.app.ActivityCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.documentfile.provider.DocumentFile
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.model.event.RequestPermissionEvent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.PerformanceClass
import com.t8rin.imagetoolbox.core.domain.utils.FileMode
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.image_vector.toImageBitmap
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.permission.PermissionStatus
import com.t8rin.imagetoolbox.core.ui.utils.permission.PermissionUtils.askUserToRequestPermissionExplicitly
import com.t8rin.imagetoolbox.core.ui.utils.permission.PermissionUtils.checkPermissions
import com.t8rin.imagetoolbox.core.ui.utils.permission.PermissionUtils.hasPermissionAllowed
import com.t8rin.imagetoolbox.core.ui.utils.permission.PermissionUtils.setPermissionsAllowed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.shifenmiao.model.event.AppEventBus
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import com.t8rin.imagetoolbox.core.utils.filename
import com.t8rin.logger.makeLog
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.math.ceil
import kotlin.random.Random


object ContextUtils {

    private var currentActivityRef: WeakReference<Activity>? = null

    fun registerCurrentActivity(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    fun clearCurrentActivity(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    fun currentActivity(): Activity? = currentActivityRef?.get()

    fun Activity.requestStoragePermission() = requestPermissions(
        permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ),
        permissionRequest = PermissionRequest.STORAGE
    )


    fun requestStoragePermissionAndExecute(context: Context, block: () -> Unit) {
        if (context is Activity) {
            context.requestStoragePermission(success = {
                block()
            })
        }
    }

    fun requestPermissionAndExecute(
        permissions: Array<String> = mutableListOf<String>().toTypedArray(),
        context: Context,
        permissionRequest: PermissionRequest = PermissionRequest.ALL,
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        if (context is Activity) {
            context.requestPermissions(
                permissions,
                permissionRequest = permissionRequest,
                onSuccess = {
                    onGranted()
                },
                onFailed = {
                    onDenied()
                }
            )
        }
    }

    fun requestPermissionAndExecute(
        permissions: Array<String> = mutableListOf<String>().toTypedArray(),
        permissionRequest: PermissionRequest = PermissionRequest.ALL,
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        val activity = currentActivity()
        if (activity == null) {
            onDenied()
            return
        }
        activity.requestPermissions(
            permissions = permissions,
            permissionRequest = permissionRequest,
            onSuccess = onGranted,
            onFailed = onDenied
        )
    }

    fun Activity.requestPermissions(permissions: List<String>) {
        requestPermissions(
            permissions = permissions.toTypedArray(),
            permissionRequest = PermissionRequest.ALL
        )
    }

    fun Activity.requestPermissions(
        permissions: Array<String> = mutableListOf<String>().toTypedArray(),
        permissionRequest: PermissionRequest = PermissionRequest.ALL,
        onSuccess: () -> Unit = {},
        onFailed: () -> Unit = {}
    ) {
        val state = checkPermissions(permissions.toList())
        val requestPermissionEvent = RequestPermissionEvent(
            permissions = permissions.toList(),
            permissionRequest = permissionRequest,
            onSuccess = onSuccess,
            onFailed = onFailed,
            onRequest = {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    permissionRequest.code
                )
            }
        )
        when (state.permissionStatus.values.first()) {
            PermissionStatus.NOT_GIVEN -> {
                AppEventBus.emit(requestPermissionEvent)
            }

            PermissionStatus.DENIED_PERMANENTLY -> {
                if (PermissionRequest.APK_INSTALL.code == permissionRequest.code) {
                    AppEventBus.emit(requestPermissionEvent)
                } else {
                    askUserToRequestPermissionExplicitly()
                    Toast.makeText(this, permissionRequest.description, Toast.LENGTH_LONG).show()
                    onFailed.invoke()
                }
            }

            PermissionStatus.ALLOWED -> {
                onSuccess.invoke()
            }
        }
    }

    fun Activity.requestStoragePermission(success: () -> Unit = {}, onFailed: () -> Unit = {}) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        requestPermissions(
            permissions = permissions.toTypedArray(),
            permissionRequest = PermissionRequest.STORAGE,
            onSuccess = success,
            onFailed = onFailed
        )
    }

    fun Context.startActivity(
        clazz: Class<*>,
        intentBuilder: Intent.() -> Unit,
    ) {
        startActivity(buildIntent(clazz, intentBuilder))
    }

    fun Context.buildIntent(
        clazz: Class<*>,
        intentBuilder: Intent.() -> Unit,
    ): Intent = Intent(applicationContext, clazz).apply(intentBuilder)

    fun Context.postToast(
        textRes: Int,
        vararg formatArgs: Any,
    ) {
        mainLooperAction {
            Toast.makeText(
                applicationContext,
                getString(
                    textRes,
                    *formatArgs
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Context.postToast(
        textRes: Int,
        isLong: Boolean = false,
        vararg formatArgs: Any,
    ) {
        mainLooperAction {
            Toast.makeText(
                applicationContext,
                getString(
                    textRes,
                    *formatArgs
                ),
                if (isLong) {
                    Toast.LENGTH_LONG
                } else Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Context.needToShowStoragePermissionRequest(): Boolean {
        val permissions = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val show = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) false
        else !permissions.all { (this as Activity).hasPermissionAllowed(it) }

        if (!show) setPermissionsAllowed(permissions)

        return show
    }

    fun Context.adjustFontSize(
        scale: Float?,
    ): Context {
        val configuration = resources.configuration
        configuration.fontScale = resolveSafeFontScale(scale)
        return createConfigurationContext(configuration)
    }

    fun Context.resolveSafeFontScale(scale: Float?): Float {
        val baseScale = resources.configuration.fontScale.takeIf { it.isFinite() && it > 0f } ?: 1f
        return scale?.takeIf { it.isFinite() && it > 0f } ?: baseScale
    }

    fun Context.createFontScaleOverrideConfiguration(scale: Float): Configuration {
        val baseConfiguration = Configuration(resources.configuration)
        baseConfiguration.fontScale = resolveSafeFontScale(scale)

        // Preserve the full locale list from the copied configuration instead of
        // calling setLocale() which replaces the multi-locale list with a single
        // entry. On Android 15+ (API 35), ConfigurationController
        // .updateLocaleListFromAppContext compares the activity locale list with
        // the application context locale list and throws NPE when it encounters
        // a null or inconsistent locale caused by the replacement.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = baseConfiguration.locales
            if (locales.isEmpty) {
                // Locale list not yet populated during early attachBaseContext;
                // set a safe default to avoid framework NPE.
                val defaultLocale = Locale.getDefault()
                baseConfiguration.setLocales(android.os.LocaleList(defaultLocale))
                baseConfiguration.setLayoutDirection(defaultLocale)
            } else {
                // Locale list already present from the configuration copy;
                // just ensure layout direction is consistent.
                baseConfiguration.setLayoutDirection(locales[0])
            }
        } else {
            @Suppress("DEPRECATION")
            val locale = baseConfiguration.locale ?: Locale.getDefault()
            baseConfiguration.setLocale(locale)
            baseConfiguration.setLayoutDirection(locale)
        }

        return baseConfiguration
    }

    fun Context.isInstalledFromPlayStore(): Boolean = verifyInstallerId(
        listOf(
            "com.android.vending",
            "com.google.android.feedback"
        )
    )

    private fun Context.verifyInstallerId(
        validInstallers: List<String>,
    ): Boolean = validInstallers.contains(getInstallerPackageName(packageName))

    private fun Context.getInstallerPackageName(packageName: String): String? {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                return packageManager.getInstallSourceInfo(packageName).installingPackageName
            @Suppress("DEPRECATION")
            return packageManager.getInstallerPackageName(packageName)
        }
        return null
    }

    fun Context.getFilename(
        uri: Uri
    ): String? = if (uri.toString().startsWith("file:///")) {
        uri.toString().takeLastWhile { it != '/' }
    } else {
        DocumentFile.fromSingleUri(this, uri)?.name
    }?.decodeEscaped()

    @Composable
    fun rememberFilename(uri: Uri): String? {
        val context = LocalContext.current

        return remember(context, uri) {
            derivedStateOf {
                context.getFilename(uri)
            }
        }.value
    }

    @Composable
    fun rememberFileExtension(uri: Uri): String? {
        val context = LocalContext.current

        return remember(context, uri) {
            derivedStateOf {
                context.getExtension(uri)
            }
        }.value
    }


    val Context.performanceClass: PerformanceClass
        get() {
            val androidVersion = Build.VERSION.SDK_INT
            val cpuCount = Runtime.getRuntime().availableProcessors()
            val memoryClass =
                (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
            var totalCpuFreq = 0
            var freqResolved = 0
            for (i in 0 until cpuCount) {
                runCatching {
                    val reader = RandomAccessFile(
                        String.format(
                            Locale.ENGLISH,
                            "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq",
                            i
                        ), FileMode.Read.mode
                    )
                    val line = reader.readLine()
                    if (line != null) {
                        totalCpuFreq += line.toInt() / 1000
                        freqResolved++
                    }
                    reader.close()
                }
            }
            val maxCpuFreq =
                if (freqResolved == 0) -1 else ceil((totalCpuFreq / freqResolved.toFloat()).toDouble())
                    .toInt()

            return if (androidVersion < 21 || cpuCount <= 2 || memoryClass <= 100 || cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 || cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 || cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24) {
                PerformanceClass.Low
            } else if (cpuCount < 8 || memoryClass <= 160 || maxCpuFreq != -1 && maxCpuFreq <= 2050 || maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23) {
                PerformanceClass.Average
            } else {
                PerformanceClass.High
            }
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun Context.getStringLocalized(
        @StringRes
        resId: Int,
        locale: Locale,
    ): String = createConfigurationContext(
        Configuration(resources.configuration).apply { setLocale(locale) }
    ).getText(resId).toString()

    fun Context.pasteColorFromClipboard(
        onPastedColor: (Color) -> Unit,
        onPastedColorFailure: (String) -> Unit,
    ) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        val text = item?.text?.toString()
        text?.let {
            runCatching {
                onPastedColor(Color(it.toColorInt()))
            }.getOrElse {
                onPastedColorFailure(getString(R.string.clipboard_paste_invalid_color_code))
            }
        } ?: run {
            onPastedColorFailure(getString(R.string.clipboard_paste_invalid_empty))
        }
    }

    fun isMiUi(): Boolean {
        return !getSystemProperty("ro.miui.ui.version.name").isNullOrBlank()
    }

    fun isRedMagic(): Boolean {
        val osName = runCatching {
            System.getProperty("os.name")
        }.getOrNull() ?: getSystemProperty("os.name")
        return listOf("redmagic", "magic", "red").all {
            osName?.contains(it, true) == true
        }
    }

    private fun getSystemProperty(name: String): String? {
        return runCatching {
            val p = Runtime.getRuntime().exec("getprop $name")
            BufferedReader(InputStreamReader(p.inputStream), 1024).use {
                return@runCatching it.readLine()
            }
        }.getOrNull()
    }

    fun Context.getLanguages(): Map<String, String> {
        val supportedLocales = LocaleConfigCompat(this@getLanguages).supportedLocales
        val supportedLanguages = buildList {
            if (supportedLocales != null) {
                for (i in 0 until supportedLocales.size()) {
                    val locale = supportedLocales.get(i) ?: continue
                    add(
                        locale.toLanguageTag() to locale.getDisplayName(locale)
                            .replaceFirstChar(Char::uppercase)
                    )
                }
            }
        }

        val languages = mutableListOf("" to getString(R.string.system)).apply {
            addAll(supportedLanguages)
        }

        return languages.let { tags ->
            listOf(tags.first()) + tags.drop(1).sortedBy { it.second }
        }.toMap()
    }

    /**
     * 判断 APK 是否打包了多于一种语言（除系统默认外还有可选语言）。
     * 比 [getLanguages] 更轻量，仅读取 locale_config.xml 并计数,不构建显示名映射或排序。
     */
    fun Context.supportsMultipleLocales(): Boolean {
        val supportedLocales = LocaleConfigCompat(this@supportsMultipleLocales).supportedLocales
        return supportedLocales != null && supportedLocales.size() > 1
    }

    fun Context.getCurrentLocaleString(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales == LocaleListCompat.getEmptyLocaleList()) {
            return getString(R.string.system)
        }
        return locales.getDisplayName()
    }

    fun LocaleListCompat.getDisplayName(): String = getDisplayName(toLanguageTags())

    fun getDisplayName(
        lang: String?,
        useDefaultLocale: Boolean = false
    ): String {
        if (lang == null) {
            return ""
        }

        val locale = when (lang) {
            "" -> LocaleListCompat.getAdjustedDefault().get(0) ?: Locale.getDefault()
            else -> Locale.forLanguageTag(lang)
        }
        val displayLocale = if (useDefaultLocale) Locale.getDefault() else locale
        return locale.getDisplayName(displayLocale)
            .replaceFirstChar { it.uppercase(displayLocale) }
    }

    private const val SCREEN_ID_EXTRA = "screen_id"
    const val SHORTCUT_OPEN_ACTION = "shortcut"

    fun Intent?.getScreenExtra(): Screen? {
        if (this?.hasExtra(SCREEN_ID_EXTRA) != true) return null

        val screenIdExtra = getIntExtra(SCREEN_ID_EXTRA, -100).takeIf {
            it != -100
        } ?: return null

        return Screen.entries.find {
            it.id == screenIdExtra
        }
    }

    fun Intent.putScreenExtra(screen: Screen?) = apply {
        if (screen == null) {
            removeExtra(SCREEN_ID_EXTRA)
        } else {
            putExtra(SCREEN_ID_EXTRA, screen.id)
        }
    }

    fun Intent?.getScreenOpeningShortcut(
        onNavigate: (Screen) -> Unit,
    ): Boolean {
        if (this == null) return false

        val screenExtra = getScreenExtra()

        if (action == SHORTCUT_OPEN_ACTION && screenExtra != null) {
            onNavigate(screenExtra)

            return true
        }

        return false
    }

    suspend fun Context.createScreenShortcut(
        screen: Screen,
        tint: Color = Color.Unspecified,
        backgroundColor: Color = Color.Unspecified,
        onFailure: (Throwable) -> Unit = {},
    ) = withContext(Dispatchers.Main.immediate) {
        runCatching {
            val context = this@createScreenShortcut
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context) && screen.icon != null) {
                val imageBitmap = screen.icon!!.toImageBitmap(
                    context = context,
                    width = 256,
                    height = 256,
                    tint = tint.takeOrElse { AppTheme.colorScheme.onPrimaryContainer },
                    backgroundColor = backgroundColor.takeOrElse { AppTheme.colorScheme.primaryContainer },
                    iconPadding = if (backgroundColor != Color.Unspecified) 80 else 0
                )

                val info = ShortcutInfoCompat.Builder(context, screen.id.toString())
                    .setShortLabel(getString(screen.title))
                    .setLongLabel(getString(screen.subtitle))
                    .setIcon(IconCompat.createWithAdaptiveBitmap(imageBitmap.asAndroidBitmap()))
                    .setIntent(
                        context.buildIntent(AppActivityClass) {
                            action = SHORTCUT_OPEN_ACTION
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putScreenExtra(screen)
                        }
                    )
                    .build()

                val callbackIntent =
                    ShortcutManagerCompat.createShortcutResultIntent(context, info)

                val successCallback = PendingIntentCompat.getBroadcast(
                    context, 0, callbackIntent, 0, false
                )

                ShortcutManagerCompat.requestPinShortcut(
                    context,
                    info,
                    successCallback?.intentSender
                )
            } else {
                throw UnsupportedOperationException()
            }
        }.onFailure {
            onFailure(it)
        }
    }

    fun Context.canPinShortcuts(): Boolean = runCatching {
        ShortcutManagerCompat.isRequestPinShortcutSupported(this)
    }.getOrNull() == true

    @SuppressLint("MissingPermission")
    fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    fun Context.shareText(value: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, value)
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(shareIntent)
    }

    fun Context.shareUris(uris: List<Uri>) {
        if (uris.isEmpty()) return

        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            type = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    getExtension(uris.first())
                ) ?: "*/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(shareIntent)
    }

    fun Context.getExtension(uri: Uri): String? = runCatching {
        val filename = getFilename(uri) ?: ""
        if (filename.endsWith(".qoi")) return "qoi"
        if (filename.endsWith(".jxl")) return "jxl"
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(
                    contentResolver.getType(uri)
                )
        } else {
            MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase(Locale.getDefault())
        }
    }.getOrNull()

    val Context.density: Density
        get() = object : Density {
            override val density: Float
                get() = resources.displayMetrics.density
            override val fontScale: Float
                get() = resources.configuration.fontScale
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun manageAllFilesIntent() = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

    @RequiresApi(Build.VERSION_CODES.R)
    fun Context.manageAppAllFilesIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            .setData("package:${packageName}".toUri())
    }

    fun Context.appSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData("package:${packageName}".toUri())
    }


    fun Uri.takePersistablePermission(): Uri = apply {
        runCatching {
            appContext.contentResolver.takePersistableUriPermission(
                this,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }.onFailure {
            it.makeLog("takePersistablePermission")
        }
    }

    fun Uri.moveToCache(): Uri? = appContext.run {
        contentResolver.openInputStream(this@moveToCache)?.use { stream ->
            val file = File(
                cacheDir,
                filename() ?: "cache_${Random.nextInt()}.tmp"
            ).apply { createNewFile() }

            file.outputStream().use { stream.copyTo(it) }

            file.toUri()
        }
    }

    fun Uri.isFromAppFileProvider() = toString().run {
        contains("content://media/external") || contains(appContext.fileProviderAuthority)
    }


}
