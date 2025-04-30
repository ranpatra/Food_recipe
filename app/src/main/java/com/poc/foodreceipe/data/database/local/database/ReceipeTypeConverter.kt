package com.poc.foodreceipe.data.database.local.database

import androidx.room.TypeConverter
import com.poc.foodreceipe.domain.model.FoodJokes
import com.poc.foodreceipe.domain.model.ModelResult
import com.poc.foodreceipe.domain.model.FoodRecipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReceipeTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromModelResult(modelResult: ModelResult): String {
        return gson.toJson(modelResult)
    }

    @TypeConverter
    fun toModelResult(json: String): ModelResult {
        val type = object : TypeToken<ModelResult>() {}.type
        return gson.fromJson(json, type)
    }


    @TypeConverter
    fun fromMunchRecipe(foodRecipe: FoodRecipe): String {
        return gson.toJson(foodRecipe)
    }


    @TypeConverter
    fun toMunchRecipe(json: String): FoodRecipe {
        val type = object : TypeToken<FoodRecipe>() {}.type
        return gson.fromJson(json, type)
    }


    @TypeConverter
    fun fromFoodJokes(foodJokes: FoodJokes): String {
        return gson.toJson(foodJokes)
    }

    @TypeConverter
    fun toFoodJokes(json: String): FoodJokes {
        val type = object : TypeToken<FoodJokes>() {}.type
        return gson.fromJson(json, type)
    }
}