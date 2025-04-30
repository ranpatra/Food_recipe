
package com.poc.foodreceipe.data.remote

import com.poc.foodreceipe.data.network.ApiService
import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.domain.model.FoodRecipe
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getRecipes(queries: Map<String, String>): Response<FoodRecipe> {
        return apiService.getRecipes(queries)
    }

    suspend fun searchRecipes(searchQuery: Map<String, String>): Response<FoodRecipe> {
        return apiService.searchRecipes(searchQuery)
    }

    suspend fun getFoodJoke(apiKey: String): Response<FoodJokes> {
        return apiService.getFoodJoke(apiKey)
    }
}