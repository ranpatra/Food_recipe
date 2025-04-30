
package com.poc.foodreceipe.domain.model

import com.google.gson.annotations.SerializedName

data class FoodRecipe(
    @SerializedName("results")
    val result: List<ModelResult>? = null
)