package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Subject
import com.example.data.TeacherClass
import com.example.ui.components.AddEditClassDialog
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.screens.ClassDetailScreen
import com.example.ui.screens.DirectAttendanceScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileVaultScreen
import com.example.ui.screens.SectionSheetsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TeacherPortalScreen
import com.example.ui.screens.TeacherSignupScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.AttendancePresent
import com.example.ui.theme.AttendancePresentBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusClockApp(
    viewModel: AttendanceViewModel = viewModel()
) {
    val context = LocalContext.current
    var isSplashDone by remember { mutableStateOf(false) }
    val isTeacherRegistered by viewModel.isTeacherRegistered.collectAsStateWithLifecycle()
    val teacherProfile by viewModel.teacherProfile.collectAsStateWithLifecycle()

    // 1. Initial Splash Screen
    if (!isSplashDone) {
        SplashScreen(onSplashFinished = { isSplashDone = true })
        return
    }

    // 2. Teacher Signup Screen (App is strictly for teachers, with autocomplete suggestions)
    if (!isTeacherRegistered) {
        TeacherSignupScreen(
            onSignupComplete = { name, id, dept ->
                viewModel.registerTeacher(name, id, dept)
            }
        )
        return
    }

    // 3. Main Teacher Interface
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Classes, 1: TimeTable Print, 2: Daily Attendance, 3: Faculty Profile

    // Dialog & navigation state
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Teacher class dialogs
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<TeacherClass?>(null) }

    // Flow states
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val subjectsWithStats by viewModel.subjectsWithStats.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val todayRecords by viewModel.selectedDateRecords.collectAsStateWithLifecycle()
    val recordsWithSubject by viewModel.recordsWithSubject.collectAsStateWithLifecycle()

    // Teacher portal flow states
    val teacherClassesWithStats by viewModel.teacherClassesWithStats.collectAsStateWithLifecycle()
    val activeTeacherClass by viewModel.activeTeacherClass.collectAsStateWithLifecycle()
    val activeClassStudents by viewModel.activeClassStudents.collectAsStateWithLifecycle()
    val activeClassAttendanceRecords by viewModel.activeClassAttendanceRecords.collectAsStateWithLifecycle()
    val directStudents by viewModel.directStudents.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "CampusClock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (teacherProfile.name.isNotEmpty()) "${teacherProfile.name} • ${teacherProfile.department}"
                                       else "Shyam Lal College (DU) Faculty",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Faculty ID pill & offline status
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { selectedTab = 4 }
                            .testTag("privacy_indicator_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AttendancePresent)
                            )
                            Text(
                                text = if (teacherProfile.id > 0) "ID: #${teacherProfile.id}" else "Faculty",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        viewModel.selectActiveTeacherClass(null)
                        selectedTab = 0
                    },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = "Direct Attendance") },
                    label = { Text("Direct") },
                    modifier = Modifier.testTag("nav_direct")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        viewModel.selectActiveTeacherClass(null)
                        selectedTab = 1
                    },
                    icon = { Icon(Icons.Default.CastForEducation, contentDescription = "Teacher Classes") },
                    label = { Text("Classes") },
                    modifier = Modifier.testTag("nav_teacher")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        viewModel.selectActiveTeacherClass(null)
                        selectedTab = 2
                    },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Section Lists Workbook") },
                    label = { Text("Sheets") },
                    modifier = Modifier.testTag("nav_sections")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        viewModel.selectActiveTeacherClass(null)
                        selectedTab = 3
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Daily Roll Call") },
                    label = { Text("Roll Call") },
                    modifier = Modifier.testTag("nav_today")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        viewModel.selectActiveTeacherClass(null)
                        selectedTab = 4
                    },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Faculty Profile") },
                    label = { Text("Faculty") },
                    modifier = Modifier.testTag("nav_vault")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Direct Attendance (Jab teacher ya CR class na banayein - instant roll search & marking)
                    DirectAttendanceScreen(
                        directStudents = directStudents,
                        selectedDate = selectedDate,
                        onDateChange = { viewModel.setSelectedDate(it) },
                        onAddStudentByRoll = { roll, manualName, status ->
                            viewModel.addDirectStudentByRoll(roll, manualName, status)
                        },
                        onAddStudentItem = { stu ->
                            viewModel.addDirectStudentItem(stu)
                        },
                        onUpdateStatus = { roll, status ->
                            viewModel.updateDirectStudentStatus(roll, status)
                        },
                        onRemoveStudent = { roll ->
                            viewModel.removeDirectStudent(roll)
                        },
                        onMarkAll = { status ->
                            viewModel.markAllDirectStudents(status)
                        },
                        onClearAll = {
                            viewModel.clearDirectAttendance()
                        },
                        onSaveToDatabase = { ctx, title, date, onSaved ->
                            viewModel.saveDirectAttendanceToDatabase(ctx, title, date, onSaved)
                        },
                        onShareWhatsApp = { ctx, title, date ->
                            viewModel.shareDirectAttendanceWhatsApp(ctx, title, date)
                        },
                        onExportExcel = { ctx, title, date ->
                            viewModel.exportDirectAttendanceToExcel(ctx, title, date)
                        }
                    )
                }
                1 -> {
                    // Teacher Portal: Either Class List or Active Class Detail
                    val currentClass = activeTeacherClass
                    if (currentClass != null) {
                        ClassDetailScreen(
                            teacherClass = currentClass,
                            students = activeClassStudents,
                            allRecords = activeClassAttendanceRecords,
                            selectedDate = selectedDate,
                            onDateChange = { viewModel.setSelectedDate(it) },
                            onBack = { viewModel.selectActiveTeacherClass(null) },
                            onAddStudentSingle = { name, roll, email, initialStatus ->
                                viewModel.addStudentToActiveSingle(name, roll, email, initialStatus)
                            },
                            onAddStudentsBulk = { list ->
                                viewModel.addStudentsToActiveBulk(list)
                            },
                            onDeleteStudent = { stu ->
                                viewModel.deleteStudentFromClass(stu)
                            },
                            onSaveAttendanceBatch = { statusMap ->
                                viewModel.saveActiveAttendanceBatch(statusMap)
                            },
                            onExportExcel = {
                                viewModel.exportActiveTeacherClassToExcel(context, currentClass)
                            }
                        )
                    } else {
                        TeacherPortalScreen(
                            classesWithStats = teacherClassesWithStats,
                            onSelectClass = { cls ->
                                viewModel.selectActiveTeacherClass(cls)
                            },
                            onCreateClassClick = {
                                classToEdit = null
                                showCreateClassDialog = true
                            },
                            onEditClassClick = { cls ->
                                classToEdit = cls
                                showCreateClassDialog = true
                            },
                            onDeleteClassClick = { cls ->
                                viewModel.deleteTeacherClass(cls)
                            },
                            onExportExcelDirect = { cls ->
                                viewModel.exportActiveTeacherClassToExcel(context, cls)
                            },
                            onShareWhatsAppDirect = { cls ->
                                viewModel.shareClassAttendanceToWhatsApp(context, cls)
                            },
                            onLoadFirstYearRoster = {
                                viewModel.loadShyamLalFirstYearRoster()
                            },
                            onLoadAllYearsRosters = {
                                viewModel.loadAllThreeYearsClasses()
                            },
                            onOpenSectionWorkbook = {
                                selectedTab = 2
                            }
                        )
                    }
                }
                2 -> SectionSheetsScreen(
                    initialYear = "3rd Year",
                    initialTabCode = "EC+OM",
                    onTakeAttendanceForSheet = { yr, code ->
                        viewModel.importAndSelectSectionSheet(yr, code) {
                            selectedTab = 1
                        }
                    },
                    onImportToMyClasses = { yr, code ->
                        viewModel.importAndSelectSectionSheet(yr, code)
                    }
                )
                3 -> TodayScreen(
                    selectedDate = selectedDate,
                    onDateChange = { viewModel.setSelectedDate(it) },
                    subjectsWithStats = subjectsWithStats,
                    todayRecords = todayRecords,
                    overallStats = overallStats,
                    onMarkAttendance = { subId, status ->
                        viewModel.markAttendance(subId, status)
                    },
                    onNavigateToSubjects = { selectedTab = 1 }
                )
                4 -> ProfileVaultScreen(
                    userProfile = userProfile,
                    teacherProfile = teacherProfile,
                    onSwitchTeacher = { viewModel.signoutTeacher() },
                    onEditProfileClick = { showEditProfileDialog = true },
                    onResetData = { viewModel.resetAllData() },
                    onGenerateReport = { viewModel.generateAttendanceReport() }
                )
            }
        }
    }

    // Dialog for creating or editing teacher class
    if (showCreateClassDialog) {
        AddEditClassDialog(
            classToEdit = classToEdit,
            onDismiss = {
                showCreateClassDialog = false
                classToEdit = null
            },
            onConfirm = { name, code, dept, sem, sec, color ->
                if (classToEdit == null) {
                    viewModel.createTeacherClass(name, code, dept, sem, sec, color)
                } else {
                    classToEdit?.let { existing ->
                        viewModel.updateTeacherClass(
                            existing.copy(
                                className = name,
                                courseCode = code,
                                department = dept,
                                semester = sem,
                                section = sec,
                                colorHex = color
                            )
                        )
                    }
                }
                showCreateClassDialog = false
                classToEdit = null
            }
        )
    }

    // Dialog for adding a student subject
    if (showAddSubjectDialog) {
        AddEditSubjectDialog(
            subjectToEdit = null,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name, code, teacher, room, target, color ->
                viewModel.addSubject(name, code, teacher, room, target, color)
                showAddSubjectDialog = false
            }
        )
    }

    // Dialog for editing an existing student subject
    subjectToEdit?.let { sub ->
        AddEditSubjectDialog(
            subjectToEdit = sub,
            onDismiss = { subjectToEdit = null },
            onConfirm = { name, code, teacher, room, target, color ->
                viewModel.updateSubject(
                    sub.copy(
                        name = name,
                        code = code,
                        teacher = teacher,
                        room = room,
                        targetPercentage = target,
                        colorHex = color
                    )
                )
                subjectToEdit = null
            }
        )
    }

    // Dialog for editing student profile
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, roll, course, semester, target ->
                viewModel.updateProfile(name, roll, course, semester, target)
                showEditProfileDialog = false
            }
        )
    }
}
