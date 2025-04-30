
package com.poc.foodreceipe.data.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.poc.foodreceipe.domain.model.ModelResult
import com.poc.foodreceipe.utils.AppConstants

@Entity(tableName = AppConstants.FAVORITES_TABLE)
data class BookedRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var result: ModelResult
)