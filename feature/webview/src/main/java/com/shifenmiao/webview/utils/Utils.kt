/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.webview.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import com.shifenmiao.core.constants.Strings.USER_AGENT
import com.shifenmiao.webview.BuildConfig

object Utils {
    private fun detectUri(uri: Uri): Boolean {
        val scheme = uri.scheme
        // Based on some condition you need to determine if you are going to load the url
        // in your web view itself or in a browser.
        // You can use `host` or `scheme` or any part of the `uri` to decide.
        return (scheme?.startsWith("http", true) == true
                || scheme?.startsWith("https", true) == true)
    }

    fun handleUri(uri: Uri, view: WebView?, context: Context): Boolean {
        return if (detectUri(uri)) {
            view?.loadUrl(uri.toString())
            true
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent, null)
                true
            } catch (e: Exception) {
                return false;
            }
        }
    }

    fun customUserAgent(webView: WebView): String {
        return "${webView.settings.userAgentString} ${USER_AGENT}/${BuildConfig.VersionCode} "
    }
    
    /**
     * 检查网络是否可用
     * 
     * 对不同Android API版本做了兼容处理
     * - Android 10(API 29)及以上使用NetworkCapabilities方式
     * - 旧版本使用传统ConnectivityManager方式检查
     * 
     * @param context 应用上下文
     * @return 网络是否可用
     */
    fun checkNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29)及以上版本使用NetworkCapabilities
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            )
        } else {
            // 向后兼容老版本Android
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}
