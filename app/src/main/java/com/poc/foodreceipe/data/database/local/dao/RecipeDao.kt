
package com.poc.foodreceipe.data.database.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.poc.foodreceipe.data.database.local.entities.BookedRecipeEntity
import com.poc.foodreceipe.data.database.local.entities.FoodJokesEntity
import com.poc.foodreceipe.data.database.local.entities.RecipeEntity
import com.poc.foodreceipe.utils.AppConstants.Companion.FAVORITES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookedMarked(recipesEntity: BookedRecipeEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodJoke(foodJokeEntity: FoodJokesEntity)


    @Query("SELECT * FROM munch_recipes_table ORDER BY id ASC")
    fun readRecipes(): Flow<List<RecipeEntity>>


    @Query("SELECT * FROM booked_table ORDER BY id ASC")
    fun readBookedRecipes(): Flow<List<BookedRecipeEntity>>


    @Query("SELECT * FROM food_joke_table ORDER BY id ASC")
    fun readFoodJoke(): Flow<List<FoodJokesEntity>>

    @Delete
    suspend fun deleteBookedRecipe(bookedRecipeEntity: BookedRecipeEntity)

    @Query("DELETE FROM booked_table")
    suspend fun deleteAllBookedRecipe()

    @Query("SELECT EXISTS(SELECT 1 FROM $FAVORITES_TABLE WHERE id = :recipeId LIMIT 1)")
    suspend fun isRecipeBooked(recipeId: Int): Boolean
}