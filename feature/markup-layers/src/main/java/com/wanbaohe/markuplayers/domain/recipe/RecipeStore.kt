package com.wanbaohe.markuplayers.domain.recipe

interface RecipeStore {
    suspend fun listRecipes(): List<MarkupRecipe>

    /** Returns the recipe whose overlay references now point to durable private assets. */
    suspend fun saveRecipe(recipe: MarkupRecipe): MarkupRecipe

    suspend fun deleteRecipe(id: String)
}
