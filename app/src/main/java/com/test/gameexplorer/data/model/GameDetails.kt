package com.test.gameexplorer.data.model

data class GameDetails(
    val id: Int,
    val name: String,
    val description: String,
    val backgroundImage: String?,
    val rating: Double,
    val released: String?,
    val website: String?,
    val genres: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val platforms: List<String>
)
