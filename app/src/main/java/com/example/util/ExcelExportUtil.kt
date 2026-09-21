package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.ClassStudent
import com.example.data.StudentAttendanceRecord
import com.example.data.TeacherClass
import com.example.ui.DirectStudentEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class YearMonthOption(
    val label: String,
    val yearMonth: String, // "yyyy-MM"
    val startDate: String, // "yyyy-MM-01"
    val endDate: String,   // "yyyy-MM-lastDay"
    val sessionCount: Int
)

object ExcelExportUtil {

    /**
     * Escapes a value for RFC 4180 CSV compliance (double quotes around text with commas or quotes).
     */
    private fun escapeCsv(value: String): String {
        var str = value.replace("\r", "").replace("\n", " ")
        if (str.contains(",") || str.contains("\"")) {
            str = "\"" + str.replace("\"", "\"\"") + "\""
        }
        return str
    }

    /**
     * Extracts all unique months from attendance records, plus includes current and previous month,
     * sorted descending (most recent first).
     */
    fun extractAvailableMonths(records: List<StudentAttendanceRecord>): List<YearMonthOption> {
        val cal = Calendar.getInstance()
        val ymFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val foundYearMonths = mutableSetOf<String>()

        // Always include current month
        foundYearMonths.add(ymFormat.format(cal.time))

        // Also include previous month
        val prevCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        foundYearMonths.add(ymFormat.format(prevCal.time))

        // Also add from all records
        records.forEach { r ->
            if (r.date.length >= 7) {
                foundYearMonths.add(r.date.take(7))
            }
        }

        return foundYearMonths.sortedDescending().map { ym ->
            val parts = ym.split("-")
            val year = parts[0].toIntOrNull() ?: 2026
            val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1

            val mCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val label = monthNameFormat.format(mCal.time)
            val startDate = String.format(Locale.US, "%04d-%02d-01", year, month + 1)
            val maxDay = mCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, maxDay)

            val sessionsInMonth = records.filter { it.date.startsWith(ym) }.map { it.date }.distinct().size

            YearMonthOption(
                label = label,
                yearMonth = ym,
                startDate = startDate,
                endDate = endDate,
                sessionCount = sessionsInMonth
            )
        }
    }

    /**
     * Generates a comprehensive CSV sheet compatible with Microsoft Excel, Google Sheets,
     * LibreOffice Calc, and WPS Office.
     * Supports:
     * - Entire Month export
     * - Custom Date Range export (startDate to endDate)
     * - All sessions (default when startDate/endDate are null)
     *
     * Contains Class summary, dates as horizontal columns, student roll numbers, names,
     * daily attendance status (P/A/L), Total Attended, Total Sessions, and Attendance Percentage.
     */
    fun generateClassAttendanceCsv(
        teacherClass: TeacherClass,
        students: List<ClassStudent>,
        attendanceRecords: List<StudentAttendanceRecord>,
        startDate: String? = null,
        endDate: String? = null,
        rangeLabel: String? = null
    ): String {
        val sb = StringBuilder()

        // BOM for Excel UTF-8 recognition (handles Indian names & special characters properly in MS Excel)
        sb.append("\uFEFF")

        // Filter records by date range if provided
        val filteredRecords = attendanceRecords.filter { record ->
            val afterStart = startDate.isNullOrBlank() || record.date >= startDate
            val beforeEnd = endDate.isNullOrBlank() || record.date <= endDate
            afterStart && beforeEnd
        }

        // Unique sorted dates on which attendance was taken in this date range
        val distinctDates = filteredRecords.map { it.date }.distinct().sorted()

        // Build effective period label
        val effectiveRange = when {
            !rangeLabel.isNullOrBlank() -> rangeLabel
            !startDate.isNullOrBlank() && !endDate.isNullOrBlank() -> "$startDate to $endDate"
            !startDate.isNullOrBlank() -> "From $startDate onwards"
            !endDate.isNullOrBlank() -> "Up to $endDate"
            distinctDates.isNotEmpty() -> "${distinctDates.first()} to ${distinctDates.last()} (All Recorded Sessions)"
            else -> "Complete Academic Term"
        }

        // Metadata Header
        sb.append("ATTENDANCE REGISTER - SHYAM LAL COLLEGE (UNIVERSITY OF DELHI)\n")
        sb.append("Class / Subject,").append(escapeCsv(teacherClass.className)).append("\n")
        sb.append("Course / Paper Code,").append(escapeCsv(teacherClass.courseCode.ifBlank { "N/A" })).append("\n")
        sb.append("Department,").append(escapeCsv(teacherClass.department.ifBlank { "General" })).append("\n")
        sb.append("Semester / Section,").append(escapeCsv("${teacherClass.semester} - Section ${teacherClass.section}")).append("\n")
        sb.append("Attendance Period / Date Range,").append(escapeCsv(effectiveRange)).append("\n")
        sb.append("Total Sessions Held in Period,").append(distinctDates.size).append("\n")
        sb.append("Total Enrolled Students,").append(students.size).append("\n")
        val exportDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(Date())
        sb.append("Generated On,").append(escapeCsv(exportDate)).append("\n\n")

        // Mapping: (studentId to (date to status))
        val studentDateStatusMap = mutableMapOf<Pair<Long, String>, String>()
        filteredRecords.forEach { record ->
            studentDateStatusMap[Pair(record.studentId, record.date)] = when (record.status) {
                StudentAttendanceRecord.STATUS_PRESENT -> "P"
                StudentAttendanceRecord.STATUS_ABSENT -> "A"
                StudentAttendanceRecord.STATUS_LEAVE -> "L"
                else -> "-"
            }
        }

        // Table Header Row:
        // S.No., Roll No., Student Name, [Dates...], Total Present, Total Absent, Total Sessions, Attendance %, Eligibility Status
        val headers = mutableListOf("S.No.", "Roll No.", "Student Name")
        distinctDates.forEach { date ->
            headers.add(date)
        }
        headers.add("Total Present")
        headers.add("Total Absent")
        headers.add("Total Sessions")
        headers.add("Attendance %")
        headers.add("Eligibility Status (>=75%)")

        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        // Sort students naturally by Roll Number
        val sortedStudents = students.sortedWith(compareBy({ it.rollNo.length }, { it.rollNo }))

        // Student Data Rows
        sortedStudents.forEachIndexed { index, student ->
            var presentCount = 0
            var absentCount = 0
            var totalCount = 0

            val row = mutableListOf<String>()
            row.add((index + 1).toString())
            row.add(student.rollNo)
            row.add(student.studentName)

            distinctDates.forEach { date ->
                val status = studentDateStatusMap[Pair(student.id, date)] ?: "-"
                if (status == "P") {
                    presentCount++
                    totalCount++
                } else if (status == "A") {
                    absentCount++
                    totalCount++
                } else if (status == "L") {
                    totalCount++
                }
                row.add(status)
            }

            val percentage = if (totalCount > 0) {
                (presentCount.toDouble() / totalCount.toDouble()) * 100.0
            } else {
                0.0
            }

            row.add(presentCount.toString())
            row.add(absentCount.toString())
            row.add(totalCount.toString())
            row.add(String.format(Locale.US, "%.1f%%", percentage))
            row.add(if (percentage >= 75.0) "ELIGIBLE" else "SHORTAGE (<75%)")

            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }

        // Bottom Summary Statistics
        sb.append("\nSummary Statistics\n")
        sb.append("Total Students,").append(students.size).append("\n")
        sb.append("Total Classes Conducted in Range,").append(distinctDates.size).append("\n")
        sb.append("Date Range Evaluated,").append(escapeCsv(effectiveRange)).append("\n")
        sb.append("Generated by,CampusClock Teacher Portal - 100% Offline\n")

        return sb.toString()
    }

    /**
     * Generates a single-session CSV sheet for Direct / Instant Attendance.
     */
    fun generateDirectAttendanceCsv(
        lectureTitle: String,
        date: String,
        students: List<DirectStudentEntry>,
        teacherName: String = ""
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        val cleanTitle = lectureTitle.ifBlank { "Direct Lecture" }
        sb.append("ATTENDANCE REGISTER - SHYAM LAL COLLEGE (UNIVERSITY OF DELHI)\n")
        sb.append("Lecture / Subject,").append(escapeCsv(cleanTitle)).append("\n")
        sb.append("Date,").append(escapeCsv(date)).append("\n")
        if (teacherName.isNotBlank()) {
            sb.append("Conducted By,").append(escapeCsv(teacherName)).append("\n")
        }
        val exportDate = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.US).format(Date())
        sb.append("Generated On,").append(escapeCsv(exportDate)).append("\n\n")

        val sorted = students.sortedWith(compareBy({ it.rollNo.length }, { it.rollNo }))
        val presentCount = sorted.count { it.status == StudentAttendanceRecord.STATUS_PRESENT }
        val absentCount = sorted.count { it.status == StudentAttendanceRecord.STATUS_ABSENT }
        val leaveCount = sorted.count { it.status == StudentAttendanceRecord.STATUS_LEAVE }

        sb.append("S.No.,Roll No.,Student Name,Attendance Status\n")
        sorted.forEachIndexed { index, s ->
            val statusDisplay = when (s.status) {
                StudentAttendanceRecord.STATUS_PRESENT -> "PRESENT (P)"
                StudentAttendanceRecord.STATUS_ABSENT -> "ABSENT (A)"
                StudentAttendanceRecord.STATUS_LEAVE -> "LEAVE (L)"
                else -> "-"
            }
            sb.append("${index + 1},${escapeCsv(s.rollNo)},${escapeCsv(s.studentName)},${escapeCsv(statusDisplay)}\n")
        }

        sb.append("\nSummary Statistics\n")
        sb.append("Total Students,").append(sorted.size).append("\n")
        sb.append("Total Present,").append(presentCount).append("\n")
        sb.append("Total Absent,").append(absentCount).append("\n")
        if (leaveCount > 0) {
            sb.append("Total On Leave,").append(leaveCount).append("\n")
        }
        val pct = if (sorted.isNotEmpty()) (presentCount.toDouble() / sorted.size.toDouble()) * 100.0 else 0.0
        sb.append("Attendance Percentage,").append(String.format(Locale.US, "%.1f%%", pct)).append("\n")
        sb.append("Generated by,CampusClock SLC Attendance Portal\n")

        return sb.toString()
    }

    /**
     * Saves CSV content to cache and returns its FileProvider Uri.
     */
    fun createExcelFileUri(
        context: Context,
        prefixName: String,
        csvContent: String,
        fileSuffix: String = ""
    ): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val sanitizedName = prefixName
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .take(25)
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val suffix = if (fileSuffix.isNotBlank()) "_$fileSuffix" else ""
            val file = File(exportDir, "Attendance_${sanitizedName}${suffix}_$dateStamp.csv")

            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves the CSV string to cache directory and opens Android system share sheet
     * to open in Excel / Sheets or send via WhatsApp / Drive / Gmail.
     */
    fun shareOrDownloadExcel(
        context: Context,
        teacherClass: TeacherClass,
        csvContent: String,
        fileSuffix: String = "",
        rangeLabel: String = ""
    ): Uri? {
        val uri = createExcelFileUri(context, teacherClass.className, csvContent, fileSuffix) ?: return null
        try {
            val subjectPeriod = if (rangeLabel.isNotBlank()) " ($rangeLabel)" else ""
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Attendance Register: ${teacherClass.className}$subjectPeriod")
                putExtra(Intent.EXTRA_TEXT, "Attached is the attendance sheet for ${teacherClass.className}$subjectPeriod, Shyam Lal College (DU).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Open or Download Attendance Sheet")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Shares the attendance Excel CSV file directly to WhatsApp.
     */
    fun shareClassAttendanceToWhatsApp(
        context: Context,
        teacherClass: TeacherClass,
        csvContent: String,
        fileSuffix: String = "",
        rangeLabel: String = ""
    ): Uri? {
        val uri = createExcelFileUri(context, teacherClass.className, csvContent, fileSuffix) ?: return null
        val periodText = if (rangeLabel.isNotBlank()) " [$rangeLabel]" else ""
        val caption = "📊 Attendance Register for ${teacherClass.className}$periodText (${teacherClass.semester}, Sec ${teacherClass.section}) - Shyam Lal College (DU)"
        WhatsAppShareUtil.shareFileViaWhatsApp(context, uri, caption)
        return uri
    }

    /**
     * Shares or downloads Direct Attendance Excel CSV.
     */
    fun shareOrDownloadDirectExcel(
        context: Context,
        lectureTitle: String,
        csvContent: String,
        date: String
    ): Uri? {
        val uri = createExcelFileUri(context, lectureTitle, csvContent, date) ?: return null
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Attendance Sheet: $lectureTitle ($date)")
                putExtra(Intent.EXTRA_TEXT, "Attached is the direct attendance sheet for $lectureTitle ($date), Shyam Lal College (DU).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Open or Download Attendance Sheet")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return uri
    }
}
