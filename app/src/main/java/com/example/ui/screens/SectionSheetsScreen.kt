package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import com.example.util.WhatsAppShareUtil
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CollegeRosterDirectory
import com.example.data.CollegeStudentItem
import com.example.data.GlobalStudentSearchResult
import com.example.data.SectionSheet
import com.example.data.TeacherClass
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg
import java.io.File
import java.io.FileWriter

enum class SheetsSearchScope {
    ALL_COMBINATIONS_AND_YEARS,
    CURRENT_SHEET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSheetsScreen(
    initialYear: String = "3rd Year",
    initialTabCode: String = "EC+OM",
    onTakeAttendanceForSheet: (year: String, courseCode: String) -> Unit,
    onImportToMyClasses: (year: String, courseCode: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Active Year & Active Course Tab state
    val years = listOf("1st Year", "2nd Year", "3rd Year")
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedCourseTab by remember { mutableStateOf(initialTabCode) }
    var selectedSectionFilter by remember(selectedYear, selectedCourseTab) { mutableStateOf<String?>("All") }
    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf(SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS) }
    var globalYearFilter by remember { mutableStateOf("All") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }

    // Dynamic local custom students loaded on top of the directory
    val customAddedStudents = remember { mutableStateListOf<CollegeStudentItem>() }

    val currentRoster = remember(selectedYear, selectedCourseTab, selectedSectionFilter) {
        CollegeRosterDirectory.getRoster(selectedYear, selectedCourseTab, selectedSectionFilter)
    }

    val displayStudents = remember(currentRoster, customAddedStudents.size, searchQuery, searchScope) {
        val base = currentRoster.students + customAddedStudents.filter { it.rollNo.isNotEmpty() }
        if (searchQuery.isBlank() || searchScope == SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS) {
            base
        } else {
            base.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.rollNo.contains(searchQuery, ignoreCase = true) ||
                it.sNo.toString() == searchQuery.trim()
            }
        }
    }

    val isGlobalSearchActive = searchQuery.isNotBlank() && searchScope == SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS

    val globalSearchResults = remember(searchQuery, globalYearFilter, isGlobalSearchActive) {
        if (isGlobalSearchActive) {
            CollegeRosterDirectory.searchAllCombinationsAndYears(
                query = searchQuery,
                yearFilter = globalYearFilter,
                limit = 120
            )
        } else {
            emptyList()
        }
    }

    val courseInfo = CollegeRosterDirectory.courseTabs.find { it.code == selectedCourseTab }
        ?: CollegeRosterDirectory.courseTabs[0]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Section List: $selectedYear (2026-2027)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Excel Workbook",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Shyam Lal College (University of Delhi) • Shahdara",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val sheet = currentRoster.copy(students = displayStudents)
                            val summary = WhatsAppShareUtil.buildSectionRosterSummary(sheet)
                            WhatsAppShareUtil.shareTextViaWhatsApp(context, summary)
                        },
                        modifier = Modifier.testTag("whatsapp_section_sheet_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share on WhatsApp",
                            tint = androidx.compose.ui.graphics.Color(0xFF25D366)
                        )
                    }
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("upload_custom_sheet_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload / Add Data")
                    }
                    IconButton(
                        onClick = { showPrintDialog = true },
                        modifier = Modifier.testTag("print_section_sheet_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print Sheet")
                    }
                    IconButton(
                        onClick = {
                            exportSectionCsv(context, currentRoster.copy(students = displayStudents))
                        },
                        modifier = Modifier.testTag("export_section_csv_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Excel / CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Authentic Excel-style bottom sheet tabs
            ExcelBottomTabsBar(
                courseTabs = CollegeRosterDirectory.courseTabs,
                selectedTabCode = selectedCourseTab,
                onSelectTab = {
                    selectedCourseTab = it
                    if (isGlobalSearchActive) {
                        searchScope = SheetsSearchScope.CURRENT_SHEET
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // A. Top Integrated Search Bar (Global & Local search across all combinations and years)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (searchScope == SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS)
                                "Search roll no or student name (all combinations & years)..."
                            else
                                "Search in $selectedCourseTab • $selectedYear...",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_students_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                // Search Scope & Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAllSelected = searchScope == SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { searchScope = SheetsSearchScope.ALL_COMBINATIONS_AND_YEARS }
                            .testTag("scope_all_combinations_btn")
                    ) {
                        Text(
                            text = "🌐 All Combinations & Years",
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    val isCurrentSelected = searchScope == SheetsSearchScope.CURRENT_SHEET
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrentSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrentSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { searchScope = SheetsSearchScope.CURRENT_SHEET }
                            .testTag("scope_current_sheet_btn")
                    ) {
                        Text(
                            text = "📄 Current: $selectedCourseTab • $selectedYear",
                            fontSize = 11.sp,
                            fontWeight = if (isCurrentSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrentSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    if (isGlobalSearchActive) {
                        listOf("All", "1st Year", "2nd Year", "3rd Year").forEach { yr ->
                            val isYrSelected = globalYearFilter == yr
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isYrSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isYrSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { globalYearFilter = yr }
                                    .testTag("global_year_filter_$yr")
                            ) {
                                Text(
                                    text = yr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isYrSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isYrSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            if (isGlobalSearchActive) {
                // GLOBAL SEARCH RESULTS VIEW
                GlobalSearchResultsHeader(
                    query = searchQuery,
                    count = globalSearchResults.size,
                    yearFilter = globalYearFilter
                )

                if (globalSearchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "No Student Found Matching \"$searchQuery\"",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try searching by full or partial Roll No (e.g., 240402, 260001) or student name across 1st, 2nd, and 3rd year combinations.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(globalSearchResults, key = { "${it.year}_${it.courseCode}_${it.student.rollNo}_${it.section}" }) { res ->
                            GlobalStudentResultCard(
                                result = res,
                                onOpenInSheet = {
                                    selectedYear = res.year
                                    selectedCourseTab = res.courseCode
                                    selectedSectionFilter = res.section
                                    searchScope = SheetsSearchScope.CURRENT_SHEET
                                    Toast.makeText(
                                        context,
                                        "Jumped to ${res.courseTitle} (${res.year}, ${res.section})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onTakeAttendance = {
                                    onTakeAttendanceForSheet(res.year, res.courseCode)
                                },
                                onShareWhatsApp = {
                                    WhatsAppShareUtil.shareStudentRosterDetails(
                                        context = context,
                                        studentName = res.student.name,
                                        rollNo = res.student.rollNo,
                                        courseTitle = res.courseTitle,
                                        year = res.year,
                                        section = res.section,
                                        sNo = res.student.sNo
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // NORMAL SHEET OR LOCAL FILTER VIEW
                // 1. Year Selector Tabs (1st Year, 2nd Year, 3rd Year)
                YearSelectorRow(
                    years = years,
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it }
                )

                // 2. Official Document Header Card
                OfficialDocumentHeader(
                    year = selectedYear,
                    courseCode = selectedCourseTab,
                    courseTitle = currentRoster.courseTitle,
                    totalStudents = displayStudents.size,
                    department = courseInfo.department,
                    room = courseInfo.defaultRoom,
                    onTakeAttendance = { onTakeAttendanceForSheet(selectedYear, selectedCourseTab) },
                    onImportToClasses = { onImportToMyClasses(selectedYear, selectedCourseTab) }
                )

                // 2b. Multi-Section Filter (when course has Section-A, Section-B, Section-C, Section-D)
                if (currentRoster.availableSections.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Section:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        currentRoster.availableSections.forEach { sec ->
                            val isSelected = (selectedSectionFilter ?: "All") == sec
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { selectedSectionFilter = sec }
                                    .testTag("section_chip_$sec")
                            ) {
                                Text(
                                    text = sec,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Students Table Header
                StudentsTableHeader()

                // 5. Students List (formatted like College tentative list)
                if (displayStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No students match '$searchQuery' in this sheet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("students_roster_table")
                    ) {
                        itemsIndexed(displayStudents) { index, student ->
                            StudentTableRow(
                                sNo = student.sNo,
                                rollNo = student.rollNo,
                                name = student.name,
                                section = student.section,
                                isEven = index % 2 == 0
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }

    // Dialog: Import / Upload Custom Student Roster
    if (showImportDialog) {
        ImportSheetDialog(
            currentYear = selectedYear,
            currentTabCode = selectedCourseTab,
            onDismiss = { showImportDialog = false },
            onImportStudents = { parsed ->
                var startSNo = (currentRoster.students.maxOfOrNull { it.sNo } ?: 0) + 1
                parsed.forEach { (roll, name) ->
                    customAddedStudents.add(
                        CollegeStudentItem(
                            sNo = startSNo++,
                            rollNo = roll,
                            name = name
                        )
                    )
                }
                showImportDialog = false
                Toast.makeText(context, "Added ${parsed.size} students to $selectedCourseTab!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Print Sheet Preview & Share
    if (showPrintDialog) {
        PrintSheetDialog(
            sheet = currentRoster.copy(students = displayStudents),
            onDismiss = { showPrintDialog = false },
            onPrintNow = {
                printOrShareSection(context, currentRoster.copy(students = displayStudents))
                showPrintDialog = false
            }
        )
    }
}

/**
 * Header row for global student search results
 */
@Composable
fun GlobalSearchResultsHeader(
    query: String,
    count: Int,
    yearFilter: String
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "Search in All Combinations for \"$query\":",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "$count found",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Card displaying matching student from global cross-year & cross-combination search
 */
@Composable
fun GlobalStudentResultCard(
    result: GlobalStudentSearchResult,
    onOpenInSheet: () -> Unit,
    onTakeAttendance: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("global_student_card_${result.student.rollNo}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Student Name and Roll Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "S.No. #${result.student.sNo} in Class Roster",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = result.student.rollNo,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Middle Badges: Year, Course, Section, Room
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year badge
                val (yearBg, yearFg) = when {
                    result.year.contains("1") -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
                    result.year.contains("2") -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
                    else -> Color(0xFFEDE7F6) to Color(0xFF4A148C)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = yearBg
                ) {
                    Text(
                        text = result.year,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = yearFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Course Code and Title
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${result.courseCode} • ${result.courseTitle}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Section
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = result.section,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Room
                Text(
                    text = "• ${result.defaultRoom}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bottom Actions: Open In Sheet, Roll Call, WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenInSheet,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("open_sheet_${result.student.rollNo}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Open In Sheet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onTakeAttendance,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("roll_call_${result.student.rollNo}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Roll Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onShareWhatsApp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("whatsapp_student_${result.student.rollNo}")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share on WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Top Segmented Year Selector
 */
@Composable
fun YearSelectorRow(
    years: List<String>,
    selectedYear: String,
    onYearSelected: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            years.forEach { yr ->
                val isSelected = yr == selectedYear
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onYearSelected(yr) }
                        .testTag("year_chip_$yr")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = yr,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "2026-27",
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic Document Header Card styled like Shyam Lal College tentative roll list
 */
@Composable
fun OfficialDocumentHeader(
    year: String,
    courseCode: String,
    courseTitle: String,
    totalStudents: Int,
    department: String,
    room: String,
    onTakeAttendance: () -> Unit,
    onImportToClasses: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Shyam Lal College (University of Delhi)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shahdara, Delhi - 110032",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AttendancePresentBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AttendancePresent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Tentative Roll List",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AttendancePresent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))

            // Course title and section
            Text(
                text = courseTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enrolled: $totalStudents Students",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Room: $room",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onImportToClasses,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("sync_to_my_classes_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add to My Classes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Table header row
 */
@Composable
fun StudentsTableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "S. No.",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(55.dp)
            )
            Text(
                text = "Roll No.",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(110.dp)
            )
            Text(
                text = "Student Name",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Sec",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

/**
 * Individual student table row
 */
@Composable
fun StudentTableRow(
    sNo: Int,
    rollNo: String,
    name: String,
    section: String = "Section-A",
    isEven: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isEven) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sNo.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(55.dp)
        )
        Text(
            text = rollNo,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            val secLabel = section.removePrefix("Section-").ifEmpty { "A" }
            Text(
                text = secLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Excel-Style Bottom Tabs Bar matching the 18 tabs:
 * EC+OM, EC+PS, EN+EC, EN+PS, HN+HS, HS+PS, EC, EN, HN, HS, PS, BCOM, BCH, CH(H), MT(H), BSC.CH, BSC.CS, BSC.EL
 */
@Composable
fun ExcelBottomTabsBar(
    courseTabs: List<com.example.data.CourseSectionTab>,
    selectedTabCode: String,
    onSelectTab: (String) -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // Title label for the bottom workbook tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHEET TABS (${courseTabs.size} Courses)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Active: $selectedTabCode",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            ScrollableTabRow(
                selectedTabIndex = courseTabs.indexOfFirst { it.code == selectedTabCode }.coerceAtLeast(0),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    val idx = courseTabs.indexOfFirst { it.code == selectedTabCode }.coerceAtLeast(0)
                    if (idx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("excel_sheets_tab_row")
            ) {
                courseTabs.forEach { tab ->
                    val isSelected = tab.code == selectedTabCode
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTab(tab.code) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(tab.colorHex))
                                            } catch (e: Exception) {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                )
                                Text(
                                    text = tab.code,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_${tab.code}")
                    )
                }
            }
        }
    }
}

/**
 * Dialog to Import / Upload new students to any section sheet
 */
@Composable
fun ImportSheetDialog(
    currentYear: String,
    currentTabCode: String,
    onDismiss: () -> Unit,
    onImportStudents: (List<Pair<String, String>>) -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            """
            240430, ROHAN SHARMA
            240431, PRIYA VERMA
            240432, AMIT KUMAR
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Upload / Add to $currentTabCode")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Paste student records for $currentYear ($currentTabCode). Format: 'RollNo, Student Name' (one per line):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("upload_raw_text_input"),
                    placeholder = { Text("240402, Shambhawi Srivastava\n240403, Khush Jaggi") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = rawText.lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .mapNotNull { line ->
                            val parts = line.split(",", "\t", "-").map { it.trim() }
                            if (parts.size >= 2) {
                                Pair(parts[0], parts[1])
                            } else null
                        }
                    onImportStudents(parsed)
                },
                modifier = Modifier.testTag("confirm_upload_btn")
            ) {
                Text("Upload to Sheet")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog: Print Sheet Preview
 */
@Composable
fun PrintSheetDialog(
    sheet: SectionSheet,
    onDismiss: () -> Unit,
    onPrintNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Print Official Sheet")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Shyam Lal College (University of Delhi)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${sheet.courseTitle}\nTotal Students: ${sheet.students.size}\nStatus: Tentative Section List",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "This will format and send the complete college tentative register to the Android system Print/Share service.",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPrintNow,
                modifier = Modifier.testTag("print_confirm_btn")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Print / Share")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun printOrShareSection(context: Context, sheet: SectionSheet) {
    val sb = StringBuilder()
    sb.append("\"Shyam Lal College (University of Delhi), Shahdara, Delhi - 110032\"\n")
    sb.append("University of Delhi - Tentative Section List\n")
    sb.append("${sheet.courseTitle} - ${sheet.section}\n")
    sb.append("Total Students: ${sheet.students.size}\n")
    sb.append("====================================================\n")
    sb.append("S. No. \tRoll No.\tStudent Name\n")
    sb.append("----------------------------------------------------\n")

    sheet.students.forEach { s ->
        sb.append("${s.sNo}\t${s.rollNo}\t${s.name}\n")
    }
    sb.append("====================================================\n")
    sb.append("Generated by CampusClock SLC DU Faculty System\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "SLC DU Tentative List - ${sheet.courseCode} ${sheet.year}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Print / Share Section Sheet"))
}

private fun exportSectionCsv(context: Context, sheet: SectionSheet) {
    try {
        val file = File(context.cacheDir, "SLC_${sheet.courseCode}_${sheet.year.replace(" ", "_")}.csv")
        FileWriter(file).use { writer ->
            writer.write("\"Shyam Lal College (University of Delhi), Shahdara, Delhi - 110032\",,,,,,\n")
            writer.write(",,,,,,\n")
            writer.write(",,,,,,Tentative\n")
            writer.write(",,,,,,\n")
            writer.write("\"${sheet.courseTitle}\",,,,,,${sheet.section}\n")
            writer.write(",,,,,,\n")
            writer.write("S. No. ,Roll No.,Name,,S. No. ,Roll No.,Name\n")

            // Write pairs side by side like college CSV
            val half = (sheet.students.size + 1) / 2
            for (i in 0 until half) {
                val left = sheet.students[i]
                val rightIndex = i + half
                val right = if (rightIndex < sheet.students.size) sheet.students[rightIndex] else null

                if (right != null) {
                    writer.write("${left.sNo},${left.rollNo},\"${left.name}\",,${right.sNo},${right.rollNo},\"${right.name}\"\n")
                } else {
                    writer.write("${left.sNo},${left.rollNo},\"${left.name}\",,,,\n")
                }
            }
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Download Section CSV"))
        Toast.makeText(context, "CSV generated for ${sheet.courseCode}!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
