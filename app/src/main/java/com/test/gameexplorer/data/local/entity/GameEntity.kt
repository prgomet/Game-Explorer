package com.test.gameexplorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val backgroundImage: String?,
    val rating: Double,
    val released: String?,
    // Stored as a comma-separated string to simplify local filtering without complex junction tables
    val genres: String
)
