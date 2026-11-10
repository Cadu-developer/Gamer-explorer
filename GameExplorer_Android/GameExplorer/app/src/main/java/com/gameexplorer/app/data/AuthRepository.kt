package com.gameexplorer.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.sessionStore by preferencesDataStore(name = "session")

class AuthRepository(private val context: Context, private val dao: UserDao) {
    private val sessionKey = longPreferencesKey("logged_user_id")
    val session = context.sessionStore.data.map { it[sessionKey] ?: -1L }
    private fun hash(password: String, salt: String): String = MessageDigest.getInstance("SHA-256")
        .digest("$salt:$password".toByteArray()).joinToString("") { "%02x".format(it) }
    suspend fun register(name: String, email: String, password: String): String? {
        val cleanEmail = email.trim().lowercase()
        if (name.isBlank()) return "Informe seu nome."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) return "Informe um e-mail válido."
        if (password.length < 6) return "A senha precisa ter pelo menos 6 caracteres."
        if (dao.byEmail(cleanEmail) != null) return "Este e-mail já está cadastrado."
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        return try {
            val id = dao.insert(UserEntity(name = name.trim(), email = cleanEmail, passwordHash = hash(password, salt), salt = salt))
            context.sessionStore.edit { it[sessionKey] = id }
            null
        } catch (_: Exception) { "Não foi possível cadastrar. Confira se o e-mail já existe." }
    }
    suspend fun login(email: String, password: String): String? {
        val user = dao.byEmail(email.trim().lowercase())
        if (user == null || user.passwordHash != hash(password, user.salt)) return "E-mail ou senha incorretos."
        context.sessionStore.edit { it[sessionKey] = user.id }
        return null
    }
    suspend fun logout() { context.sessionStore.edit { it.remove(sessionKey) } }
    suspend fun user(id: Long): UserEntity? = dao.byId(id)
    suspend fun editName(id: Long, name: String) { if (name.isNotBlank()) dao.updateName(id, name.trim()) }
}
