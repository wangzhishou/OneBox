package com.shifenmiao.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: Int,
    val name: String,
    val message: String,
    val details: DataValue,
)



@Serializable
data class StrapiErrorResponse(
    val data: Map<String, DataValue>? = emptyMap(),
    val error: ErrorResponse
)


@Serializable
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