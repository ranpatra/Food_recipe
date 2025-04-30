
package com.poc.foodreceipe.data.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.poc.foodreceipe.domain.model.FoodRecipe
import com.poc.foodreceipe.utils.AppConstants

@Entity(tableName = AppConstants.RECIPES_TABLE)
data class RecipeEntity(
    val recipe: FoodRecipe
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}