package com.example.data

data class ClassWithStudentCount(
    val teacherClass: TeacherClass,
    val studentCount: Int,
    val totalSessionsHeld: Int,
    val averageAttendancePercentage: Double
)

data class StudentWithAttendanceStats(
    val student: ClassStudent,
    val totalSessions: Int,
    val attendedSessions: Int,
    val absentSessions: Int,
    val leaveSessions: Int,
    val percentage: Double,
    val currentDayStatus: String? = null // status on the selected date if recorded
)

data class ClassDailyAttendanceSummary(
    val date: String,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int,
    val leaveCount: Int,
    val unmarkedCount: Int
)
