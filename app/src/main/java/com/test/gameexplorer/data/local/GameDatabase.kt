package com.test.gameexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.test.gameexplorer.data.local.dao.*
import com.test.gameexplorer.data.local.entity.*

@Database(
    entities = [GameEntity::class, RemoteKeysEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}