package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "student_attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = TeacherClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassStudent::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("studentId"),
        Index("date"),
        Index(value = ["studentId", "date"], unique = true)
    ]
)
data class StudentAttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val classId: Long,
    val studentId: Long,
    val date: String, // format YYYY-MM-DD
    val status: String, // "PRESENT", "ABSENT", "LEAVE"
    val timestamp: Long = System.currentTimeMillis(),
    val remark: String = ""
) {
    companion object {
        const val STATUS_PRESENT = "PRESENT"
        const val STATUS_ABSENT = "ABSENT"
        const val STATUS_LEAVE = "LEAVE"
    }
}
