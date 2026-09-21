package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CollegeRosterDirectory
import com.example.data.CollegeStudentItem
import com.example.data.StudentAttendanceRecord
import com.example.ui.DirectStudentEntry
import com.example.ui.theme.AttendanceAbsent
import com.example.ui.theme.AttendanceAbsentBg
import com.example.ui.theme.AttendanceLeave
import com.example.ui.theme.AttendanceLeaveBg
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DirectAttendanceScreen(
    directStudents: List<DirectStudentEntry>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onAddStudentByRoll: (rollNo: String, manualName: String?, initialStatus: String) -> Boolean,
    onAddStudentItem: (CollegeStudentItem) -> Unit,
    onUpdateStatus: (rollNo: String, status: String) -> Unit,
    onRemoveStudent: (rollNo: String) -> Unit,
    onMarkAll: (status: String) -> Unit,
    onClearAll: () -> Unit,
    onSaveToDatabase: (context: Context, lectureTitle: String, date: String, onSaved: (Boolean) -> Unit) -> Unit,
    onShareWhatsApp: (context: Context, lectureTitle: String, date: String) -> Unit,
    onExportExcel: (context: Context, lectureTitle: String, date: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current

    var lectureTitle by remember { mutableStateOf("Direct Lecture") }
    var rollQuery by remember { mutableStateOf("") }
    var manualStudentName by remember { mutableStateOf("") }
    var showBulkDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.US) }

    val parsedDate = try {
        dateFormat.parse(selectedDate) ?: Date()
    } catch (_: Exception) {
        Date()
    }

    val isToday = dateFormat.format(Date()) == selectedDate

    val trimmedQuery = rollQuery.trim().uppercase()

    // College search suggestions
    val suggestions = remember(trimmedQuery) {
        if (trimmedQuery.length >= 2) {
            CollegeRosterDirectory.searchCollegeStudents(trimmedQuery, limit = 6)
        } else {
            emptyList()
        }
    }

    // Direct match check in official directory
    val exactDirectoryMatch = remember(trimmedQuery) {
        if (trimmedQuery.isNotEmpty()) {
            CollegeRosterDirectory.findStudentByRoll(trimmedQuery)
        } else null
    }

    // Stats calculation
    val totalCount = directStudents.size
    val presentCount = directStudents.count { it.status == StudentAttendanceRecord.STATUS_PRESENT }
    val absentCount = directStudents.count { it.status == StudentAttendanceRecord.STATUS_ABSENT }
    val leaveCount = directStudents.count { it.status == StudentAttendanceRecord.STATUS_LEAVE }
    val attendancePct = if (totalCount > 0) (presentCount * 100.0 / totalCount) else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("direct_attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner: Direct Roll Call (No Class Required)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Direct Attendance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class banaye bina direct student roll number search karein aur attendance lagayein",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Date Switcher Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { time = parsedDate }
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    onDateChange(dateFormat.format(cal.time))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = displayFormat.format(parsedDate),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isToday) {
                                    Text(
                                        text = "Today (Live)",
                                        fontSize = 11.sp,
                                        color = AttendancePresent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { time = parsedDate }
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                    onDateChange(dateFormat.format(cal.time))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                            }
                        }
                    }

                    // Lecture Title / Topic Field
                    OutlinedTextField(
                        value = lectureTitle,
                        onValueChange = { lectureTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_lecture_title_input"),
                        label = { Text("Lecture / Subject / Room Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 2. Roll Number Search & Auto-Add Card ("vo bachhe khud hi add ho jayege")
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search Roll Number to Add",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        FilledTonalButton(
                            onClick = { showBulkDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Bulk Rolls", fontSize = 11.sp)
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = rollQuery,
                        onValueChange = {
                            rollQuery = it
                            manualStudentName = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_roll_search_input"),
                        placeholder = { Text("Enter Roll No (e.g. 260015, 240402)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (rollQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    rollQuery = ""
                                    manualStudentName = ""
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (trimmedQuery.isNotEmpty()) {
                                    val added = onAddStudentByRoll(trimmedQuery, manualStudentName.ifBlank { null }, StudentAttendanceRecord.STATUS_PRESENT)
                                    if (added) {
                                        Toast.makeText(context, "$trimmedQuery added as Present!", Toast.LENGTH_SHORT).show()
                                        rollQuery = ""
                                        manualStudentName = ""
                                    }
                                }
                            }
                        )
                    )

                    // Match found in Official College Directory (Auto-Add ready!)
                    if (exactDirectoryMatch != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = AttendancePresentBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AttendancePresent.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AttendancePresent, modifier = Modifier.size(16.dp))
                                        Text("Auto-matched from College Roster:", fontSize = 11.sp, color = AttendancePresent, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "${exactDirectoryMatch.rollNo} • ${exactDirectoryMatch.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        onAddStudentItem(exactDirectoryMatch)
                                        Toast.makeText(context, "${exactDirectoryMatch.name} marked Present!", Toast.LENGTH_SHORT).show()
                                        rollQuery = ""
                                        manualStudentName = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add (P)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (trimmedQuery.isNotEmpty() && suggestions.isEmpty()) {
                        // Roll number NOT found in directory -> Manual Entry required ("ager roll number na mile to manually add karna hoga")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Roll No \"$trimmedQuery\" not found in college records.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Enter student's name manually to add:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = manualStudentName,
                                    onValueChange = { manualStudentName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Student Full Name (e.g. Rahul Sharma)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (manualStudentName.trim().isNotEmpty()) {
                                                onAddStudentByRoll(trimmedQuery, manualStudentName.trim(), StudentAttendanceRecord.STATUS_PRESENT)
                                                Toast.makeText(context, "${manualStudentName.trim()} added as Present!", Toast.LENGTH_SHORT).show()
                                                rollQuery = ""
                                                manualStudentName = ""
                                            } else {
                                                Toast.makeText(context, "Please enter student name", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent)
                                    ) {
                                        Text("Add Present (P)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (manualStudentName.trim().isNotEmpty()) {
                                                onAddStudentByRoll(trimmedQuery, manualStudentName.trim(), StudentAttendanceRecord.STATUS_ABSENT)
                                                Toast.makeText(context, "${manualStudentName.trim()} added as Absent!", Toast.LENGTH_SHORT).show()
                                                rollQuery = ""
                                                manualStudentName = ""
                                            } else {
                                                Toast.makeText(context, "Please enter student name", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Add Absent (A)", fontSize = 12.sp, color = AttendanceAbsent, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (suggestions.isNotEmpty() && exactDirectoryMatch == null) {
                        // Multiple partial suggestions
                        Text("Suggested SLC Students (Tap to Add):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            suggestions.forEach { stu ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddStudentItem(stu)
                                            Toast.makeText(context, "${stu.name} marked Present!", Toast.LENGTH_SHORT).show()
                                            rollQuery = ""
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(stu.rollNo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(stu.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Stats & Quick Batch Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live Counters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalCount", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = AttendancePresentBg
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Present", fontSize = 10.sp, color = AttendancePresent)
                                Text("$presentCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AttendancePresent)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = AttendanceAbsentBg
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Absent", fontSize = 10.sp, color = AttendanceAbsent)
                                Text("$absentCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AttendanceAbsent)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = AttendanceLeaveBg
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Leave", fontSize = 10.sp, color = AttendanceLeave)
                                Text("$leaveCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AttendanceLeave)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.0f", attendancePct)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Batch Actions Row
                    if (directStudents.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onMarkAll(StudentAttendanceRecord.STATUS_PRESENT) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { onMarkAll(StudentAttendanceRecord.STATUS_ABSENT) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("All Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // 4. Primary Actions: WhatsApp Share, Excel Export & Save Session
        if (directStudents.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onShareWhatsApp(context, lectureTitle, selectedDate)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("direct_whatsapp_share_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                onExportExcel(context, lectureTitle, selectedDate)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("direct_excel_export_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AttendancePresent)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export Excel", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            onSaveToDatabase(context, lectureTitle, selectedDate) { saved ->
                                if (saved) {
                                    // kept in view or reset
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("direct_save_record_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save to Teacher Classes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Students List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Students in Session (${directStudents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (directStudents.isNotEmpty()) {
                    Text(
                        text = "Tap P / A / L to toggle",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Empty State
        if (directStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No students added yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upar search bar me student ka roll number type karein. Shyam Lal College ke bachhe auto-add ho jayenge.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Student Items List
            itemsIndexed(directStudents, key = { _, s -> s.rollNo }) { index, student ->
                DirectStudentRowItem(
                    index = index + 1,
                    student = student,
                    onUpdateStatus = { newStatus -> onUpdateStatus(student.rollNo, newStatus) },
                    onDelete = { onRemoveStudent(student.rollNo) }
                )
            }
        }
    }

    // Bulk Rolls Paste Dialog
    if (showBulkDialog) {
        BulkRollsDialog(
            onDismiss = { showBulkDialog = false },
            onConfirm = { rollsText ->
                val rolls = rollsText
                    .split(",", "\n", " ", ";", "\t")
                    .map { it.trim().uppercase() }
                    .filter { it.isNotEmpty() }

                var addedCount = 0
                rolls.forEach { r ->
                    val added = onAddStudentByRoll(r, null, StudentAttendanceRecord.STATUS_PRESENT)
                    if (added) addedCount++
                }
                Toast.makeText(context, "$addedCount students auto-added from college roster!", Toast.LENGTH_LONG).show()
                showBulkDialog = false
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Current Session?") },
            text = { Text("Yeh session ke saare added students clear ho jayenge.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DirectStudentRowItem(
    index: Int,
    student: DirectStudentEntry,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (student.status) {
                StudentAttendanceRecord.STATUS_PRESENT -> AttendancePresentBg.copy(alpha = 0.5f)
                StudentAttendanceRecord.STATUS_ABSENT -> AttendanceAbsentBg.copy(alpha = 0.5f)
                else -> AttendanceLeaveBg.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Index & Student details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$index.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    Text(
                        text = student.rollNo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = student.studentName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick P / A / L status buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Present
                StatusTogglePill(
                    label = "P",
                    isSelected = student.status == StudentAttendanceRecord.STATUS_PRESENT,
                    activeColor = AttendancePresent,
                    onClick = { onUpdateStatus(StudentAttendanceRecord.STATUS_PRESENT) }
                )

                // Absent
                StatusTogglePill(
                    label = "A",
                    isSelected = student.status == StudentAttendanceRecord.STATUS_ABSENT,
                    activeColor = AttendanceAbsent,
                    onClick = { onUpdateStatus(StudentAttendanceRecord.STATUS_ABSENT) }
                )

                // Leave
                StatusTogglePill(
                    label = "L",
                    isSelected = student.status == StudentAttendanceRecord.STATUS_LEAVE,
                    activeColor = AttendanceLeave,
                    onClick = { onUpdateStatus(StudentAttendanceRecord.STATUS_LEAVE) }
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTogglePill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isSelected) activeColor else Color.Transparent)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BulkRollsDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var rawRolls by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Bulk Paste Roll Numbers", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Roll numbers paste karein (comma, space, ya enter separated):\nExample: 260015, 260017, 260020",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rawRolls,
                    onValueChange = { rawRolls = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Paste rolls here...") },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rawRolls) },
                enabled = rawRolls.isNotBlank()
            ) {
                Text("Add All")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
