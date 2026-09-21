package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.StudentAttendanceRecord
import com.example.data.TeacherClass
import com.example.util.ExcelExportUtil
import com.example.util.YearMonthOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ExportScopeType(val label: String) {
    MONTH("Monthly"),
    RANGE("Date Range"),
    ALL("All Sessions")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportExcelDialog(
    teacherClass: TeacherClass,
    attendanceRecords: List<StudentAttendanceRecord>,
    onDismiss: () -> Unit,
    onExport: (startDate: String?, endDate: String?, rangeLabel: String, shareToWhatsApp: Boolean) -> Unit
) {
    val context = LocalContext.current
    val todayCal = Calendar.getInstance()
    val dayFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val todayStr = remember { dayFormatter.format(todayCal.time) }

    // Extract available months from records
    val availableMonths = remember(attendanceRecords) {
        ExcelExportUtil.extractAvailableMonths(attendanceRecords)
    }

    // Export Scope Tab (0: Monthly, 1: Date Range, 2: All Dates)
    var selectedScopeIndex by remember { mutableIntStateOf(0) }

    // Month Selection State
    var selectedMonthOption by remember {
        mutableStateOf(availableMonths.firstOrNull() ?: YearMonthOption("Current Month", todayStr.take(7), "$todayStr.take(7)-01", todayStr, 0))
    }

    // Date Range Selection State
    // Default From: 1st of current month
    val firstDayOfMonth = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        dayFormatter.format(cal.time)
    }
    var startDate by remember { mutableStateOf(firstDayOfMonth) }
    var endDate by remember { mutableStateOf(todayStr) }

    // Helper date picker dialogs
    fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        try {
            dayFormatter.parse(initialDate)?.let { c.time = it }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selected)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Format human readable date
    fun formatDisplay(dateStr: String): String {
        return try {
            val d = dayFormatter.parse(dateStr)
            if (d != null) displayDateFormatter.format(d) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    // Calculate effective sessions count in current selection
    val effectiveSessionsCount by remember(selectedScopeIndex, selectedMonthOption, startDate, endDate, attendanceRecords) {
        derivedStateOf {
            when (selectedScopeIndex) {
                0 -> {
                    // Monthly
                    attendanceRecords.filter { it.date.startsWith(selectedMonthOption.yearMonth) }
                        .map { it.date }
                        .distinct()
                        .size
                }
                1 -> {
                    // Custom Range
                    attendanceRecords.filter { it.date >= startDate && it.date <= endDate }
                        .map { it.date }
                        .distinct()
                        .size
                }
                else -> {
                    // All
                    attendanceRecords.map { it.date }.distinct().size
                }
            }
        }
    }

    val currentRangeLabel by remember(selectedScopeIndex, selectedMonthOption, startDate, endDate) {
        derivedStateOf {
            when (selectedScopeIndex) {
                0 -> selectedMonthOption.label
                1 -> "${formatDisplay(startDate)} to ${formatDisplay(endDate)}"
                else -> "Complete Semester (All Sessions)"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_excel_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Excel",
                        tint = Color(0xFF2E7D32)
                    )
                }
                Column {
                    Text(
                        text = "Export to Excel Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${teacherClass.className} • ${teacherClass.semester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Description
                Text(
                    text = "Generate a complete .csv spreadsheet with Class Name, Date Range, Student Names, Roll Numbers, and individual attendance columns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tab Row for Scope Selection
                TabRow(
                    selectedTabIndex = selectedScopeIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("export_scope_tabs")
                ) {
                    Tab(
                        selected = selectedScopeIndex == 0,
                        onClick = { selectedScopeIndex = 0 },
                        text = { Text("By Month", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedScopeIndex == 1,
                        onClick = { selectedScopeIndex = 1 },
                        text = { Text("Date Range", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedScopeIndex == 2,
                        onClick = { selectedScopeIndex = 2 },
                        text = { Text("All Dates", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                }

                // Scope Content
                when (selectedScopeIndex) {
                    0 -> {
                        // Month Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Month:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableMonths.forEach { option ->
                                    val isSelected = option.yearMonth == selectedMonthOption.yearMonth
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMonthOption = option },
                                        label = {
                                            Text(
                                                text = if (option.sessionCount > 0) "${option.label} (${option.sessionCount})" else option.label,
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF1565C0),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Custom Date Range
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Date Range:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // From Date Button
                                OutlinedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            showDatePicker(startDate) { picked ->
                                                startDate = picked
                                                if (picked > endDate) endDate = picked
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "FROM DATE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "From Date",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = formatDisplay(startDate),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // To Date Button
                                OutlinedCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            showDatePicker(endDate) { picked ->
                                                endDate = picked
                                                if (picked < startDate) startDate = picked
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "TO DATE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "To Date",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = formatDisplay(endDate),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Shortcuts for Date Range
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ShortcutChip("Last 7 Days") {
                                    val c = Calendar.getInstance()
                                    endDate = dayFormatter.format(c.time)
                                    c.add(Calendar.DAY_OF_YEAR, -6)
                                    startDate = dayFormatter.format(c.time)
                                }
                                ShortcutChip("Last 15 Days") {
                                    val c = Calendar.getInstance()
                                    endDate = dayFormatter.format(c.time)
                                    c.add(Calendar.DAY_OF_YEAR, -14)
                                    startDate = dayFormatter.format(c.time)
                                }
                                ShortcutChip("Last 30 Days") {
                                    val c = Calendar.getInstance()
                                    endDate = dayFormatter.format(c.time)
                                    c.add(Calendar.DAY_OF_YEAR, -29)
                                    startDate = dayFormatter.format(c.time)
                                }
                                ShortcutChip("This Month") {
                                    val c = Calendar.getInstance()
                                    endDate = dayFormatter.format(c.time)
                                    c.set(Calendar.DAY_OF_MONTH, 1)
                                    startDate = dayFormatter.format(c.time)
                                }
                            }
                        }
                    }

                    2 -> {
                        // All Sessions Info
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Complete Academic Term",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Exports every session recorded from beginning to date.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Range Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F8E9) // Light green tint
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selected Period:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "$effectiveSessionsCount Sessions Found",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (effectiveSessionsCount > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentRangeLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Option: Open/Download Excel
                Button(
                    onClick = {
                        val (sDate, eDate) = when (selectedScopeIndex) {
                            0 -> Pair(selectedMonthOption.startDate, selectedMonthOption.endDate)
                            1 -> Pair(startDate, endDate)
                            else -> Pair(null, null)
                        }
                        onExport(sDate, eDate, currentRangeLabel, false)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_export_excel_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open / Download Excel (.csv)")
                }

                // Secondary Option: Share directly to WhatsApp
                OutlinedButton(
                    onClick = {
                        val (sDate, eDate) = when (selectedScopeIndex) {
                            0 -> Pair(selectedMonthOption.startDate, selectedMonthOption.endDate)
                            1 -> Pair(startDate, endDate)
                            else -> Pair(null, null)
                        }
                        onExport(sDate, eDate, currentRangeLabel, true)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_excel_whatsapp_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B5E20))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Excel File via WhatsApp")
                }

                // Dismiss
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel")
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun ShortcutChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
