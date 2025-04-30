package com.poc.foodreceipe.presentation.viewModels.states

import com.poc.foodreceipe.domain.model.MealAndDietType
import com.poc.foodreceipe.domain.model.ModelResult
import com.poc.foodreceipe.utils.AppConstants

data class RecipesUiState(
    val recipes: List<ModelResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mealAndDietType: MealAndDietType = MealAndDietType(
        selectedMealType = AppConstants.DEFAULT_MEAL_TYPE,
        selectedMealTypeId = 0,
        selectedDietType = AppConstants.DEFAULT_DIET_TYPE,
        selectedDietTypeId = 0
    ),
    val isNetworkAvailable: Boolean = true,
    val networkMessage: String? = null,
    val searchQuery: String = "",
    val selectedRecipe: ModelResult? = null,
)
