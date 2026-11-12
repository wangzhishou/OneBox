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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.RetryPolicy
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.DailyWeatherInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.weather.domain.repository.LocationProvider
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * 内置工具:`get_weather` — 查询指定城市/经纬度/设备定位的实时天气与逐日预报。
 *
 * 城市解析优先级:city 名称 > latitude+longitude > 设备最近已知位置。
 * 预报天数 1-7,days=1 时只返回实时天气。
 */
class GetWeatherTool @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "get_weather"

    override val description: String =
        textProvider.string(R.string.agent_tool_get_weather_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_get_weather_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_get_weather_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    // 声明仅供参考: 声明了定位权限 (requiredPermissions 非空),
    // 执行层安全约束会强制跳过重试, 避免重复权限申请
    override val retryPolicy: RetryPolicy = RetryPolicy.API_DEFAULT

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_get_weather_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_get_weather_examples)

    override val permissionRequest: PermissionRequest = PermissionRequest.LOCATION

    override val executionTimeoutMs: Long = 30_000L

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "city" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_get_weather_param_city),
            ),
            "latitude" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_get_weather_param_latitude),
            ),
            "longitude" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_get_weather_param_longitude),
            ),
            "days" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_get_weather_param_days),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val city = resolveCity(json).getOrThrow()
            val weather = weatherRepository.getWeatherAtLocation(city.lat, city.lon).getOrThrow()
            val days = json.optInt("days", 1).coerceIn(1, 7)
            val forecast = if (days > 1) {
                weatherRepository.getDailyForecast(city.id, days).getOrThrow()
            } else {
                emptyList()
            }

            AgentToolResult(content = buildResultJson(city, weather, forecast).toString())
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_get_weather_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true,
            )
        }
    }

    private suspend fun resolveCity(json: JSONObject): Result<CityInfo> {
        val cityName = json.optString("city").takeIf { it.isNotBlank() }
        if (cityName != null) {
            return weatherRepository.getCityByName(cityName)
        }

        val hasLat = json.has("latitude")
        val hasLon = json.has("longitude")
        if (hasLat && hasLon) {
            return weatherRepository.getCityAtLocation(
                lat = json.getDouble("latitude"),
                lon = json.getDouble("longitude"),
            )
        }
        require(!hasLat && !hasLon) {
            textProvider.string(R.string.agent_tool_get_weather_latlon_incomplete)
        }

        val location = locationProvider.lastKnownLocation()
            ?: return Result.failure(
                IllegalStateException(
                    textProvider.string(R.string.agent_tool_get_weather_location_unavailable)
                )
            )
        return weatherRepository.getCityAtLocation(location.latitude, location.longitude)
    }

    private fun buildResultJson(
        city: CityInfo,
        weather: WeatherInfo,
        forecast: List<DailyWeatherInfo>,
    ): JSONObject {
        return JSONObject().apply {
            put("success", true)
            put("city", JSONObject().apply {
                put("name", city.name)
                put("adm1", city.adm1)
                put("adm2", city.adm2)
                put("country", city.country)
                put("timezone", city.tz)
            })
            put("now", JSONObject().apply {
                put("obsTime", weather.obsTime)
                put("temp", weather.temp)
                put("feelsLike", weather.feelsLike)
                put("text", weather.text)
                put("windDir", weather.windDir)
                put("windScale", weather.windScale)
                put("windSpeed", weather.windSpeed)
                put("humidity", weather.humidity)
                put("precip", weather.precip)
                put("pressure", weather.pressure)
                put("vis", weather.vis)
            })
            if (forecast.isNotEmpty()) {
                put("forecast", JSONArray().apply {
                    forecast.forEach { day ->
                        put(JSONObject().apply {
                            put("date", day.fxDate)
                            put("tempMax", day.tempMax)
                            put("tempMin", day.tempMin)
                            put("textDay", day.textDay)
                            put("textNight", day.textNight)
                            put("windDirDay", day.windDirDay)
                            put("windScaleDay", day.windScaleDay)
                            put("humidity", day.humidity)
                            put("precip", day.precip)
                            put("sunrise", day.sunrise)
                            put("sunset", day.sunset)
                        })
                    }
                })
            }
        }
    }
}
