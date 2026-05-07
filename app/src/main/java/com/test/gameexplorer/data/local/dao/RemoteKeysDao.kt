package com.test.gameexplorer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.test.gameexplorer.data.local.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE gameId = :gameId")
    suspend fun remoteKeysGameId(gameId: Int): RemoteKeysEntity?

    @Query("SELECT * FROM remote_keys ORDER BY nextKey DESC LIMIT 1")
    suspend fun getLastRemoteKey(): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}
