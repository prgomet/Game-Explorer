package com.test.gameexplorer.data.mapper

import com.test.gameexplorer.data.local.entity.GameEntity
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.data.model.GameDetails
import com.test.gameexplorer.data.model.Genre
import com.test.gameexplorer.data.remote.dto.GameDetailsDto
import com.test.gameexplorer.data.remote.dto.GameDto
import com.test.gameexplorer.data.remote.dto.GenreDto

/**
 * Domain mappers to convert between Network DTOs, Database Entities, and UI Models.
 * 
 * Note: We're using simple extension functions for mapping. For larger projects,
 * consider using a more robust mapping library if complexity grows.
 */

fun GameDto.toEntity(): GameEntity {
    return GameEntity(
        id = id,
        name = name,
        backgroundImage = backgroundImage,
        rating = rating,
        released = released,
        // We wrap genre IDs in commas (e.g., ",4,51,") to allow robust SQL LIKE 
        // queries without matching partial IDs (like finding '4' inside '44').
        genres = "," + genres.joinToString(",") { it.id.toString() } + ","
    )
}

fun GameEntity.toDomain(): Game {
    return Game(
        id = id,
        name = name,
        backgroundImage = backgroundImage,
        rating = rating,
        released = released,
        genres = genres.split(",").filter { it.isNotBlank() }
    )
}

fun GameDetailsDto.toDomain(): GameDetails {
    return GameDetails(
        id = id,
        name = name,
        description = descriptionRaw ?: description ?: "",
        backgroundImage = backgroundImage,
        rating = rating,
        released = released,
        website = website,
        genres = genres.map { it.name },
        developers = developers?.map { it.name } ?: emptyList(),
        publishers = publishers?.map { it.name } ?: emptyList(),
        platforms = platforms?.map { it.platform.name } ?: emptyList()
    )
}

fun GenreDto.toDomain(): Genre {
    return Genre(
        id = id,
        name = name,
        slug = slug,
        imageBackground = imageBackground
    )
}
