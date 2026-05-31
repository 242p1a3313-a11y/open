package com.example.data.local

import androidx.room.*
import com.example.data.model.ChatEntry
import com.example.data.model.UserSession
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSession)

    @Query("UPDATE user_sessions SET isLoggedIn = :isLoggedIn WHERE id = 1")
    suspend fun updateLoginState(isLoggedIn: Boolean)

    @Query("UPDATE user_sessions SET ecoPoints = :points WHERE id = 1")
    suspend fun updateEcoPoints(points: Int)

    @Query("UPDATE user_sessions SET streak = :newStreak WHERE id = 1")
    suspend fun updateStreak(newStreak: Int)

    @Query("UPDATE user_sessions SET badges = :newBadges WHERE id = 1")
    suspend fun updateBadges(newBadges: String)

    @Query("DELETE FROM user_sessions")
    suspend fun clearSession()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_entries ORDER BY timestamp ASC")
    fun getAllChatsFlow(): Flow<List<ChatEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntry)

    @Query("DELETE FROM chat_entries")
    suspend fun clearChats()
}

@Database(entities = [UserSession::class, ChatEntry::class], version = 1, exportSchema = false)
abstract class EcoDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: EcoDatabase? = null

        fun getDatabase(context: android.content.Context): EcoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoDatabase::class.java,
                    "eco_friend_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
