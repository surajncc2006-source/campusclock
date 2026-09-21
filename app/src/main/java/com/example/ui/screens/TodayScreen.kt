package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.OverallAttendanceStats
import com.example.data.SubjectStats
import com.example.ui.theme.AttendanceAbsent
import com.example.ui.theme.AttendanceAbsentBg
import com.example.ui.theme.AttendanceCancelled
import com.example.ui.theme.AttendanceCancelledBg
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    subjectsWithStats: List<SubjectStats>,
    todayRecords: List<AttendanceRecord>,
    overallStats: OverallAttendanceStats,
    onMarkAttendance: (subjectId: Long, status: String) -> Unit,
    onNavigateToSubjects: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.US) }

    val parsedDate = try {
        dateFormat.parse(selectedDate) ?: Date()
    } catch (_: Exception) {
        Date()
    }
    val isToday = dateFormat.format(Date()) == selectedDate

    val recordBySubject = remember(todayRecords) {
        todayRecords.associateBy { it.subjectId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = parsedDate
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            onDateChange(dateFormat.format(cal.time))
                        },
                        modifier = Modifier.testTag("prev_date_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Quick reset to today if clicked
                            onDateChange(dateFormat.format(Date()))
                        }
                    ) {
                        Text(
                            text = if (isToday) "Today, ${displayFormat.format(parsedDate)}" else displayFormat.format(parsedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isToday) {
                            Text(
                                text = "Tap to jump back to Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = parsedDate
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            onDateChange(dateFormat.format(cal.time))
                        },
                        modifier = Modifier.testTag("next_date_button")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }
        }

        // Quick Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Attendance Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Shyam Lal College (DU) • 100% Offline Vault",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (overallStats.isEligible) AttendancePresentBg else AttendanceAbsentBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (overallStats.isEligible) AttendancePresent else AttendanceAbsent
                            )
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", overallStats.percentage)}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (overallStats.isEligible) AttendancePresent else AttendanceAbsent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val markedCount = todayRecords.size
                    val totalSubs = subjectsWithStats.size
                    Text(
                        text = if (totalSubs == 0) "No subjects registered yet."
                        else "Marked $markedCount of $totalSubs classes for this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Subjects Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Classes for this Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${subjectsWithStats.size} Subjects",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Empty state
        if (subjectsWithStats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No subjects added yet!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add your DU courses to start recording attendance.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onNavigateToSubjects) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Subject")
                        }
                    }
                }
            }
        } else {
            items(subjectsWithStats, key = { it.subject.id }) { stat ->
                val currentRecord = recordBySubject[stat.subject.id]
                val currentStatus = currentRecord?.status

                TodaySubjectCard(
                    stat = stat,
                    currentStatus = currentStatus,
                    onStatusSelected = { status ->
                        onMarkAttendance(stat.subject.id, status)
                    }
                )
            }
        }
    }
}

@Composable
fun TodaySubjectCard(
    stat: SubjectStats,
    currentStatus: String?,
    onStatusSelected: (String) -> Unit
) {
    val subColor = try {
        Color(android.graphics.Color.parseColor(stat.subject.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_subject_${stat.subject.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(subColor)
                    )
                    Column {
                        Text(
                            text = stat.subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val details = buildString {
                            if (stat.subject.code.isNotEmpty()) append(stat.subject.code)
                            if (stat.subject.room.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append(stat.subject.room)
                            }
                            if (stat.subject.teacher.isNotEmpty()) {
                                if (isNotEmpty()) append(" • ")
                                append(stat.subject.teacher)
                            }
                        }
                        if (details.isNotEmpty()) {
                            Text(
                                text = details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stats badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stat.isEligible) AttendancePresentBg else AttendanceAbsentBg,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", stat.percentage)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.isEligible) AttendancePresent else AttendanceAbsent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status marking buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Present Button
                val isPresent = currentStatus == AttendanceRecord.STATUS_PRESENT
                Button(
                    onClick = { onStatusSelected(AttendanceRecord.STATUS_PRESENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_present_${stat.subject.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPresent) AttendancePresent else AttendancePresentBg,
                        contentColor = if (isPresent) Color.White else AttendancePresent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isPresent) "Present" else "Present",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Absent Button
                val isAbsent = currentStatus == AttendanceRecord.STATUS_ABSENT
                Button(
                    onClick = { onStatusSelected(AttendanceRecord.STATUS_ABSENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_absent_${stat.subject.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAbsent) AttendanceAbsent else AttendanceAbsentBg,
                        contentColor = if (isAbsent) Color.White else AttendanceAbsent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isAbsent) "Absent" else "Absent",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Cancelled / Off Button
                val isCancelled = currentStatus == AttendanceRecord.STATUS_CANCELLED
                Button(
                    onClick = { onStatusSelected(AttendanceRecord.STATUS_CANCELLED) },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("btn_cancelled_${stat.subject.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCancelled) AttendanceCancelled else AttendanceCancelledBg,
                        contentColor = if (isCancelled) Color.White else AttendanceCancelled
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isCancelled) "Off" else "No Class",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick advice row
            Spacer(modifier = Modifier.height(8.dp))
            val adviceText = when {
                stat.totalHeld == 0 -> "No classes recorded yet"
                stat.safeBunks > 0 -> "Can bunk ${stat.safeBunks} class(es) safely"
                stat.classesNeeded > 0 -> "Attend next ${stat.classesNeeded} class(es) to hit ${stat.subject.targetPercentage.toInt()}%"
                else -> "On track (Held: ${stat.totalHeld}, Attended: ${stat.totalAttended})"
            }
            Text(
                text = adviceText,
                style = MaterialTheme.typography.labelSmall,
                color = if (stat.isEligible) AttendancePresent else AttendanceAbsent,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
