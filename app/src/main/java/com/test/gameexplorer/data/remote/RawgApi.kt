package com.test.gameexplorer.data.remote

import com.test.gameexplorer.data.remote.dto.GameDetailsDto
import com.test.gameexplorer.data.remote.dto.GameResponse
import com.test.gameexplorer.data.remote.dto.GenreResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApi {
    @GET("genres")
    suspend fun getGenres(): GenreResponse

    @GET("games")
    suspend fun getGames(
        @Query("genres") genres: String?,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("ordering") ordering: String = "-rating"
    ): GameResponse

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") id: Int
    ): GameDetailsDto

    companion object {
        const val BASE_URL = "https://api.rawg.io/api/"
    }
}
