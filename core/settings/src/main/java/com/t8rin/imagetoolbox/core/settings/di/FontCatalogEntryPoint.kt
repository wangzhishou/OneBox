package com.t8rin.imagetoolbox.core.settings.di

import com.t8rin.imagetoolbox.core.settings.domain.FontCatalog
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 供 core/ui 内的 Composable 经 EntryPointAccessors 取 FontCatalog 单例 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FontCatalogEntryPoint {
    val fontCatalog: FontCatalog
}
