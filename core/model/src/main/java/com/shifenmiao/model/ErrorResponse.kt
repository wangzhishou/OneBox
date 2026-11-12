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

package com.shifenmiao.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: Int = 0,
    val name: String = "",
    val message: String = "",
    val details: DataValue = DataValue.StringValue(""),
)



@Serializable
data class StrapiErrorResponse(
    val data: Map<String, DataValue>? = emptyMap(),
    val error: ErrorResponse = ErrorResponse()
)


@Serializable(com.shifenmiao.model.deserializer.DataValueKSerializer::class)
sealed class DataValue {
    @Serializable
    data class StringValue(val value: String) : DataValue()

    @Serializable
    data class IntValue(val value: Int) : DataValue()

    @Serializable
    data class BooleanValue(val value: Boolean) : DataValue()

    @Serializable
    data class StringListValue(val value: List<String>) : DataValue()

    @Serializable
    data class MapValue(val value: Map<String, DataValue>) : DataValue()
}