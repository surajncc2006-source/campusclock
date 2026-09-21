package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val code: String = "",
    val teacher: String = "",
    val room: String = "",
    val targetPercentage: Double = 75.0,
    val colorHex: String = "#1E3A8A",
    val createdAt: Long = System.currentTimeMillis()
)
