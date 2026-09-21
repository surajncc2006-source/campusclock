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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AttendanceRecord
import com.example.data.RecordWithSubject
import com.example.data.Subject
import com.example.ui.theme.AttendanceAbsent
import com.example.ui.theme.AttendanceAbsentBg
import com.example.ui.theme.AttendanceCancelled
import com.example.ui.theme.AttendanceCancelledBg
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg

@Composable
fun HistoryScreen(
    subjects: List<Subject>,
    recordsWithSubject: List<RecordWithSubject>,
    onDeleteRecord: (AttendanceRecord) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    val filteredRecords = remember(recordsWithSubject, selectedSubjectId) {
        if (selectedSubjectId == null) recordsWithSubject
        else recordsWithSubject.filter { it.record.subjectId == selectedSubjectId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Attendance Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectId == null,
                            onClick = { selectedSubjectId = null },
                            label = { Text("All (${recordsWithSubject.size})") },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                    items(subjects, key = { it.id }) { subject ->
                        val count = recordsWithSubject.count { it.record.subjectId == subject.id }
                        FilterChip(
                            selected = selectedSubjectId == subject.id,
                            onClick = {
                                selectedSubjectId = if (selectedSubjectId == subject.id) null else subject.id
                            },
                            label = { Text("${subject.code.ifEmpty { subject.name }} ($count)") }
                        )
                    }
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
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
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mark attendance in the 'Today' tab to see your log history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredRecords, key = { it.record.id }) { item ->
                AttendanceHistoryItem(
                    item = item,
                    onDelete = { onDeleteRecord(item.record) }
                )
            }
        }
    }
}

@Composable
fun AttendanceHistoryItem(
    item: RecordWithSubject,
    onDelete: () -> Unit
) {
    val (statusText, statusColor, statusBg) = when (item.record.status) {
        AttendanceRecord.STATUS_PRESENT -> Triple("PRESENT", AttendancePresent, AttendancePresentBg)
        AttendanceRecord.STATUS_ABSENT -> Triple("ABSENT", AttendanceAbsent, AttendanceAbsentBg)
        else -> Triple("NO CLASS", AttendanceCancelled, AttendanceCancelledBg)
    }

    val subColor = try {
        Color(android.graphics.Color.parseColor(item.subjectColor))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("record_item_${item.record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(subColor)
                )
                Column {
                    Text(
                        text = item.subjectName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.record.date}${if (item.record.remark.isNotEmpty()) " • ${item.record.remark}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
