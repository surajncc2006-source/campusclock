package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "teacher_classes",
    indices = [
        Index("courseCode"),
        Index("semester")
    ]
)
data class TeacherClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val className: String, // e.g., "Data Structures & Algorithms"
    val courseCode: String = "", // e.g., "CS-301"
    val department: String = "Computer Science", // e.g. "Computer Science", "Commerce", "Arts"
    val semester: String = "Semester 3", // e.g., "Sem 3", "Section A"
    val section: String = "A", // Section A, B, etc.
    val academicYear: String = "2024-2025",
    val colorHex: String = "#1E3A8A",
    val createdAt: Long = System.currentTimeMillis()
)
