package com.poc.foodreceipe.presentation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.poc.foodreceipe.data.DataStoreRepository
import com.poc.foodreceipe.data.Repository
import com.poc.foodreceipe.data.database.local.entities.BookedRecipeEntity
import com.poc.foodreceipe.data.database.local.entities.FoodJokesEntity
import com.poc.foodreceipe.data.database.local.entities.RecipeEntity
import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.domain.model.ModelResult
import com.poc.foodreceipe.domain.model.FoodRecipe
import com.poc.foodreceipe.utils.AppConstants.Companion.DEFAULT_DIET_TYPE
import com.poc.foodreceipe.utils.AppConstants.Companion.DEFAULT_MEAL_TYPE
import com.poc.foodreceipe.utils.AppConstants.Companion.DEFAULT_RECIPES_NUMBER
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_ADD_RECIPE_INFORMATION
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_API_KEY
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_DIET
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_FILL_INGREDIENTS
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_NUMBER
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_SEARCH
import com.poc.foodreceipe.utils.AppConstants.Companion.QUERY_TYPE
import com.poc.foodreceipe.utils.CryptoHelper
import com.poc.foodreceipe.utils.NetworkChecker
import com.poc.foodreceipe.utils.NetworkResponse
import com.poc.foodreceipe.presentation.viewModels.events.MainEvent
import com.poc.foodreceipe.presentation.viewModels.states.MainUiState
import com.poc.foodreceipe.presentation.viewModels.states.RecipesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository,
    private val networkChecker: NetworkChecker,
    private val cryptoHelper: CryptoHelper,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _netState = MutableStateFlow(RecipesUiState())
    val netState: StateFlow<RecipesUiState> = _netState.asStateFlow()


    init {
        observeLastUpdateTime()
        observeNetworkStatus()
        observeDatabase()
    }

    // Add this helper property to get API key
    private val apiKey: String
        get() = cryptoHelper.getApiKey()
            ?: throw IllegalStateException("API key not configured")

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.local.readRecipes().collect { recipes ->
                _uiState.update {
                    it.copy(recipes = recipes.flatMap { it.recipe.result ?: emptyList() })
                }
            }
            repository.local.readJokes().collect { jokes ->
                _uiState.update { it.copy(
                    foodJoke = jokes.firstOrNull()?.foodJoke
                ) }
            }
        }

        viewModelScope.launch {
            repository.local.readBooked().collect { favorites ->
                _uiState.update { it.copy(favoriteRecipes = favorites) }
            }
        }

        viewModelScope.launch {
            repository.local.readJokes().collect { jokes ->
                _uiState.update {
                    it.copy(foodJoke = jokes.firstOrNull()?.foodJoke)
                }
            }
        }
    }

    private fun observeLastUpdateTime() {
        viewModelScope.launch {
            dataStoreRepository.lastUpdateTime.collect { lastUpdateTime ->
                checkForRecipeUpdates(lastUpdateTime)
            }
        }
    }

    suspend fun checkForRecipeUpdates(lastUpdate: Long) {
        val currentTime = System.currentTimeMillis()
        val updateInterval = 24 * 60 * 60 * 1000

        if (currentTime - lastUpdate > updateInterval && _netState.value.isNetworkAvailable){
            try {
                _uiState.update { it.copy(isLoading = true) }

                val response = repository.remote.getRecipes(applyQueries())
                handleRecipesResponse(response).let { result ->
                    if (result is NetworkResponse.SuccessResponse) {
                        result.data?.let { cacheRecipes(it) }
                        dataStoreRepository.saveLastUpdateTime(currentTime)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Update failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.SearchRecipes -> handleSearch(event.query)
            is MainEvent.RefreshRecipes -> forceRefresh()
            is MainEvent.AddToFavorites -> addFavorite(event.recipe)
            is MainEvent.RemoveFromFavorites -> removeFavorite(event.recipe)
            is MainEvent.GetFoodJoke -> getFoodJoke()
            MainEvent.ClearError -> clearError()
        }
    }

    private fun handleSearch(query: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val response = repository.remote.searchRecipes(applySearchQuery(query))
            handleRecipesResponse(response).let { result ->
                if (result is NetworkResponse.SuccessResponse) {
                    result.data?.let { cacheRecipes(it) }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Search failed: ${e.message}") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun forceRefresh() = viewModelScope.launch {
        try {
            checkForRecipeUpdates(0L)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Refresh failed") }
        }
    }

    private fun addFavorite(recipe: ModelResult) = viewModelScope.launch {
        val isBooked = repository.local.checkFavourites(recipe.recipeId)

        if (isBooked) {
            // Delete using both ID and result for safety
            repository.local.deleteBookedRecipe(
                BookedRecipeEntity(
                    id = recipe.recipeId,
                    result = recipe
                )
            )
        } else {
            // Insert with the complete result
            repository.local.insertBooked(
                BookedRecipeEntity(
                    id = recipe.recipeId,
                    result = recipe
                )
            )
        }
    }

    private fun removeFavorite(recipe: BookedRecipeEntity) = viewModelScope.launch {
        try {
            repository.local.deleteBookedRecipe(recipe)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Remove failed") }
        }
    }

    private fun getFoodJoke() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val response = repository.remote.getFoodJoke(apiKey)
            when (val result = handleJokeResponse(response)) {
                is NetworkResponse.SuccessResponse -> {
                    result.data?.let { joke ->
                        repository.local.insertJokes(FoodJokesEntity(joke))
                    }
                }
                is NetworkResponse.ErrorResponse -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Joke failed") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun cacheRecipes(foodRecipe: FoodRecipe) {
        try {
            repository.local.insertRecipes(RecipeEntity(foodRecipe))
            _uiState.update { state ->
                state.copy(
                    recipes = foodRecipe.result?: emptyList(),
                    error = null
                )
            }
        } catch (e: Exception) {
            _netState.update {
            it.copy(networkMessage = "Failed to cache recipes") }
            throw e
        }
    }

    private fun handleRecipesResponse(response: Response<FoodRecipe>) =
        when {
            response.isSuccessful && response.body()?.result?.isNotEmpty() == true -> {
                NetworkResponse.SuccessResponse(response.body()!!)
            }
            response.code() == 402 -> NetworkResponse.ErrorResponse("API Limit")
            else -> NetworkResponse.ErrorResponse("Recipes unavailable")
        }

    private fun handleJokeResponse(response: Response<FoodJokes>) =
        when {
            response.isSuccessful -> NetworkResponse.SuccessResponse(response.body()!!)
            response.code() == 402 -> NetworkResponse.ErrorResponse("API Limit")
            else -> NetworkResponse.ErrorResponse("Joke unavailable")
        }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkChecker.getNetworkAvailability().collect { isNetworkAvailable ->
                val wasUnavailable = !_netState.value.isNetworkAvailable
                _netState.update { state ->
                    state.copy(
                        isNetworkAvailable = isNetworkAvailable,
                        networkMessage = when {
                            !isNetworkAvailable -> "No internet connection"
                            wasUnavailable && isNetworkAvailable -> "Back Online"
                            else -> null
                        }
                    )
                }

                // If we're back online, try to refresh recipes
                if (isNetworkAvailable && wasUnavailable) {
                    viewModelScope.launch {
                        checkForRecipeUpdates(0L)
                    }
                }

                // If we're offline, ensure we're showing cached recipes
                if (!isNetworkAvailable) {
                    observeDatabase()
                }
            }
        }
    }

    private fun applyQueries() = mapOf(
        QUERY_NUMBER to DEFAULT_RECIPES_NUMBER,
        QUERY_API_KEY to apiKey,
        QUERY_TYPE to DEFAULT_MEAL_TYPE,
        QUERY_DIET to DEFAULT_DIET_TYPE,
        QUERY_ADD_RECIPE_INFORMATION to "true",
        QUERY_FILL_INGREDIENTS to "true"
    )

    private fun applySearchQuery(query: String) = mapOf(
        QUERY_SEARCH to query,
        QUERY_NUMBER to DEFAULT_RECIPES_NUMBER,
        QUERY_API_KEY to apiKey,
        QUERY_ADD_RECIPE_INFORMATION to "true",
        QUERY_FILL_INGREDIENTS to "true"
    )
}