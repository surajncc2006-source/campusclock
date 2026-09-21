package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.ceil
import kotlin.math.floor

data class SubjectStats(
    val subject: Subject,
    val totalHeld: Int,
    val totalAttended: Int,
    val totalAbsent: Int,
    val totalCancelled: Int,
    val percentage: Double,
    val safeBunks: Int,
    val classesNeeded: Int,
    val isEligible: Boolean // percentage >= subject.targetPercentage
)

data class OverallAttendanceStats(
    val totalHeld: Int,
    val totalAttended: Int,
    val totalAbsent: Int,
    val totalCancelled: Int,
    val percentage: Double,
    val safeBunks: Int,
    val classesNeeded: Int,
    val targetPercentage: Double = 75.0,
    val isEligible: Boolean
)

data class RecordWithSubject(
    val record: AttendanceRecord,
    val subjectName: String,
    val subjectCode: String,
    val subjectColor: String
)

class AttendanceRepository(private val dao: AttendanceDao) {

    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allRecords: Flow<List<AttendanceRecord>> = dao.getAllRecords()

    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>> =
        dao.getRecordsForDate(date)

    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>> =
        dao.getRecordsForSubject(subjectId)

    suspend fun insertSubject(subject: Subject): Long = dao.insertSubject(subject)

    suspend fun updateSubject(subject: Subject) = dao.updateSubject(subject)

    suspend fun deleteSubject(subject: Subject) = dao.deleteSubject(subject)

    suspend fun markAttendance(
        subjectId: Long,
        date: String,
        status: String,
        remark: String = ""
    ) {
        val existing = dao.getRecordForSubjectAndDate(subjectId, date)
        if (existing != null) {
            if (existing.status == status) {
                // Clicking again removes/unmarks it
                dao.deleteRecord(existing)
            } else {
                dao.updateRecord(existing.copy(status = status, remark = remark, timestamp = System.currentTimeMillis()))
            }
        } else {
            dao.insertRecord(
                AttendanceRecord(
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    remark = remark
                )
            )
        }
    }

    suspend fun quickIncrementAttendance(subjectId: Long, isPresent: Boolean, date: String) {
        dao.insertRecord(
            AttendanceRecord(
                subjectId = subjectId,
                date = date,
                status = if (isPresent) AttendanceRecord.STATUS_PRESENT else AttendanceRecord.STATUS_ABSENT,
                remark = if (isPresent) "Quick Attended" else "Quick Absent"
            )
        )
    }

    suspend fun deleteRecord(record: AttendanceRecord) = dao.deleteRecord(record)
    suspend fun deleteRecordById(id: Long) = dao.deleteRecordById(id)

    suspend fun clearAll() {
        dao.clearAllRecords()
        dao.clearAllSubjects()
    }

    suspend fun prepopulateShyamLalCollegeDefaults() {
        val sampleSubjects = listOf(
            Subject(
                name = "Core DSC: Data Structures & Algorithms",
                code = "CS-DSC-301",
                teacher = "Dr. S. K. Sharma",
                room = "Room 204, SLC",
                targetPercentage = 75.0,
                colorHex = "#1E3A8A"
            ),
            Subject(
                name = "Generic Elective: Principles of Macroeconomics",
                code = "GE-ECO-302",
                teacher = "Prof. R. K. Gupta",
                room = "Room 105, SLC",
                targetPercentage = 75.0,
                colorHex = "#880E4F"
            ),
            Subject(
                name = "Skill Enhancement: Web Development Lab",
                code = "SEC-CS-02",
                teacher = "Dr. Ananya Verma",
                room = "Computer Lab 2",
                targetPercentage = 75.0,
                colorHex = "#0D9488"
            ),
            Subject(
                name = "Value Addition: Digital Empowerment",
                code = "VAC-DU-101",
                teacher = "Dr. P. K. Singh",
                room = "Seminar Hall SLC",
                targetPercentage = 67.0, // DU Minimum
                colorHex = "#D97706"
            )
        )
        sampleSubjects.forEach { dao.insertSubject(it) }
    }

    // Combines Subjects & Records to compute live stats
    val subjectsWithStats: Flow<List<SubjectStats>> =
        combine(allSubjects, allRecords) { subjects, records ->
            val recordsBySubject = records.groupBy { it.subjectId }

            subjects.map { subject ->
                val subjectRecords = recordsBySubject[subject.id] ?: emptyList()
                val attended = subjectRecords.count { it.status == AttendanceRecord.STATUS_PRESENT }
                val absent = subjectRecords.count { it.status == AttendanceRecord.STATUS_ABSENT }
                val cancelled = subjectRecords.count { it.status == AttendanceRecord.STATUS_CANCELLED }
                val held = attended + absent

                val pct = if (held == 0) 100.0 else (attended.toDouble() / held.toDouble()) * 100.0
                val targetFrac = subject.targetPercentage / 100.0

                val safeBunks = if (held > 0 && pct >= subject.targetPercentage && targetFrac > 0) {
                    val maxPossibleHeld = floor(attended / targetFrac).toInt()
                    (maxPossibleHeld - held).coerceAtLeast(0)
                } else 0

                val classesNeeded = if (pct < subject.targetPercentage && targetFrac < 1.0) {
                    val needed = ceil((targetFrac * held - attended) / (1.0 - targetFrac)).toInt()
                    needed.coerceAtLeast(1)
                } else 0

                SubjectStats(
                    subject = subject,
                    totalHeld = held,
                    totalAttended = attended,
                    totalAbsent = absent,
                    totalCancelled = cancelled,
                    percentage = pct,
                    safeBunks = safeBunks,
                    classesNeeded = classesNeeded,
                    isEligible = pct >= subject.targetPercentage
                )
            }
        }

    val overallStats: Flow<OverallAttendanceStats> =
        combine(subjectsWithStats, allRecords) { subjectStats, _ ->
            val totalHeld = subjectStats.sumOf { it.totalHeld }
            val totalAttended = subjectStats.sumOf { it.totalAttended }
            val totalAbsent = subjectStats.sumOf { it.totalAbsent }
            val totalCancelled = subjectStats.sumOf { it.totalCancelled }

            val targetPct = 75.0
            val targetFrac = targetPct / 100.0
            val pct = if (totalHeld == 0) 100.0 else (totalAttended.toDouble() / totalHeld.toDouble()) * 100.0

            val safeBunks = if (totalHeld > 0 && pct >= targetPct && targetFrac > 0) {
                val maxPossibleHeld = floor(totalAttended / targetFrac).toInt()
                (maxPossibleHeld - totalHeld).coerceAtLeast(0)
            } else 0

            val classesNeeded = if (pct < targetPct && targetFrac < 1.0) {
                val needed = ceil((targetFrac * totalHeld - totalAttended) / (1.0 - targetFrac)).toInt()
                needed.coerceAtLeast(1)
            } else 0

            OverallAttendanceStats(
                totalHeld = totalHeld,
                totalAttended = totalAttended,
                totalAbsent = totalAbsent,
                totalCancelled = totalCancelled,
                percentage = pct,
                safeBunks = safeBunks,
                classesNeeded = classesNeeded,
                targetPercentage = targetPct,
                isEligible = pct >= targetPct
            )
        }
}
