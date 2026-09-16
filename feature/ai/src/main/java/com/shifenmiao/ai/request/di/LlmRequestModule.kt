package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.DefaultLlmContextBudgetService
import com.shifenmiao.ai.request.LlmContextBudgetService
import com.shifenmiao.ai.request.LlmProviderAdapter
import com.shifenmiao.ai.request.LocalOnDeviceAdapter
import com.shifenmiao.ai.request.adapter.AnthropicAdapter
import com.shifenmiao.ai.request.adapter.OpenAiChatAdapter
import com.shifenmiao.ai.request.adapter.OwnProxyAdapter
import com.shifenmiao.ai.request.adapter.ResponsesAdapter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class LlmRequestBindingsModule {

    @Binds
    @IntoSet
    abstract fun bindOpenAiChatAdapter(impl: OpenAiChatAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindResponsesAdapter(impl: ResponsesAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindAnthropicAdapter(impl: AnthropicAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindOwnProxyAdapter(impl: OwnProxyAdapter): LlmProviderAdapter

    @Binds
    @IntoSet
    abstract fun bindLocalOnDeviceAdapter(impl: LocalOnDeviceAdapter): LlmProviderAdapter

    // LocalLlmRuntime / LocalLlmModelRegistry 的绑定按渠道拆在 flavor sourceSet 的
    // LocalLlmFlavorModule 中(src/litertlm = google/foss, src/domestic = 国内 6 渠道)。

    @Binds
    abstract fun bindLlmContextBudgetService(impl: DefaultLlmContextBudgetService): LlmContextBudgetService
}
