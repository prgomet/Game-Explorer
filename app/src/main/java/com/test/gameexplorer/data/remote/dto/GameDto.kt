package com.test.gameexplorer.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameResponse(
    val results: List<GameDto>
)

@JsonClass(generateAdapter = true)
data class GameDto(
    val id: Int,
    val name: String,
    @property:Json(name = "background_image") val backgroundImage: String?,
    val rating: Double,
    val released: String?,
    val genres: List<GenreShortDto>
)

@JsonClass(generateAdapter = true)
data class GenreShortDto(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class GameDetailsDto(
    val id: Int,
    val name: String,
    val description: String?,
    @property:Json(name = "description_raw") val descriptionRaw: String?,
    @property:Json(name = "background_image") val backgroundImage: String?,
    val rating: Double,
    val released: String?,
    val website: String?,
    val genres: List<GenreShortDto>,
    val developers: List<DeveloperDto>?,
    val publishers: List<PublisherDto>?,
    val platforms: List<PlatformDto>?
)

@JsonClass(generateAdapter = true)
data class DeveloperDto(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class PublisherDto(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class PlatformDto(
    val platform: PlatformInfo
)

@JsonClass(generateAdapter = true)
data class PlatformInfo(
    val id: Int,
    val name: String
)
