package com.gameexplorer.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class GameDto(
    val id: Int,
    val title: String,
    val thumbnail: String?,
    @SerializedName("short_description") val shortDescription: String?,
    val genre: String?,
    val platform: String?,
    val publisher: String?,
    val developer: String?,
    @SerializedName("release_date") val releaseDate: String?
)

data class GameDetailDto(
    val id: Int,
    val title: String,
    val thumbnail: String?,
    val description: String?,
    val genre: String?,
    val platform: String?,
    val publisher: String?,
    val developer: String?,
    @SerializedName("release_date") val releaseDate: String?
)

interface GameApi {
    @GET("games") suspend fun games(): List<GameDto>
    @GET("game") suspend fun game(@Query("id") id: Int): GameDetailDto
}
