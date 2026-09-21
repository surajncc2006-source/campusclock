package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_students",
    foreignKeys = [
        ForeignKey(
            entity = TeacherClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("rollNo")
    ]
)
data class ClassStudent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val classId: Long,
    val studentName: String,
    val rollNo: String,
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
