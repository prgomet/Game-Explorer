package com.test.gameexplorer.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenreResponse(
    val results: List<GenreDto>
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    val id: Int,
    val name: String,
    val slug: String,
    @property:Json(name = "image_background") val imageBackground: String?
)
