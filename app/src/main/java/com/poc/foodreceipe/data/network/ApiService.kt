
package com.poc.foodreceipe.data.network

import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.domain.model.FoodRecipe
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
        @GET("/recipes/complexSearch")
        suspend fun getRecipes(
                @QueryMap queries: Map<String, String>
        ): Response<FoodRecipe>

        @GET("/recipes/complexSearch")
        suspend fun searchRecipes(
                @QueryMap searchQuery: Map<String, String>
        ): Response<FoodRecipe>

        @GET("food/jokes/random")
        suspend fun getFoodJoke(
                @Query("apiKey") apiKey: String
        ): Response<FoodJokes>
}
