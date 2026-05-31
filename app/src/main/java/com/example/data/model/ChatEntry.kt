package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_entries")
data class ChatEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "prakriti_mitra"
    val text: String,
    val detectedLanguage: String, // "English", "Hindi", "Telugu"
    val timestamp: Long = System.currentTimeMillis()
)
