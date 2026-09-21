package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassStudent
import com.example.data.CollegeRosterDirectory
import com.example.data.StudentAttendanceRecord
import com.example.data.TeacherClass
import com.example.ui.components.AddStudentDialog
import com.example.ui.components.ExportExcelDialog
import com.example.ui.components.WhatsAppShareDialog
import com.example.ui.theme.AttendanceAbsent
import com.example.ui.theme.AttendanceAbsentBg
import com.example.ui.theme.AttendanceCancelled
import com.example.ui.theme.AttendanceCancelledBg
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg
import com.example.util.ExcelExportUtil
import com.example.util.WhatsAppShareUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    teacherClass: TeacherClass,
    students: List<ClassStudent>,
    allRecords: List<StudentAttendanceRecord>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onBack: () -> Unit,
    onAddStudentSingle: (name: String, rollNo: String, email: String, initialStatus: String?) -> Unit,
    onAddStudentsBulk: (List<Pair<String, String>>) -> Unit,
    onDeleteStudent: (ClassStudent) -> Unit,
    onSaveAttendanceBatch: (Map<Long, String>) -> Unit,
    onExportExcel: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily Attendance, 1: Student Roster & Stats
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualAddInitialRoll by remember { mutableStateOf("") }
    var manualAddInitialName by remember { mutableStateOf("") }
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var showExportExcelDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<ClassStudent?>(null) }

    // Attendance map for selected date: studentId -> "PRESENT" / "ABSENT" / "LEAVE"
    val dailyRecords = remember(allRecords, selectedDate) {
        allRecords.filter { it.date == selectedDate }
    }

    // Local mutable state for current date attendance session
    val attendanceStatusMap = remember(dailyRecords, students, selectedDate) {
        mutableStateMapOf<Long, String>().apply {
            // First load from records if existing
            dailyRecords.forEach { rec ->
                put(rec.studentId, rec.status)
            }
        }
    }

    // Parse date for DatePickerDialog
    val calendar = remember(selectedDate) {
        Calendar.getInstance().apply {
            try {
                val parts = selectedDate.split("-")
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } catch (_: Exception) {}
        }
    }

    val classColor = try {
        Color(android.graphics.Color.parseColor(teacherClass.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("class_detail_screen")
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = teacherClass.className,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${teacherClass.courseCode.ifEmpty { "Paper" }} • ${teacherClass.semester} (Sec ${teacherClass.section})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Action buttons in header: WhatsApp Share & Download Excel
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showWhatsAppDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("whatsapp_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { showExportExcelDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_excel_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Take Daily Attendance (${students.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Students & Stats") }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // TAB 0: Daily Attendance Mode
            DailyAttendanceView(
                selectedDate = selectedDate,
                students = students,
                statusMap = attendanceStatusMap,
                onDatePickClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            onDateChange(newDate)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                onMarkAll = { status ->
                    students.forEach { s ->
                        attendanceStatusMap[s.id] = status
                    }
                },
                onStatusChange = { studentId, status ->
                    attendanceStatusMap[studentId] = status
                },
                onSave = {
                    onSaveAttendanceBatch(attendanceStatusMap.toMap())
                    Toast.makeText(context, "Attendance saved for $selectedDate!", Toast.LENGTH_SHORT).show()
                },
                onWhatsAppShareClick = { showWhatsAppDialog = true },
                onAddStudentClick = { showAddStudentDialog = true },
                onManualAddStudent = { name, roll, status ->
                    onAddStudentSingle(name, roll, "", status)
                    Toast.makeText(context, "Added $name ($roll) & marked $status!", Toast.LENGTH_SHORT).show()
                },
                onOpenManualAddDialog = { roll, name ->
                    manualAddInitialRoll = roll
                    manualAddInitialName = name
                    showManualAddDialog = true
                }
            )
        } else {
            // TAB 1: Student Roster & Register
            StudentRosterView(
                students = students,
                allRecords = allRecords,
                onAddStudentClick = { showAddStudentDialog = true },
                onDeleteStudentClick = { studentToDelete = it },
                onOpenManualAddDialog = { roll, name ->
                    manualAddInitialRoll = roll
                    manualAddInitialName = name
                    showManualAddDialog = true
                }
            )
        }
    }

    if (showManualAddDialog) {
        ManualAddStudentDialog(
            initialRoll = manualAddInitialRoll,
            initialName = manualAddInitialName,
            onDismiss = { showManualAddDialog = false },
            onConfirm = { name, roll, status ->
                onAddStudentSingle(name, roll, "", status)
                showManualAddDialog = false
                Toast.makeText(context, "Added $name ($roll) & marked $status!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            classId = teacherClass.id,
            onDismiss = { showAddStudentDialog = false },
            onAddSingle = { name, roll, email ->
                onAddStudentSingle(name, roll, email, null)
                showAddStudentDialog = false
                Toast.makeText(context, "Student added!", Toast.LENGTH_SHORT).show()
            },
            onAddBulk = { list ->
                onAddStudentsBulk(list)
                showAddStudentDialog = false
                Toast.makeText(context, "${list.size} students imported!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    studentToDelete?.let { stu ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Remove Student?") },
            text = { Text("Are you sure you want to remove ${stu.studentName} (${stu.rollNo}) from this class? All their attendance records will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(stu)
                        studentToDelete = null
                        Toast.makeText(context, "Student removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWhatsAppDialog) {
        WhatsAppShareDialog(
            teacherClass = teacherClass,
            selectedDate = selectedDate,
            students = students,
            statusMap = attendanceStatusMap,
            onDismiss = { showWhatsAppDialog = false }
        )
    }

    if (showExportExcelDialog) {
        ExportExcelDialog(
            teacherClass = teacherClass,
            attendanceRecords = allRecords,
            onDismiss = { showExportExcelDialog = false },
            onExport = { sDate, eDate, rangeLabel, shareToWhatsApp ->
                val csv = ExcelExportUtil.generateClassAttendanceCsv(
                    teacherClass = teacherClass,
                    students = students,
                    attendanceRecords = allRecords,
                    startDate = sDate,
                    endDate = eDate,
                    rangeLabel = rangeLabel
                )
                val suffix = when {
                    rangeLabel.isNotBlank() -> rangeLabel.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_-]"), "")
                    !sDate.isNullOrBlank() && !eDate.isNullOrBlank() -> "${sDate}_to_${eDate}"
                    else -> "All"
                }
                if (shareToWhatsApp) {
                    val uri = ExcelExportUtil.shareClassAttendanceToWhatsApp(
                        context = context,
                        teacherClass = teacherClass,
                        csvContent = csv,
                        fileSuffix = suffix,
                        rangeLabel = rangeLabel
                    )
                    if (uri == null) {
                        Toast.makeText(context, "Could not create Excel file for WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val uri = ExcelExportUtil.shareOrDownloadExcel(
                        context = context,
                        teacherClass = teacherClass,
                        csvContent = csv,
                        fileSuffix = suffix,
                        rangeLabel = rangeLabel
                    )
                    if (uri != null) {
                        Toast.makeText(context, "Opening Excel sheet: $rangeLabel", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun ManualAddStudentDialog(
    initialRoll: String = "",
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, roll: String, status: String) -> Unit
) {
    var roll by remember(initialRoll) { mutableStateOf(initialRoll) }
    var name by remember(initialName) { mutableStateOf(initialName) }
    var selectedStatus by remember { mutableStateOf(StudentAttendanceRecord.STATUS_PRESENT) }
    var rollError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text("Manually Type Student Details", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Student detail nahi mili? Yahan student ka Roll No aur Name type karke class me add karein aur turant attendance lagayein.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = roll,
                    onValueChange = {
                        roll = it
                        rollError = false
                    },
                    label = { Text("Roll Number *") },
                    placeholder = { Text("e.g. 260015, SLC/2026/01") },
                    singleLine = true,
                    isError = rollError,
                    supportingText = { if (rollError) Text("Roll number is required") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_dialog_roll_input")
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Student Name *") },
                    placeholder = { Text("e.g. Sarah Zia / Rohit Sharma") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = { if (nameError) Text("Student name is required") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_dialog_name_input")
                )

                Text(
                    "Attendance Status for Today:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        StudentAttendanceRecord.STATUS_PRESENT to "Present (P)",
                        StudentAttendanceRecord.STATUS_ABSENT to "Absent (A)",
                        StudentAttendanceRecord.STATUS_LEAVE to "Leave (L)"
                    ).forEach { (status, label) ->
                        val isSelected = selectedStatus == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatus = status },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roll.isBlank()) {
                        rollError = true
                        return@Button
                    }
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    onConfirm(name.trim(), roll.trim(), selectedStatus)
                },
                modifier = Modifier.testTag("confirm_manual_add_student")
            ) {
                Text("Add & Mark")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DailyAttendanceView(
    selectedDate: String,
    students: List<ClassStudent>,
    statusMap: Map<Long, String>,
    onDatePickClick: () -> Unit,
    onMarkAll: (String) -> Unit,
    onStatusChange: (Long, String) -> Unit,
    onSave: () -> Unit,
    onWhatsAppShareClick: () -> Unit,
    onAddStudentClick: () -> Unit,
    onManualAddStudent: (name: String, roll: String, status: String) -> Unit,
    onOpenManualAddDialog: (initialRoll: String, initialName: String) -> Unit
) {
    val presentCount = students.count { statusMap[it.id] == StudentAttendanceRecord.STATUS_PRESENT }
    val absentCount = students.count { statusMap[it.id] == StudentAttendanceRecord.STATUS_ABSENT }
    val leaveCount = students.count { statusMap[it.id] == StudentAttendanceRecord.STATUS_LEAVE }
    val unmarkedCount = students.size - (presentCount + absentCount + leaveCount)

    var searchQuery by remember { mutableStateOf("") }
    val trimmedQuery = searchQuery.trim()

    val filteredStudents = remember(students, trimmedQuery) {
        if (trimmedQuery.isEmpty()) {
            students
        } else {
            students.filter {
                it.studentName.contains(trimmedQuery, ignoreCase = true) ||
                it.rollNo.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_attendance_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Date Selector & Quick Mark Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Class Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { onDatePickClick() }
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDatePickClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Change Date")
                        }
                    }

                    // Stat Pills Row: Present, Absent, Unmarked
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CountBadge(label = "Present", count = presentCount, color = AttendancePresent, bgColor = AttendancePresentBg, modifier = Modifier.weight(1f))
                        CountBadge(label = "Absent", count = absentCount, color = AttendanceAbsent, bgColor = AttendanceAbsentBg, modifier = Modifier.weight(1f))
                        CountBadge(label = "Leave", count = leaveCount, color = AttendanceCancelled, bgColor = AttendanceCancelledBg, modifier = Modifier.weight(0.9f))
                        CountBadge(label = "Unmarked", count = unmarkedCount, color = MaterialTheme.colorScheme.onSurfaceVariant, bgColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.weight(1.1f))
                    }

                    // Fast Action Buttons: Mark All Present / Absent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onMarkAll(StudentAttendanceRecord.STATUS_PRESENT) },
                            colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("All Present (P)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onMarkAll(StudentAttendanceRecord.STATUS_ABSENT) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("All Absent (A)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // SEARCH & QUICK MANUAL ENTRY BAR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("attendance_student_search_input"),
                            placeholder = {
                                Text(
                                    "Search Name or Roll No...",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        FilledTonalButton(
                            onClick = {
                                val prefillRoll = if (trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                                val prefillName = if (!trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                                onOpenManualAddDialog(prefillRoll, prefillName)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("open_manual_add_dialog_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Type Manually", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (trimmedQuery.isNotEmpty() && filteredStudents.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Found ${filteredStudents.size} student(s) for \"$trimmedQuery\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Clear filter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        }

        // SMART "STUDENT NOT FOUND -> AUTO-MATCH FROM COLLEGE OR TYPE DETAILS MANUALLY" INLINE CARD
        if (trimmedQuery.isNotEmpty() && filteredStudents.isEmpty()) {
            item {
                val collegeMatch = remember(trimmedQuery) {
                    CollegeRosterDirectory.findStudentByRoll(trimmedQuery)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_not_found_inline_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (collegeMatch != null) AttendancePresentBg.copy(alpha = 0.4f)
                                         else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (collegeMatch != null) AttendancePresent.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (collegeMatch != null) {
                            // Instant Match from Shyam Lal College Roster!
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AttendancePresent
                                )
                                Column {
                                    Text(
                                        text = "Student Found in College Records!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AttendancePresent
                                    )
                                    Text(
                                        text = "${collegeMatch.rollNo} • ${collegeMatch.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Yeh student college list me enrolled hai. Ek tap me class me add karein aur attendance lagayein:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onManualAddStudent(collegeMatch.name, collegeMatch.rollNo, StudentAttendanceRecord.STATUS_PRESENT)
                                        searchQuery = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add as Present (P)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onManualAddStudent(collegeMatch.name, collegeMatch.rollNo, StudentAttendanceRecord.STATUS_ABSENT)
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Add as Absent (A)", fontSize = 12.sp, color = AttendanceAbsent, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Not found in College Roster -> Manual Entry
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Student \"$trimmedQuery\" Not in Class or College Roster",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Text(
                                text = "Student detail nahi mili? Yahan direct Roll No aur Name type karke class me add karein aur turant attendance lagayein:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            var inlineRoll by remember(trimmedQuery) {
                                mutableStateOf(if (trimmedQuery.any { it.isDigit() }) trimmedQuery else "")
                            }
                            var inlineName by remember(trimmedQuery) {
                                mutableStateOf(if (!trimmedQuery.any { it.isDigit() }) trimmedQuery else "")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = inlineRoll,
                                    onValueChange = { inlineRoll = it },
                                    label = { Text("Roll No *") },
                                    placeholder = { Text("e.g. 260015") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("inline_manual_roll_input")
                                )
                                OutlinedTextField(
                                    value = inlineName,
                                    onValueChange = { inlineName = it },
                                    label = { Text("Student Name *") },
                                    placeholder = { Text("e.g. Rahul Sharma") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("inline_manual_name_input")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (inlineRoll.isNotBlank() && inlineName.isNotBlank()) {
                                            onManualAddStudent(inlineName.trim(), inlineRoll.trim(), StudentAttendanceRecord.STATUS_PRESENT)
                                            searchQuery = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("inline_add_mark_present_btn")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add & Mark Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (inlineRoll.isNotBlank() && inlineName.isNotBlank()) {
                                            onManualAddStudent(inlineName.trim(), inlineRoll.trim(), StudentAttendanceRecord.STATUS_ABSENT)
                                            searchQuery = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("inline_add_mark_absent_btn")
                                ) {
                                    Text("Add & Mark Absent", fontSize = 12.sp, color = AttendanceAbsent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (students.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Students in this Class",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add students one by one, search, or type student details manually to start marking daily attendance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onAddStudentClick,
                                modifier = Modifier.testTag("add_first_student_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Import Students")
                            }
                            OutlinedButton(
                                onClick = { onOpenManualAddDialog("", "") },
                                modifier = Modifier.testTag("manual_type_first_student_button")
                            ) {
                                Text("Type Manually")
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trimmedQuery.isNotEmpty()) "Matching Students (${filteredStudents.size})" else "Students Roll Call (${students.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onWhatsAppShareClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("daily_whatsapp_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WhatsApp", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSave,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_attendance_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(filteredStudents, key = { it.id }) { student ->
                val currentStatus = statusMap[student.id]

                StudentDailyRow(
                    student = student,
                    status = currentStatus,
                    onStatusChange = { newStatus ->
                        onStatusChange(student.id, newStatus)
                    }
                )
            }

            // Bottom Save and WhatsApp Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onWhatsAppShareClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("bottom_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Share to WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("bottom_save_attendance_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Save ($selectedDate)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StudentDailyRow(
    student: ClassStudent,
    status: String?,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_row_${student.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Roll No: ${student.rollNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick P / A / L Selector Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Present Button
                val isP = status == StudentAttendanceRecord.STATUS_PRESENT
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStatusChange(StudentAttendanceRecord.STATUS_PRESENT) },
                    color = if (isP) AttendancePresent else AttendancePresentBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            fontWeight = FontWeight.Bold,
                            color = if (isP) Color.White else AttendancePresent
                        )
                    }
                }

                // Absent Button
                val isA = status == StudentAttendanceRecord.STATUS_ABSENT
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStatusChange(StudentAttendanceRecord.STATUS_ABSENT) },
                    color = if (isA) AttendanceAbsent else AttendanceAbsentBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            fontWeight = FontWeight.Bold,
                            color = if (isA) Color.White else AttendanceAbsent
                        )
                    }
                }

                // Leave Button
                val isL = status == StudentAttendanceRecord.STATUS_LEAVE
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStatusChange(StudentAttendanceRecord.STATUS_LEAVE) },
                    color = if (isL) AttendanceCancelled else AttendanceCancelledBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L",
                            fontWeight = FontWeight.Bold,
                            color = if (isL) Color.White else AttendanceCancelled
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentRosterView(
    students: List<ClassStudent>,
    allRecords: List<StudentAttendanceRecord>,
    onAddStudentClick: () -> Unit,
    onDeleteStudentClick: (ClassStudent) -> Unit,
    onOpenManualAddDialog: (initialRoll: String, initialName: String) -> Unit
) {
    // Unique classes conducted
    val totalDates = allRecords.map { it.date }.distinct().size

    var searchQuery by remember { mutableStateOf("") }
    val trimmedQuery = searchQuery.trim()

    val filteredStudents = remember(students, trimmedQuery) {
        if (trimmedQuery.isEmpty()) {
            students
        } else {
            students.filter {
                it.studentName.contains(trimmedQuery, ignoreCase = true) ||
                it.rollNo.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_roster_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Enrolled Students (${students.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total classes held so far: $totalDates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val prefillRoll = if (trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                            val prefillName = if (!trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                            onOpenManualAddDialog(prefillRoll, prefillName)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Type Manually", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddStudentClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }
        }

        // Search Bar in Roster
        if (students.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("roster_student_search_input"),
                    placeholder = {
                        Text(
                            "Search Name or Roll No...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (trimmedQuery.isNotEmpty() && filteredStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No student found matching \"$trimmedQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Detail nahi mili? Direct type karke add karein:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                val prefillRoll = if (trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                                val prefillName = if (!trimmedQuery.any { it.isDigit() }) trimmedQuery else ""
                                onOpenManualAddDialog(prefillRoll, prefillName)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Type \"$trimmedQuery\" Details")
                        }
                    }
                }
            }
        }

        if (students.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "No students added yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Click 'Add Students' to import lists or 'Type Manually' to enter student details directly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredStudents, key = { it.id }) { student ->
                val studentRecords = allRecords.filter { it.studentId == student.id }
                val attended = studentRecords.count { it.status == StudentAttendanceRecord.STATUS_PRESENT }
                val totalSessions = studentRecords.size
                val percentage = if (totalSessions > 0) {
                    (attended.toDouble() / totalSessions.toDouble()) * 100.0
                } else 0.0

                val isEligible = percentage >= 75.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.studentName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Roll: ${student.rollNo}${if (student.email.isNotEmpty()) " • ${student.email}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attended: $attended / $totalSessions sessions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isEligible) AttendancePresentBg else AttendanceAbsentBg
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", percentage)}%",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEligible) AttendancePresent else AttendanceAbsent
                                )
                            }

                            IconButton(
                                onClick = { onDeleteStudentClick(student) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Student",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountBadge(label: String, count: Int, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
