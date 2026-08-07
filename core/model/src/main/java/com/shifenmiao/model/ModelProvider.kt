package com.shifenmiao.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiProviderTypeAdapter
import com.shifenmiao.model.ai.ListOrStringContent
import com.shifenmiao.model.ai.ListOrStringContentTypeAdapter
import com.shifenmiao.model.deserializer.DataValueTypeAdapter

object ModelProvider {

    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(ListOrStringContent::class.java, ListOrStringContentTypeAdapter())
            .registerTypeAdapter(AiProvider::class.java, AiProviderTypeAdapter())
            .registerTypeAdapter(DataValue::class.java, DataValueTypeAdapter())
            .create()
    }

}