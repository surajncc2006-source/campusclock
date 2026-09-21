package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OverallAttendanceStats
import com.example.data.Subject
import com.example.data.SubjectStats
import com.example.ui.theme.AttendanceAbsent
import com.example.ui.theme.AttendanceAbsentBg
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg
import java.util.Locale

@Composable
fun SubjectsScreen(
    subjectsWithStats: List<SubjectStats>,
    overallStats: OverallAttendanceStats,
    onAddSubjectClick: () -> Unit,
    onEditSubjectClick: (Subject) -> Unit,
    onDeleteSubjectClick: (Subject) -> Unit,
    onQuickIncrement: (subjectId: Long, isPresent: Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("subjects_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Analytics Hero Card
            item {
                OverallStatsCard(overallStats = overallStats)
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Subjects & Target",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${subjectsWithStats.size} Registered",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Subjects list
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No Subjects Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap the '+' button below to add your first college course.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(subjectsWithStats, key = { it.subject.id }) { stat ->
                    SubjectAnalyticsCard(
                        stat = stat,
                        onEdit = { onEditSubjectClick(stat.subject) },
                        onDelete = { onDeleteSubjectClick(stat.subject) },
                        onQuickIncrement = { isPresent ->
                            onQuickIncrement(stat.subject.id, isPresent)
                        }
                    )
                }
            }

            // Spacer for FAB clearance
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to add subject
        FloatingActionButton(
            onClick = onAddSubjectClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_subject"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Subject")
        }
    }
}

@Composable
fun OverallStatsCard(overallStats: OverallAttendanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Delhi University Target: ${overallStats.targetPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (overallStats.isEligible) AttendancePresentBg else AttendanceAbsentBg
                ) {
                    Text(
                        text = if (overallStats.isEligible) "ELIGIBLE" else "LOW ATTENDANCE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (overallStats.isEligible) AttendancePresent else AttendanceAbsent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large percentage and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", overallStats.percentage)}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (overallStats.isEligible) AttendancePresent else AttendanceAbsent
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${overallStats.totalAttended} Attended",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AttendancePresent
                    )
                    Text(
                        text = "${overallStats.totalHeld} Classes Held",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (overallStats.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (overallStats.isEligible) AttendancePresent else AttendanceAbsent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // DU Bunk advice pill
            val adviceText = when {
                overallStats.totalHeld == 0 -> "Mark your classes to see safe bunk advice."
                overallStats.safeBunks > 0 -> "You can safely bunk ${overallStats.safeBunks} class(es) and remain above ${overallStats.targetPercentage.toInt()}%!"
                overallStats.classesNeeded > 0 -> "Must attend the next ${overallStats.classesNeeded} classes consecutively to hit ${overallStats.targetPercentage.toInt()}%."
                else -> "Exactly meeting the criteria. Keep attending!"
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = adviceText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SubjectAnalyticsCard(
    stat: SubjectStats,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickIncrement: (isPresent: Boolean) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val subColor = try {
        Color(android.graphics.Color.parseColor(stat.subject.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${stat.subject.id}"),
        shape = RoundedCornerShape(18.dp),
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
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(subColor)
                    )
                    Column {
                        Text(
                            text = stat.subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val subInfo = buildString {
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
                        if (subInfo.isNotEmpty()) {
                            Text(
                                text = subInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Subject") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attendance ratio and percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", stat.percentage)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.isEligible) AttendancePresent else AttendanceAbsent
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "(Target: ${stat.subject.targetPercentage.toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${stat.totalAttended} / ${stat.totalHeld} Classes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (stat.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (stat.isEligible) AttendancePresent else AttendanceAbsent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Advice Pill & Quick Increment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val advice = when {
                    stat.totalHeld == 0 -> "No classes marked"
                    stat.safeBunks > 0 -> "Bunk safe: ${stat.safeBunks} class(es)"
                    stat.classesNeeded > 0 -> "Need: ${stat.classesNeeded} class(es)"
                    else -> "On track"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stat.isEligible) AttendancePresentBg else AttendanceAbsentBg
                ) {
                    Text(
                        text = advice,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.isEligible) AttendancePresent else AttendanceAbsent
                    )
                }

                // Quick +1 Present / +1 Absent buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { onQuickIncrement(true) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AttendancePresent
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("+Attended", fontSize = 12.sp, color = AttendancePresent)
                    }

                    OutlinedButton(
                        onClick = { onQuickIncrement(false) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AttendanceAbsent
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("+Missed", fontSize = 12.sp, color = AttendanceAbsent)
                    }
                }
            }
        }
    }
}
