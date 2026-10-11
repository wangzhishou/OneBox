package com.wanbaohe.markuplayers.data.recipe

import com.wanbaohe.markuplayers.domain.recipe.RecipeStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RecipeModule {
    @Binds
    abstract fun bindRecipeStore(store: AndroidRecipeStore): RecipeStore
}
