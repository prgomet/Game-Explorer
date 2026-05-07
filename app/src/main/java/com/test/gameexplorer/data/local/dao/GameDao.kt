package com.test.gameexplorer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.gameexplorer.data.local.entity.GameEntity

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Query("SELECT * FROM games WHERE genres LIKE :genreQuery ORDER BY rating DESC, name ASC")
    fun getGamesByGenres(genreQuery: String): PagingSource<Int, GameEntity>

    @Query("DELETE FROM games")
    suspend fun clearAll()
}
