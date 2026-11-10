package com.gameexplorer.app.data

import com.gameexplorer.app.network.GameApi
import com.gameexplorer.app.network.GameDetailDto
import com.gameexplorer.app.network.GameDto
import kotlinx.coroutines.flow.Flow

class GameRepository(private val api: GameApi, private val db: AppDatabase) {
    suspend fun list(): List<GameDto> = api.games()
    suspend fun detail(id: Int): GameDetailDto = api.game(id)
    fun favorites(user: Long): Flow<List<FavoriteEntity>> = db.favorites().observe(user)
    suspend fun toggle(user: Long, game: GameDetailDto, isFavorite: Boolean) {
        if (isFavorite) db.favorites().delete(user, game.id)
        else db.favorites().insert(FavoriteEntity(user, game.id, game.title, game.thumbnail.orEmpty(), game.genre.orEmpty()))
    }
}
