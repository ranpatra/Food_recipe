package com.poc.foodreceipe.data.database.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.utils.AppConstants

@Entity(tableName = AppConstants.FOOD_JOKE_TABLE)
data class FoodJokesEntity(

    @Embedded
    var foodJoke: FoodJokes
) {
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
}