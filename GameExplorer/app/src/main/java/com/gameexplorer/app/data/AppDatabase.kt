package com.gameexplorer.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "gameId"])
data class FavoriteEntity(
    val userId: Long,
    val gameId: Int,
    val title: String,
    val thumbnail: String,
    val genre: String
)

@Dao interface UserDao {
    @Insert suspend fun insert(user: UserEntity): Long
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") suspend fun byEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") suspend fun byId(id: Long): UserEntity?
    @Query("UPDATE users SET name = :name WHERE id = :id") suspend fun updateName(id: Long, name: String)
}

@Dao interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE userId = :userId AND gameId = :gameId") suspend fun delete(userId: Long, gameId: Int)
    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY title") fun observe(userId: Long): Flow<List<FavoriteEntity>>
}

@Database(entities = [UserEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun users(): UserDao
    abstract fun favorites(): FavoriteDao
}
