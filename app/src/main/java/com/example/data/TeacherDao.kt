package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    // --- Teacher Classes ---
    @Query("SELECT * FROM teacher_classes ORDER BY createdAt DESC")
    fun getAllTeacherClasses(): Flow<List<TeacherClass>>

    @Query("SELECT * FROM teacher_classes ORDER BY createdAt DESC")
    suspend fun getAllTeacherClassesSync(): List<TeacherClass>

    @Query("SELECT * FROM teacher_classes WHERE id = :id LIMIT 1")
    suspend fun getTeacherClassById(id: Long): TeacherClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherClass(teacherClass: TeacherClass): Long

    @Update
    suspend fun updateTeacherClass(teacherClass: TeacherClass)

    @Delete
    suspend fun deleteTeacherClass(teacherClass: TeacherClass)

    // --- Students in Class ---
    @Query("SELECT * FROM class_students WHERE classId = :classId ORDER BY rollNo ASC, studentName ASC")
    fun getStudentsForClass(classId: Long): Flow<List<ClassStudent>>

    @Query("SELECT * FROM class_students WHERE classId = :classId ORDER BY rollNo ASC, studentName ASC")
    suspend fun getStudentsForClassSync(classId: Long): List<ClassStudent>

    @Query("SELECT * FROM class_students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<ClassStudent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: ClassStudent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<ClassStudent>): List<Long>

    @Update
    suspend fun updateStudent(student: ClassStudent)

    @Delete
    suspend fun deleteStudent(student: ClassStudent)

    // --- Student Attendance Records ---
    @Query("SELECT * FROM student_attendance_records WHERE classId = :classId ORDER BY date DESC")
    fun getAttendanceForClass(classId: Long): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance_records WHERE classId = :classId ORDER BY date ASC")
    suspend fun getAttendanceForClassSync(classId: Long): List<StudentAttendanceRecord>

    @Query("SELECT * FROM student_attendance_records WHERE classId = :classId AND date = :date")
    fun getAttendanceForClassAndDate(classId: Long, date: String): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance_records WHERE classId = :classId AND date = :date")
    suspend fun getAttendanceForClassAndDateSync(classId: Long, date: String): List<StudentAttendanceRecord>

    @Query("SELECT * FROM student_attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<StudentAttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: StudentAttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<StudentAttendanceRecord>)

    @Query("DELETE FROM student_attendance_records WHERE classId = :classId AND date = :date")
    suspend fun clearAttendanceForDate(classId: Long, date: String)

    @Delete
    suspend fun deleteAttendanceRecord(record: StudentAttendanceRecord)

    @Query("DELETE FROM teacher_classes WHERE id = :id")
    suspend fun deleteTeacherClassById(id: Long)

    @Query("DELETE FROM class_students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("DELETE FROM teacher_classes")
    suspend fun clearAllTeacherClasses()

    @Query("DELETE FROM class_students")
    suspend fun clearAllStudents()

    @Query("DELETE FROM student_attendance_records")
    suspend fun clearAllAttendanceRecords()
}
