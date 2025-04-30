package com.poc.foodreceipe.presentation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.poc.foodreceipe.data.DataStoreRepository
import com.poc.foodreceipe.data.Repository
import com.poc.foodreceipe.utils.NetworkChecker
import com.poc.foodreceipe.presentation.viewModels.events.RecipesEvent
import com.poc.foodreceipe.presentation.viewModels.states.RecipesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val networkChecker: NetworkChecker,
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.readMealAndDietType.collect { preferences ->
                _uiState.update { it.copy(mealAndDietType = preferences) }
            }
        }
    }


    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.UpdateMealAndDietType -> {
                saveMealAndDietType(
                    event.mealType,
                    event.mealTypeId,
                    event.dietType,
                    event.dietTypeId
                )
            }
            is RecipesEvent.SearchRecipes -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is RecipesEvent.RefreshRecipes -> {

            }
            RecipesEvent.ClearSearch -> {
                _uiState.update { it.copy(searchQuery = "") }
            }

            is RecipesEvent.GetRecipeDetails -> {
                getRecipeDetails(event.recipeId)
            }
        }
    }
    private fun getRecipeDetails(recipeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // First try to get from local database
                repository.local.readRecipes().collect { recipes ->
                    val recipe = recipes.flatMap { it.recipe.result ?: emptyList() }
                        .find { it.recipeId == recipeId }

                    if (recipe != null) {
                        _uiState.update {
                            it.copy(
                                selectedRecipe = recipe,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        // If not found locally, fetch from API
                        val queries = mapOf("id" to recipeId.toString())
                        val response = repository.remote.getRecipes(queries)
                        if (response.isSuccessful && response.body() != null) {
                            val fetchedRecipe = response.body()?.result?.find { it.recipeId == recipeId }
                            if (fetchedRecipe != null) {
                                _uiState.update {
                                    it.copy(
                                        selectedRecipe = fetchedRecipe,
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Recipe not found"
                                    )
                                }
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to load recipe details"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    private fun saveMealAndDietType(
        mealType: String,
        mealTypeId: Int,
        dietType: String,
        dietTypeId: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepository.saveMealAndDietType(
            mealType,
            mealTypeId,
            dietType,
            dietTypeId
        )
    }
}