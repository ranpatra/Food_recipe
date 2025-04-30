package com.poc.foodreceipe.presentation.viewModels.states

import com.poc.foodreceipe.data.database.local.entities.BookedRecipeEntity
import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.domain.model.ModelResult

data class MainUiState(
    val recipes: List<ModelResult> = emptyList(),
    val favoriteRecipes: List<BookedRecipeEntity> = emptyList(),
    val foodJoke: FoodJokes? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
)