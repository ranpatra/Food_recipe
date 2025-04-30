
package com.poc.foodreceipe.data.database

import com.poc.foodreceipe.data.database.local.dao.RecipeDao
import com.poc.foodreceipe.data.database.local.entities.BookedRecipeEntity
import com.poc.foodreceipe.data.database.local.entities.FoodJokesEntity
import com.poc.foodreceipe.data.database.local.entities.RecipeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Data source for accessing local data through the RecipeDao.
 */
class LocalDataSource @Inject constructor(
    private val recipeDao: RecipeDao
) {


    fun readRecipes(): Flow<List<RecipeEntity>> {
        return recipeDao.readRecipes()
    }


    fun readBooked(): Flow<List<BookedRecipeEntity>> {
        return recipeDao.readBookedRecipes()
    }


    fun readJokes(): Flow<List<FoodJokesEntity>> {
        return recipeDao.readFoodJoke()
    }


    suspend fun insertRecipes(recipeEntity: RecipeEntity) {
        recipeDao.insertRecipe(recipeEntity)
    }


    suspend fun insertBooked(bookedRecipeEntity: BookedRecipeEntity) {
        recipeDao.insertBookedMarked(bookedRecipeEntity)
    }


    suspend fun insertJokes(foodJokeEntity: FoodJokesEntity) {
        recipeDao.insertFoodJoke(foodJokeEntity)
    }


    suspend fun deleteBookedRecipe(bookedRecipeEntity: BookedRecipeEntity) {
        recipeDao.deleteBookedRecipe(bookedRecipeEntity)
    }

    suspend fun deleteAllBooked() {
        recipeDao.deleteAllBookedRecipe()
    }

    suspend fun checkFavourites(recipeId: Int): Boolean {
        return recipeDao.isRecipeBooked(recipeId)
    }
}