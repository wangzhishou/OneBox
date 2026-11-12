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

package com.shifenmiao.network.interceptor

import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.base.hilt.DeviceInfoModule
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import com.shifenmiao.model.DeviceInfo
import com.shifenmiao.network.BuildConfig
import dagger.hilt.android.EntryPointAccessors
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class GlobalParamsInterceptor : Interceptor {

    private val deviceInfoModule: DeviceInfoModule

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        deviceInfoModule = entryPoint.getDeviceInfoModule()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val deviceInfo: DeviceInfo = deviceInfoModule.getQueryParameter()
        val urlBuilder = originalUrl.newBuilder()
            .addQueryParameter("build_type", BuildConfig.BUILD_TYPE)
            .addQueryParameter("flavor", BuildConfig.FLAVOR)
            .addQueryParameter("version_name", BuildConfig.VersionName)
            .addQueryParameter("version_code", BuildConfig.VersionCode)
            .addQueryParameter("locale", LocaleUtils.getCurrentLocaleTag())

        try {
            deviceInfo.deviceId?.let {
                urlBuilder.addQueryParameter(
                    "device_id",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
            deviceInfo.deviceBrand?.let {
                urlBuilder.addQueryParameter(
                    "device_brand",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
            deviceInfo.deviceModel?.let {
                urlBuilder.addQueryParameter(
                    "device_model",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
            deviceInfo.deviceName?.let {
                urlBuilder.addQueryParameter(
                    "device_name",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
            deviceInfo.deviceNetType?.let {
                urlBuilder.addQueryParameter(
                    "device_net_type",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
            deviceInfo.deviceSdkInt?.let {
                urlBuilder.addQueryParameter("device_sdk_int", it.toString())
            }
            deviceInfo.channel?.let {
                urlBuilder.addQueryParameter(
                    "channel",
                    URLEncoder.encode(it, StandardCharsets.UTF_8.name())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception as needed, e.g., log it or return an error response
        }

        val urlWithParams = urlBuilder.build()
        val newRequest = originalRequest.newBuilder().url(urlWithParams).build()
        return chain.proceed(newRequest)
    }
}