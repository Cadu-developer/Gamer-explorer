package com.gameexplorer.app.navigation

import kotlinx.serialization.Serializable

@Serializable data object Welcome
@Serializable data object Login
@Serializable data object Register
@Serializable data object Home
@Serializable data object Favorites
@Serializable data object Profile
@Serializable data class Details(val id: Int)
