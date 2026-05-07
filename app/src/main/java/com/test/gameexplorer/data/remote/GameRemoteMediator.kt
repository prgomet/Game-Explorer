package com.test.gameexplorer.data.remote

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.test.gameexplorer.data.local.GameDatabase
import com.test.gameexplorer.data.local.entity.GameEntity
import com.test.gameexplorer.data.local.entity.RemoteKeysEntity
import com.test.gameexplorer.data.mapper.toEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
/**
 * A RemoteMediator that acts as a synchronization point between the RAWG API and our local database.
 * 
 * It handles the heavy lifting of:
 * 1. Deciding which page to fetch based on the [LoadType].
 * 2. Fetching data from the network.
 * 3. Persisting that data into Room while managing pagination keys.
 * 4. Handling errors gracefully so the UI can show retry buttons.
 */
class GameRemoteMediator(
    private val api: RawgApi,
    private val database: GameDatabase,
    private val selectedGenres: String
) : RemoteMediator<Int, GameEntity>() {

    private val tag = "GameRemoteMediator"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GameEntity>
    ): MediatorResult {
        Log.d(tag, "Initiating load sequence. Type: $loadType | Filters: $selectedGenres")

        // Step 1: Resolve the target page
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = database.remoteKeysDao().getLastRemoteKey()
                val nextKey = remoteKeys?.nextKey
                
                // If nextKey is null, it means we've either reached the end or haven't loaded anything yet.
                // If we're in APPEND but have no keys, we return Success because REFRESH should have handled it.
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }

        // Step 2: Network operation with error handling
        return try {
            val genreFilter = selectedGenres.ifBlank { null }
            
            val response = api.getGames(
                genres = genreFilter,
                page = page,
                pageSize = state.config.pageSize
            )

            val games = response.results.map { it.toEntity() }
            val endOfPaginationReached = games.isEmpty()

            // Step 3: Atomic database update
            database.withTransaction {
                // If we're refreshing, the old data is considered stale. Purge it.
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                    database.gameDao().clearAll()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = games.map {
                    RemoteKeysEntity(gameId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.remoteKeysDao().insertAll(keys)
                database.gameDao().insertAll(games)
            }

            Log.d(tag, "Load successful. Page: $page, Count: ${games.size}, EndReached: $endOfPaginationReached")
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            
        } catch (e: IOException) {
            Log.e(tag, "IO Error during load: ${e.message}")
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            Log.e(tag, "API Error during load: Code ${e.code()}")
            MediatorResult.Error(e)
        }
    }
}
