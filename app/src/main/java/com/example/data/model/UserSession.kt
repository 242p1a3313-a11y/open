package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val fullName: String = "",
    val school: String = "",
    val studentClass: String = "",
    val city: String = "",
    val state: String = "",
    val preferredLanguage: String = "English",
    val favoritePlant: String = "",
    val location: String = "",
    val plantationGoal: Int = 5,
    val treesToGrow: Int = 2,
    val streak: Int = 15,
    val ecoPoints: Int = 1200,
    val badges: String = "Green Starter" // Comma-separated: "Green Starter,Tree Guardian"
)
