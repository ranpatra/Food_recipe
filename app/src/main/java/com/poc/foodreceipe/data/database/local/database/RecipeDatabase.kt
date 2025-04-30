
package com.poc.foodreceipe.data.database.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.poc.foodreceipe.data.database.local.dao.RecipeDao
import com.poc.foodreceipe.data.database.local.entities.BookedRecipeEntity
import com.poc.foodreceipe.data.database.local.entities.FoodJokesEntity
import com.poc.foodreceipe.data.database.local.entities.RecipeEntity

@Database(
    entities = [
        RecipeEntity::class,
        FoodJokesEntity::class,
        BookedRecipeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ReceipeTypeConverter::class)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun munchDao(): RecipeDao
}