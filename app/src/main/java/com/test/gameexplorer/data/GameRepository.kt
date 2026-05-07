package com.test.gameexplorer.data

import androidx.paging.PagingData
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.data.model.GameDetails
import com.test.gameexplorer.data.model.Genre

import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGames(genres: Set<String>): Flow<PagingData<Game>>
    suspend fun getGameDetails(id: Int): GameDetails
    suspend fun getGenres(): List<Genre>
    fun getSelectedGenres(): Flow<Set<String>>
    suspend fun saveSelectedGenres(genres: Set<String>)
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
