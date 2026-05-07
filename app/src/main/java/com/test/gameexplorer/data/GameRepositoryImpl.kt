package com.test.gameexplorer.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.test.gameexplorer.data.local.GameDatabase
import com.test.gameexplorer.data.mapper.toDomain
import com.test.gameexplorer.data.model.Game
import com.test.gameexplorer.data.model.GameDetails
import com.test.gameexplorer.data.model.Genre
import com.test.gameexplorer.data.remote.GameRemoteMediator
import com.test.gameexplorer.data.remote.RawgApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val api: RawgApi,
    private val database: GameDatabase,
    private val dataStoreManager: DataStoreManager
) : GameRepository {

    @OptIn(ExperimentalPagingApi::class)
    /**
     * Orchestrates the paged stream of games. 
     * 
     * We use a [RemoteMediator] to sync network data into Room, and then use Room 
     * as the single source of truth for the UI. This ensures the app works perfectly offline.
     */
    override fun getGames(genres: Set<String>): Flow<PagingData<Game>> {
        // Comma-separated string for the RAWG API
        val genreQuery = genres.joinToString(",")
        
        val pagingSourceFactory = {
            // Local filtering logic: 
            // If the user has selected multiple genres, we'll filter by the first one 
            // to keep the local SQL query simple. The RemoteMediator handles the full 
            // multi-genre fetch from the API.
            val localFilter = if (genres.isNotEmpty()) {
                "%," + genres.first() + ",%"
            } else {
                "%%"
            }
            database.gameDao().getGamesByGenres(localFilter)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5, // Prefetch slightly ahead for a smoother experience
                enablePlaceholders = false
            ),
            remoteMediator = GameRemoteMediator(api, database, genreQuery),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun getGameDetails(id: Int): GameDetails {
        return api.getGameDetails(id).toDomain()
    }

    override suspend fun getGenres(): List<Genre> {
        return api.getGenres().results
            .map { it.toDomain() }
            .sortedBy { it.name }
    }

    override fun getSelectedGenres(): Flow<Set<String>> = dataStoreManager.selectedGenres

    override suspend fun saveSelectedGenres(genres: Set<String>) = dataStoreManager.saveSelectedGenres(genres)

    override fun isOnboardingCompleted(): Flow<Boolean> = dataStoreManager.isOnboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) = dataStoreManager.setOnboardingCompleted(completed)
}
