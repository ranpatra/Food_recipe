
package com.poc.foodreceipe.domain.model

import com.google.gson.annotations.SerializedName

data class FoodJokes(
    @SerializedName("text")
    val text: String
)