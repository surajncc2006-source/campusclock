package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.ClassStudent
import com.example.data.CollegeStudentItem
import com.example.data.SectionSheet
import com.example.data.StudentAttendanceRecord
import com.example.data.TeacherClass
import com.example.ui.DirectStudentEntry
import java.text.SimpleDateFormat
import java.util.Locale

enum class ShareFormatMode(val displayName: String, val description: String) {
    FULL_REPORT("Full Report", "Summary + Absentees + Present list with Roll & Name"),
    ABSENT_ONLY("Absentees Only", "Summary + Absent students list with Roll & Name"),
    ROLL_CALL_ORDER("Roll Call Order", "Summary + Complete roster ordered by Roll No")
}

object WhatsAppShareUtil {

    /**
     * Formats date string from yyyy-MM-dd to a readable format like "12-Sep-2026 (Saturday)"
     */
    private fun formatReadableDate(rawDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("dd-MMM-yyyy (EEE)", Locale.US)
            val d = parser.parse(rawDate)
            if (d != null) formatter.format(d) else rawDate
        } catch (_: Exception) {
            rawDate
        }
    }

    /**
     * Builds a clean, organized WhatsApp text report of daily attendance
     * containing Class Name, Date, Student Name, Roll No., and Attendance status.
     */
    fun buildDailyAttendanceSummary(
        teacherClass: TeacherClass,
        selectedDate: String,
        students: List<ClassStudent>,
        statusMap: Map<Long, String>,
        shareMode: ShareFormatMode = ShareFormatMode.FULL_REPORT
    ): String {
        // Sort students naturally by Roll Number
        val sortedStudents = students.sortedWith(compareBy({ it.rollNo.length }, { it.rollNo }))

        val presentStudents = sortedStudents.filter { statusMap[it.id] == StudentAttendanceRecord.STATUS_PRESENT }
        val absentStudents = sortedStudents.filter { statusMap[it.id] == StudentAttendanceRecord.STATUS_ABSENT }
        val leaveStudents = sortedStudents.filter { statusMap[it.id] == StudentAttendanceRecord.STATUS_LEAVE }
        val unmarkedStudents = sortedStudents.filter {
            val s = statusMap[it.id]
            s == null || (s != StudentAttendanceRecord.STATUS_PRESENT && s != StudentAttendanceRecord.STATUS_ABSENT && s != StudentAttendanceRecord.STATUS_LEAVE)
        }

        val totalMarked = presentStudents.size + absentStudents.size + leaveStudents.size
        val presentPercent = if (totalMarked > 0) {
            (presentStudents.size.toDouble() / totalMarked.toDouble()) * 100.0
        } else 0.0
        val absentPercent = if (totalMarked > 0) {
            (absentStudents.size.toDouble() / totalMarked.toDouble()) * 100.0
        } else 0.0

        val formattedDate = formatReadableDate(selectedDate)

        val sb = StringBuilder()
        sb.append("🏛️ *SHYAM LAL COLLEGE (UNIVERSITY OF DELHI)*\n")
        sb.append("📋 *DAILY CLASS ATTENDANCE REPORT*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📚 *Class:* ${teacherClass.className}\n")
        if (teacherClass.courseCode.isNotBlank()) {
            sb.append("🔖 *Paper Code:* ${teacherClass.courseCode}\n")
        }
        sb.append("👥 *Section:* ${teacherClass.section} | *Semester:* ${teacherClass.semester}\n")
        if (teacherClass.department.isNotBlank()) {
            sb.append("🏛️ *Department:* ${teacherClass.department}\n")
        }
        sb.append("📅 *Date:* $formattedDate ($selectedDate)\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 *ATTENDANCE SUMMARY:*\n")
        sb.append("• Total Strength: ${students.size} Students\n")
        sb.append("✅ *Present:* ${presentStudents.size} (${String.format(Locale.US, "%.1f", presentPercent)}%)\n")
        sb.append("❌ *Absent:* ${absentStudents.size} (${String.format(Locale.US, "%.1f", absentPercent)}%)\n")
        if (leaveStudents.isNotEmpty()) {
            sb.append("⏳ *Leave / Duty:* ${leaveStudents.size}\n")
        }
        if (unmarkedStudents.isNotEmpty()) {
            sb.append("❓ *Unmarked:* ${unmarkedStudents.size}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        when (shareMode) {
            ShareFormatMode.FULL_REPORT -> {
                // 1. Absent List with Roll and Name
                if (absentStudents.isNotEmpty()) {
                    sb.append("❌ *ABSENT STUDENTS (${absentStudents.size}):*\n")
                    absentStudents.forEachIndexed { index, stu ->
                        sb.append("${index + 1}. *${stu.rollNo}* — ${stu.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                } else if (students.isNotEmpty() && unmarkedStudents.isEmpty()) {
                    sb.append("🎉 *100% Attendance! All students were present today.*\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                // 2. Present List with Roll and Name
                if (presentStudents.isNotEmpty()) {
                    sb.append("✅ *PRESENT STUDENTS (${presentStudents.size}):*\n")
                    presentStudents.forEachIndexed { index, stu ->
                        sb.append("${index + 1}. *${stu.rollNo}* — ${stu.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                // 3. Leave List with Roll and Name
                if (leaveStudents.isNotEmpty()) {
                    sb.append("🟡 *ON LEAVE / DUTY (${leaveStudents.size}):*\n")
                    leaveStudents.forEachIndexed { index, stu ->
                        sb.append("${index + 1}. *${stu.rollNo}* — ${stu.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            ShareFormatMode.ABSENT_ONLY -> {
                if (absentStudents.isNotEmpty()) {
                    sb.append("❌ *ABSENT STUDENTS LIST (${absentStudents.size}):*\n")
                    absentStudents.forEachIndexed { index, stu ->
                        sb.append("${index + 1}. *${stu.rollNo}* — ${stu.studentName}\n")
                    }
                } else {
                    sb.append("🎉 *All students were present today. Zero absentees!*\n")
                }
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            }

            ShareFormatMode.ROLL_CALL_ORDER -> {
                sb.append("📋 *STUDENT ATTENDANCE ROSTER:*\n")
                sortedStudents.forEachIndexed { index, stu ->
                    val status = when (statusMap[stu.id]) {
                        StudentAttendanceRecord.STATUS_PRESENT -> "✅ Present"
                        StudentAttendanceRecord.STATUS_ABSENT -> "❌ Absent"
                        StudentAttendanceRecord.STATUS_LEAVE -> "🟡 Leave"
                        else -> "❓ Unmarked"
                    }
                    sb.append("${index + 1}. *${stu.rollNo}* — ${stu.studentName} [ $status ]\n")
                }
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            }
        }

        sb.append("📲 _Shared via CampusClock SLC Attendance Portal (100% Offline)_")
        return sb.toString()
    }

    /**
     * Builds a clean WhatsApp text report for Direct / Instant Attendance.
     */
    fun buildDirectAttendanceSummary(
        lectureTitle: String,
        date: String,
        students: List<DirectStudentEntry>,
        takenBy: String = "Faculty / CR",
        shareMode: ShareFormatMode = ShareFormatMode.FULL_REPORT
    ): String {
        val sortedStudents = students.sortedWith(compareBy({ it.rollNo.length }, { it.rollNo }))
        val presentStudents = sortedStudents.filter { it.status == StudentAttendanceRecord.STATUS_PRESENT }
        val absentStudents = sortedStudents.filter { it.status == StudentAttendanceRecord.STATUS_ABSENT }
        val leaveStudents = sortedStudents.filter { it.status == StudentAttendanceRecord.STATUS_LEAVE }

        val pct = if (students.isNotEmpty()) (presentStudents.size * 100.0 / students.size) else 0.0
        val formattedDate = formatReadableDate(date)

        val sb = StringBuilder()
        sb.append("🏛️ *SHYAM LAL COLLEGE (UNIVERSITY OF DELHI)*\n")
        sb.append("📋 *LECTURE ATTENDANCE REPORT*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📖 *Lecture / Subject:* ${lectureTitle.ifBlank { "Direct Roll Call" }}\n")
        sb.append("📅 *Date:* $formattedDate\n")
        sb.append("👤 *Taken By:* $takenBy\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📊 *ATTENDANCE SUMMARY:*\n")
        sb.append("• Total Strength: ${students.size} Students\n")
        sb.append("✅ *Present:* ${presentStudents.size} (${String.format(Locale.US, "%.1f", pct)}%)\n")
        sb.append("❌ *Absent:* ${absentStudents.size}\n")
        if (leaveStudents.isNotEmpty()) {
            sb.append("🟡 *Leave / Duty:* ${leaveStudents.size}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

        when (shareMode) {
            ShareFormatMode.FULL_REPORT -> {
                if (absentStudents.isNotEmpty()) {
                    sb.append("❌ *ABSENT STUDENTS (${absentStudents.size}):*\n")
                    absentStudents.forEachIndexed { index, s ->
                        sb.append("${index + 1}. *${s.rollNo}* — ${s.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                } else if (students.isNotEmpty()) {
                    sb.append("🎉 *100% Attendance! All students present.*\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                if (presentStudents.isNotEmpty()) {
                    sb.append("✅ *PRESENT STUDENTS (${presentStudents.size}):*\n")
                    presentStudents.forEachIndexed { index, s ->
                        sb.append("${index + 1}. *${s.rollNo}* — ${s.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }

                if (leaveStudents.isNotEmpty()) {
                    sb.append("🟡 *ON LEAVE (${leaveStudents.size}):*\n")
                    leaveStudents.forEachIndexed { index, s ->
                        sb.append("${index + 1}. *${s.rollNo}* — ${s.studentName}\n")
                    }
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                }
            }

            ShareFormatMode.ABSENT_ONLY -> {
                if (absentStudents.isNotEmpty()) {
                    sb.append("❌ *ABSENT STUDENTS LIST (${absentStudents.size}):*\n")
                    absentStudents.forEachIndexed { index, s ->
                        sb.append("${index + 1}. *${s.rollNo}* — ${s.studentName}\n")
                    }
                } else {
                    sb.append("🎉 *All students present. Zero absentees!*\n")
                }
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            }

            ShareFormatMode.ROLL_CALL_ORDER -> {
                sb.append("📋 *STUDENT ATTENDANCE ROSTER:*\n")
                sortedStudents.forEachIndexed { index, s ->
                    val status = when (s.status) {
                        StudentAttendanceRecord.STATUS_PRESENT -> "✅ Present"
                        StudentAttendanceRecord.STATUS_ABSENT -> "❌ Absent"
                        StudentAttendanceRecord.STATUS_LEAVE -> "🟡 Leave"
                        else -> "❓ Unmarked"
                    }
                    sb.append("${index + 1}. *${s.rollNo}* — ${s.studentName} [ $status ]\n")
                }
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            }
        }

        sb.append("📲 _Shared via CampusClock SLC Attendance Portal (100% Offline)_")
        return sb.toString()
    }

    /**
     * Builds a formatted WhatsApp message for a section roster sheet.
     */
    fun buildSectionRosterSummary(
        sheet: SectionSheet
    ): String {
        val sb = StringBuilder()
        sb.append("🏛️ *SHYAM LAL COLLEGE (UNIVERSITY OF DELHI)*\n")
        sb.append("📋 *STUDENT ROSTER LIST (2026-2027)*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📚 *Program:* ${sheet.courseTitle} (${sheet.courseCode})\n")
        sb.append("🎓 *Year:* ${sheet.year} (${sheet.academicYear})\n")
        sb.append("👥 *Section:* ${sheet.section}\n")
        sb.append("🔢 *Total Students:* ${sheet.students.size}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Roll Range: ${sheet.students.firstOrNull()?.rollNo ?: "N/A"} to ${sheet.students.lastOrNull()?.rollNo ?: "N/A"}\n")
        sb.append("📲 _Shared via CampusClock SLC Attendance Portal_")
        return sb.toString()
    }

    /**
     * Sends formatted text directly to WhatsApp or WhatsApp Business,
     * falling back to the system share sheet if neither is installed.
     */
    fun shareTextViaWhatsApp(context: Context, text: String) {
        try {
            // First attempt: Standard WhatsApp
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                // Second attempt: WhatsApp Business
                val intentBiz = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    setPackage("com.whatsapp.w4b")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intentBiz)
            } catch (_: Exception) {
                // Third attempt: Standard Android Share sheet
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                val chooser = Intent.createChooser(generalIntent, "Share Attendance to WhatsApp / Apps")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        }
    }

    /**
     * Shares an Excel (.csv) file directly via WhatsApp or WhatsApp Business,
     * falling back to the standard share chooser.
     */
    fun shareFileViaWhatsApp(context: Context, fileUri: Uri, caption: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intentBiz = Intent(Intent.ACTION_SEND).apply {
                    type = "text/comma-separated-values"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    if (caption.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, caption)
                    }
                    setPackage("com.whatsapp.w4b")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intentBiz)
            } catch (_: Exception) {
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/comma-separated-values"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    if (caption.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, caption)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(generalIntent, "Send Excel via WhatsApp / Apps")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        }
    }

    /**
     * Shares a student's official section roster details via WhatsApp.
     */
    fun shareStudentRosterDetails(
        context: Context,
        studentName: String,
        rollNo: String,
        courseTitle: String,
        year: String,
        section: String,
        sNo: Int
    ) {
        val message = """
            🏛️ *SHYAM LAL COLLEGE (University of Delhi)*
            📋 *Student Roster Verification*
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            👤 *Student:* $studentName
            🔢 *Roll No:* $rollNo
            🎓 *Course:* $courseTitle
            📅 *Academic Year:* $year (2026-2027)
            🏷️ *Section:* $section (S.No. #$sNo)
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            _Shared from CampusClock Section Sheets_
        """.trimIndent()
        shareTextViaWhatsApp(context, message)
    }
}
