package com.test.gameexplorer.data.local

import android.content.Context
import androidx.room.Room
import com.test.gameexplorer.data.local.dao.GameDao
import com.test.gameexplorer.data.local.dao.RemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            "game_explorer_db"
        ).build()
    }

    @Provides
    fun provideGameDao(database: GameDatabase): GameDao = database.gameDao()

    @Provides
    fun provideRemoteKeysDao(database: GameDatabase): RemoteKeysDao = database.remoteKeysDao()
}