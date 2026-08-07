package com.shifenmiao.app.channel

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.json.JSONObject

/**
 * Google Play 渠道运行时初始化入口
 *
 * 仅有 google flavor 会编译这个类 (位于 src/google/ 源集, 依赖为 googleImplementation).
 * 国内 flavor 不会带这段代码, 互不干扰.
 *
 * 调用方在 AppApplication.onCreate() 里通过反射/类名查找执行:
 *   GoogleChannelInitializer.tryInit(this)
 *
 * 项目刻意不应用 google-services Gradle 插件, 改为运行时读取
 * app/src/google/assets/google-services.json 构建 FirebaseOptions 完成初始化.
 * release 与 debug 包都会初始化 Firebase:
 *   - json 内含多个 client 条目时按运行时包名匹配 (com.shifenmiao.app / .debug),
 *     debug 包的崩溃上报到 Firebase 控制台里独立的 OneBoxDebug 应用, 与线上数据隔离
 *   - debug 包未开启代码混淆, 堆栈无需 mapping 即可读
 * json 缺失或解析失败时静默降级 (不初始化 Firebase), 绝不能 crash 整个 App,
 * 此时 core/crash 的 AnalyticsManagerImpl 会自动降级为 no-op.
 */
object GoogleChannelInitializer {

    private const val TAG = "GoogleChannel"
    private const val GOOGLE_SERVICES_ASSET = "google-services.json"

    fun tryInit(context: Context) {
        try {
            onInit(context)
        } catch (t: Throwable) {
            // 渠道初始化绝不能 crash 整个 App
            Log.e(TAG, "init failed", t)
        }
    }

    private fun onInit(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) return

        val options = loadFirebaseOptions(context)
        if (options == null) {
            Log.w(TAG, "$GOOGLE_SERVICES_ASSET missing or invalid, Firebase disabled")
            return
        }

        FirebaseApp.initializeApp(context, options)
        Log.i(TAG, "FirebaseApp initialized")
    }

    private fun loadFirebaseOptions(context: Context): FirebaseOptions? = runCatching {
        val json = context.assets.open(GOOGLE_SERVICES_ASSET)
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val clients = root.getJSONArray("client")
        require(clients.length() > 0) { "no client entry" }

        // client 数组可能包含多个 app, 优先按包名匹配, 匹配不到取第一个
        var client = clients.getJSONObject(0)
        for (i in 0 until clients.length()) {
            val candidate = clients.getJSONObject(i)
            val packageName = candidate.getJSONObject("client_info")
                .optJSONObject("android_client_info")
                ?.optString("package_name")
            if (packageName == context.packageName) {
                client = candidate
                break
            }
        }

        val projectInfo = root.optJSONObject("project_info")
        FirebaseOptions.Builder()
            .setApplicationId(
                client.getJSONObject("client_info").getString("mobilesdk_app_id")
            )
            .setApiKey(
                client.getJSONArray("api_key").getJSONObject(0).getString("current_key")
            )
            .apply {
                projectInfo?.optString("project_id")?.takeIf(String::isNotEmpty)
                    ?.let(::setProjectId)
                projectInfo?.optString("project_number")?.takeIf(String::isNotEmpty)
                    ?.let(::setGcmSenderId)
                projectInfo?.optString("storage_bucket")?.takeIf(String::isNotEmpty)
                    ?.let(::setStorageBucket)
            }
            .build()
    }.onFailure {
        Log.w(TAG, "parse $GOOGLE_SERVICES_ASSET failed", it)
    }.getOrNull()
}
