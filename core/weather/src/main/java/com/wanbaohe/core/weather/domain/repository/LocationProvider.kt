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

package com.wanbaohe.core.weather.domain.repository

import com.wanbaohe.core.weather.domain.model.GeoCoordinates

/**
 * 设备定位能力的抽象。
 *
 * 天气查询缺省城市时用它兜底取设备最近已知位置;
 * 具体系统 API(LocationManager 等)由 data 层实现注入。
 */
interface LocationProvider {

    /** 设备最近一次已知位置;未授权定位权限或系统无缓存位置时返回 null */
    fun lastKnownLocation(): GeoCoordinates?
}
