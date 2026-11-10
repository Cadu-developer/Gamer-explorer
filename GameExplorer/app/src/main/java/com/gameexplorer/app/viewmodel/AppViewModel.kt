package com.gameexplorer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.gameexplorer.app.data.*
import com.gameexplorer.app.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "game_explorer.db").build()
    private val auth = AuthRepository(application, db.users())
    private val api = Retrofit.Builder().baseUrl("https://www.freetogame.com/api/")
        .addConverterFactory(GsonConverterFactory.create()).build().create(GameApi::class.java)
    private val repository = GameRepository(api, db)
    val session = auth.session.stateIn(viewModelScope, SharingStarted.Eagerly, -2L)
    val user = session.flatMapLatest { id -> flow { emit(if (id >= 0) auth.user(id) else null) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val favorites = session.flatMapLatest { id -> if (id >= 0) repository.favorites(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _games = MutableStateFlow<LoadState<List<GameDto>>>(LoadState.Loading)
    val games = _games.asStateFlow()
    private val _detail = MutableStateFlow<LoadState<GameDetailDto>>(LoadState.Loading)
    val detail = _detail.asStateFlow()
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()
    init { refresh() }
    fun clearError() { _authError.value = null }
    fun refresh() { viewModelScope.launch {
        _games.value = LoadState.Loading
        _games.value = try { LoadState.Success(repository.list()) }
        catch (_: Exception) { LoadState.Error("Não foi possível carregar os jogos. Confira sua conexão e tente novamente.") }
    } }
    fun loadDetail(id: Int) { viewModelScope.launch {
        _detail.value = LoadState.Loading
        _detail.value = try { LoadState.Success(repository.detail(id)) }
        catch (_: Exception) { LoadState.Error("Não foi possível carregar os detalhes do jogo.") }
    } }
    fun login(email: String, password: String) { viewModelScope.launch { _authError.value = auth.login(email, password) } }
    fun register(name: String, email: String, password: String) { viewModelScope.launch { _authError.value = auth.register(name, email, password) } }
    fun logout() { viewModelScope.launch { auth.logout() } }
    fun saveName(name: String) { val id = session.value; if (id < 0) return
        viewModelScope.launch { auth.editName(id, name) } }
    fun toggleFavorite(game: GameDetailDto) { val id = session.value; if (id < 0) return
        viewModelScope.launch { repository.toggle(id, game, favorites.value.any { it.gameId == game.id }) } }
}
