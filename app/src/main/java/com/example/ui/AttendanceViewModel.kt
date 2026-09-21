package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRecord
import com.example.data.AttendanceRepository
import com.example.data.ClassStudent
import com.example.data.ClassWithStudentCount
import com.example.data.CollegeRosterDirectory
import com.example.data.CollegeStudentItem
import com.example.data.OverallAttendanceStats
import com.example.data.RecordWithSubject
import com.example.data.StudentAttendanceRecord
import com.example.data.Subject
import com.example.data.SubjectStats
import com.example.data.TeacherClass
import com.example.data.TeacherRepository
import com.example.util.ExcelExportUtil
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val studentName: String = "SLC Student",
    val rollNo: String = "SLC/2024/001",
    val course: String = "B.Sc (Hons) Computer Science",
    val college: String = "Shyam Lal College (DU)",
    val semester: String = "Semester 3",
    val targetCriteria: Double = 75.0
)

data class DirectStudentEntry(
    val rollNo: String,
    val studentName: String,
    val status: String = StudentAttendanceRecord.STATUS_PRESENT
)

data class TeacherProfile(
    val id: Int = 0,
    val name: String = "",
    val department: String = "",
    val college: String = "Shyam Lal College (DU)",
    val designation: String = "Faculty Member",
    val isRegistered: Boolean = false
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    private val teacherRepository: TeacherRepository
    private val prefs = application.getSharedPreferences("campusclock_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val todayString: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _teacherProfile = MutableStateFlow(loadTeacherProfile())
    val teacherProfile: StateFlow<TeacherProfile> = _teacherProfile.asStateFlow()

    private val _isTeacherRegistered = MutableStateFlow(prefs.getBoolean("is_teacher_registered", false))
    val isTeacherRegistered: StateFlow<Boolean> = _isTeacherRegistered.asStateFlow()

    // --- Student / Self Attendance Flows ---
    val allSubjects: StateFlow<List<Subject>>
    val subjectsWithStats: StateFlow<List<SubjectStats>>
    val overallStats: StateFlow<OverallAttendanceStats>
    val allRecords: StateFlow<List<AttendanceRecord>>
    val recordsWithSubject: StateFlow<List<RecordWithSubject>>
    val selectedDateRecords: StateFlow<List<AttendanceRecord>>

    // --- Teacher Portal Flows ---
    val teacherClassesWithStats: StateFlow<List<ClassWithStudentCount>>
    private val _activeTeacherClass = MutableStateFlow<TeacherClass?>(null)
    val activeTeacherClass: StateFlow<TeacherClass?> = _activeTeacherClass.asStateFlow()

    private val _activeClassStudents = MutableStateFlow<List<ClassStudent>>(emptyList())
    val activeClassStudents: StateFlow<List<ClassStudent>> = _activeClassStudents.asStateFlow()

    private val _activeClassAttendanceRecords = MutableStateFlow<List<StudentAttendanceRecord>>(emptyList())
    val activeClassAttendanceRecords: StateFlow<List<StudentAttendanceRecord>> = _activeClassAttendanceRecords.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(database.attendanceDao())
        teacherRepository = TeacherRepository(database.teacherDao())

        allSubjects = repository.allSubjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        subjectsWithStats = repository.subjectsWithStats.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        overallStats = repository.overallStats.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OverallAttendanceStats(0, 0, 0, 0, 100.0, 0, 0, 75.0, true)
        )

        allRecords = repository.allRecords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recordsWithSubject = combine(repository.allRecords, repository.allSubjects) { records, subjects ->
            val subMap = subjects.associateBy { it.id }
            records.map { rec ->
                val sub = subMap[rec.subjectId]
                RecordWithSubject(
                    record = rec,
                    subjectName = sub?.name ?: "Unknown Subject",
                    subjectCode = sub?.code ?: "",
                    subjectColor = sub?.colorHex ?: "#1E3A8A"
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        selectedDateRecords = combine(_selectedDate, repository.allRecords) { date, records ->
            records.filter { it.date == date }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        teacherClassesWithStats = teacherRepository.getClassesWithStudentCounts().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Seed sample data if first run
        viewModelScope.launch {
            val existing = repository.allSubjects.first()
            if (existing.isEmpty()) {
                repository.prepopulateShyamLalCollegeDefaults()
            }
            val existingClasses = teacherRepository.allTeacherClasses.first()
            if (existingClasses.isEmpty()) {
                teacherRepository.prepopulateDefaultTeacherClassIfEmpty()
            }
        }
    }

    private fun loadTeacherProfile(): TeacherProfile {
        val isReg = prefs.getBoolean("is_teacher_registered", false)
        return TeacherProfile(
            id = prefs.getInt("teacher_id", 0),
            name = prefs.getString("teacher_name", "") ?: "",
            department = prefs.getString("teacher_dept", "General") ?: "General",
            college = "Shyam Lal College (DU)",
            designation = "Faculty Member",
            isRegistered = isReg
        )
    }

    fun registerTeacher(name: String, teacherId: Int, department: String) {
        prefs.edit()
            .putBoolean("is_teacher_registered", true)
            .putString("teacher_name", name)
            .putInt("teacher_id", teacherId)
            .putString("teacher_dept", department)
            .apply()

        _teacherProfile.value = TeacherProfile(
            id = teacherId,
            name = name,
            department = department,
            college = "Shyam Lal College (DU)",
            designation = "Faculty Member",
            isRegistered = true
        )
        _isTeacherRegistered.value = true
    }

    fun signoutTeacher() {
        prefs.edit()
            .putBoolean("is_teacher_registered", false)
            .apply()
        _isTeacherRegistered.value = false
        _teacherProfile.value = _teacherProfile.value.copy(isRegistered = false)
    }

    private fun loadProfile(): UserProfile {
        return UserProfile(
            studentName = prefs.getString("student_name", "SLC Student") ?: "SLC Student",
            rollNo = prefs.getString("roll_no", "SLC/2024/001") ?: "SLC/2024/001",
            course = prefs.getString("course", "B.Sc (Hons) Computer Science") ?: "B.Sc (Hons) Computer Science",
            college = "Shyam Lal College (DU)",
            semester = prefs.getString("semester", "Semester 3") ?: "Semester 3",
            targetCriteria = prefs.getFloat("target_criteria", 75.0f).toDouble()
        )
    }

    fun updateProfile(name: String, roll: String, course: String, semester: String, target: Double) {
        prefs.edit()
            .putString("student_name", name)
            .putString("roll_no", roll)
            .putString("course", course)
            .putString("semester", semester)
            .putFloat("target_criteria", target.toFloat())
            .apply()

        _userProfile.value = UserProfile(
            studentName = name,
            rollNo = roll,
            course = course,
            college = "Shyam Lal College (DU)",
            semester = semester,
            targetCriteria = target
        )
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun markAttendance(subjectId: Long, status: String, remark: String = "") {
        viewModelScope.launch {
            repository.markAttendance(
                subjectId = subjectId,
                date = _selectedDate.value,
                status = status,
                remark = remark
            )
        }
    }

    fun quickIncrement(subjectId: Long, isPresent: Boolean) {
        viewModelScope.launch {
            repository.quickIncrementAttendance(subjectId, isPresent, _selectedDate.value)
        }
    }

    fun addSubject(name: String, code: String, teacher: String, room: String, target: Double, colorHex: String) {
        viewModelScope.launch {
            repository.insertSubject(
                Subject(
                    name = name.trim(),
                    code = code.trim(),
                    teacher = teacher.trim(),
                    room = room.trim(),
                    targetPercentage = target,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun deleteRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAll()
            repository.prepopulateShyamLalCollegeDefaults()
        }
    }

    // ==========================================
    // TEACHER PORTAL OPERATIONS
    // ==========================================

    fun selectActiveTeacherClass(teacherClass: TeacherClass?) {
        _activeTeacherClass.value = teacherClass
        if (teacherClass != null) {
            viewModelScope.launch {
                teacherRepository.getStudentsForClass(teacherClass.id).collect { students ->
                    _activeClassStudents.value = students
                }
            }
            viewModelScope.launch {
                teacherRepository.getAttendanceForClass(teacherClass.id).collect { records ->
                    _activeClassAttendanceRecords.value = records
                }
            }
        } else {
            _activeClassStudents.value = emptyList()
            _activeClassAttendanceRecords.value = emptyList()
        }
    }

    fun createTeacherClass(
        className: String,
        courseCode: String,
        department: String,
        semester: String,
        section: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val newClass = TeacherClass(
                className = className,
                courseCode = courseCode,
                department = department,
                semester = semester,
                section = section,
                colorHex = colorHex
            )
            val newId = teacherRepository.insertTeacherClass(newClass)
            selectActiveTeacherClass(newClass.copy(id = newId))
        }
    }

    fun updateTeacherClass(teacherClass: TeacherClass) {
        viewModelScope.launch {
            teacherRepository.updateTeacherClass(teacherClass)
            if (_activeTeacherClass.value?.id == teacherClass.id) {
                _activeTeacherClass.value = teacherClass
            }
        }
    }

    fun deleteTeacherClass(teacherClass: TeacherClass) {
        viewModelScope.launch {
            teacherRepository.deleteTeacherClass(teacherClass)
            if (_activeTeacherClass.value?.id == teacherClass.id) {
                selectActiveTeacherClass(null)
            }
        }
    }

    fun addStudentToActiveSingle(name: String, rollNo: String, email: String = "", initialStatus: String? = null) {
        val activeClass = _activeTeacherClass.value ?: return
        viewModelScope.launch {
            val newStudentId = teacherRepository.insertStudent(
                ClassStudent(
                    classId = activeClass.id,
                    studentName = name,
                    rollNo = rollNo,
                    email = email
                )
            )
            if (initialStatus != null && newStudentId > 0) {
                teacherRepository.markStudentAttendance(
                    classId = activeClass.id,
                    studentId = newStudentId,
                    date = _selectedDate.value,
                    status = initialStatus
                )
            }
        }
    }

    fun addStudentsToActiveBulk(parsedList: List<Pair<String, String>>) {
        val activeClass = _activeTeacherClass.value ?: return
        viewModelScope.launch {
            val students = parsedList.map { (roll, name) ->
                ClassStudent(
                    classId = activeClass.id,
                    studentName = name,
                    rollNo = roll
                )
            }
            teacherRepository.insertStudents(students)
        }
    }

    fun deleteStudentFromClass(student: ClassStudent) {
        viewModelScope.launch {
            teacherRepository.deleteStudent(student)
        }
    }

    fun saveActiveAttendanceBatch(statusMap: Map<Long, String>) {
        val activeClass = _activeTeacherClass.value ?: return
        viewModelScope.launch {
            teacherRepository.markBulkAttendanceForDate(
                classId = activeClass.id,
                date = _selectedDate.value,
                statusMap = statusMap
            )
        }
    }

    fun exportActiveTeacherClassToExcel(context: Context, teacherClass: TeacherClass? = null) {
        val targetClass = teacherClass ?: _activeTeacherClass.value ?: return
        viewModelScope.launch {
            val (cls, students, records) = teacherRepository.getClassDataForExport(targetClass.id)
            if (cls != null) {
                val csv = ExcelExportUtil.generateClassAttendanceCsv(cls, students, records)
                ExcelExportUtil.shareOrDownloadExcel(context, cls, csv)
            }
        }
    }

    fun shareClassAttendanceToWhatsApp(context: Context, teacherClass: TeacherClass? = null) {
        val targetClass = teacherClass ?: _activeTeacherClass.value ?: return
        viewModelScope.launch {
            val (cls, students, records) = teacherRepository.getClassDataForExport(targetClass.id)
            if (cls != null) {
                val csv = ExcelExportUtil.generateClassAttendanceCsv(cls, students, records)
                ExcelExportUtil.shareClassAttendanceToWhatsApp(context, cls, csv)
            }
        }
    }

    fun loadShyamLalFirstYearRoster() {
        viewModelScope.launch {
            val classId = teacherRepository.loadShyamLalCollegeFirstYearClass()
            // Select this newly loaded class so it immediately opens for the teacher
            val cls = teacherRepository.allTeacherClasses.first().find { it.id == classId }
            if (cls != null) {
                selectActiveTeacherClass(cls)
            }
        }
    }

    fun loadAllThreeYearsClasses() {
        viewModelScope.launch {
            val ids = teacherRepository.loadShyamLalCollegeAllYearsClasses()
            if (ids.isNotEmpty()) {
                val cls = teacherRepository.allTeacherClasses.first().find { it.id == ids[0] }
                if (cls != null) {
                    selectActiveTeacherClass(cls)
                }
            }
        }
    }

    fun importAndSelectSectionSheet(year: String, courseTabCode: String, onSelected: ((TeacherClass) -> Unit)? = null) {
        viewModelScope.launch {
            val classId = teacherRepository.importSectionSheetToTeacherClasses(year, courseTabCode)
            val cls = teacherRepository.allTeacherClasses.first().find { it.id == classId }
            if (cls != null) {
                selectActiveTeacherClass(cls)
                onSelected?.invoke(cls)
            }
        }
    }

    fun generateAttendanceReport(): String {
        val profile = _userProfile.value
        val overall = overallStats.value
        val subjects = subjectsWithStats.value

        val builder = StringBuilder()
        builder.append("=========================================\n")
        builder.append("  CAMPUSCLOCK - ATTENDANCE REPORT        \n")
        builder.append("  Shyam Lal College, University of Delhi \n")
        builder.append("=========================================\n\n")
        builder.append("Student: ${profile.studentName}\n")
        builder.append("Roll No: ${profile.rollNo}\n")
        builder.append("Course: ${profile.course} (${profile.semester})\n")
        builder.append("Report Date: ${dateFormat.format(Date())}\n")
        builder.append("Target Criteria: ${profile.targetCriteria}%\n")
        builder.append("-----------------------------------------\n")
        builder.append("OVERALL SUMMARY:\n")
        builder.append("Classes Held: ${overall.totalHeld}\n")
        builder.append("Classes Attended: ${overall.totalAttended}\n")
        builder.append("Classes Absent: ${overall.totalAbsent}\n")
        builder.append("Overall Percentage: ${String.format(Locale.US, "%.1f", overall.percentage)}%\n")
        builder.append("DU Eligibility: ${if (overall.isEligible) "ELIGIBLE (>= ${profile.targetCriteria}%)" else "SHORTAGE (< ${profile.targetCriteria}%)"}\n")
        if (overall.safeBunks > 0) {
            builder.append("Safe Bunks Available: ${overall.safeBunks} classes\n")
        } else if (overall.classesNeeded > 0) {
            builder.append("Classes Needed to Attend: ${overall.classesNeeded} classes\n")
        }
        builder.append("\n-----------------------------------------\n")
        builder.append("SUBJECT-WISE BREAKDOWN:\n")
        subjects.forEach { s ->
            builder.append("\n• ${s.subject.name} (${s.subject.code})\n")
            builder.append("  Teacher: ${s.subject.teacher.ifEmpty { "N/A" }} | Room: ${s.subject.room.ifEmpty { "N/A" }}\n")
            builder.append("  Attended: ${s.totalAttended} / ${s.totalHeld} (${String.format(Locale.US, "%.1f", s.percentage)}%)\n")
            builder.append("  Target: ${s.subject.targetPercentage}%\n")
            if (s.safeBunks > 0) {
                builder.append("  Advice: Can safely bunk ${s.safeBunks} class(es)\n")
            } else if (s.classesNeeded > 0) {
                builder.append("  Advice: Must attend next ${s.classesNeeded} class(es)\n")
            } else {
                builder.append("  Advice: On track\n")
            }
        }
        builder.append("\n=========================================\n")
        builder.append("100% Offline & Private | CampusClock SLC DU\n")
        return builder.toString()
    }

    // ==========================================
    // DIRECT / INSTANT ATTENDANCE (NO CLASS REQUIRED)
    // Teacher ya CR bina class banaye direct roll number search & mark kar sakte hain
    // ==========================================

    private val _directStudents = MutableStateFlow<List<DirectStudentEntry>>(emptyList())
    val directStudents: StateFlow<List<DirectStudentEntry>> = _directStudents.asStateFlow()

    /**
     * Searches college directory by roll number.
     * If found in college records -> auto-adds the student with their name as Present!
     * If not found and manualName is provided -> adds with manual details!
     * Returns true if student was added, false if student was not in directory and manual name is required.
     */
    fun addDirectStudentByRoll(
        rollNo: String,
        manualName: String? = null,
        initialStatus: String = StudentAttendanceRecord.STATUS_PRESENT
    ): Boolean {
        val cleanRoll = rollNo.trim().uppercase()
        if (cleanRoll.isEmpty()) return false

        val currentList = _directStudents.value.toMutableList()

        // Check if already in current direct list
        val existingIndex = currentList.indexOfFirst { it.rollNo.uppercase() == cleanRoll }
        if (existingIndex >= 0) {
            currentList[existingIndex] = currentList[existingIndex].copy(status = initialStatus)
            _directStudents.value = currentList
            return true
        }

        // 1. Search official college directory for auto-add
        val collegeStudent = CollegeRosterDirectory.findStudentByRoll(cleanRoll)
        if (collegeStudent != null) {
            currentList.add(
                DirectStudentEntry(
                    rollNo = collegeStudent.rollNo,
                    studentName = collegeStudent.name,
                    status = initialStatus
                )
            )
            _directStudents.value = currentList
            return true
        }

        // 2. If not found in directory, require manual name
        val trimmedManual = manualName?.trim().orEmpty()
        if (trimmedManual.isNotEmpty()) {
            currentList.add(
                DirectStudentEntry(
                    rollNo = cleanRoll,
                    studentName = trimmedManual,
                    status = initialStatus
                )
            )
            _directStudents.value = currentList
            return true
        }

        return false
    }

    fun addDirectStudentItem(
        student: CollegeStudentItem,
        initialStatus: String = StudentAttendanceRecord.STATUS_PRESENT
    ) {
        val currentList = _directStudents.value.toMutableList()
        val cleanRoll = student.rollNo.trim().uppercase()
        val existingIndex = currentList.indexOfFirst { it.rollNo.uppercase() == cleanRoll }
        if (existingIndex >= 0) {
            currentList[existingIndex] = currentList[existingIndex].copy(status = initialStatus)
        } else {
            currentList.add(
                DirectStudentEntry(
                    rollNo = student.rollNo,
                    studentName = student.name,
                    status = initialStatus
                )
            )
        }
        _directStudents.value = currentList
    }

    fun updateDirectStudentStatus(rollNo: String, newStatus: String) {
        val cleanRoll = rollNo.trim().uppercase()
        val currentList = _directStudents.value.toMutableList()
        val idx = currentList.indexOfFirst { it.rollNo.uppercase() == cleanRoll }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(status = newStatus)
            _directStudents.value = currentList
        }
    }

    fun removeDirectStudent(rollNo: String) {
        val cleanRoll = rollNo.trim().uppercase()
        _directStudents.value = _directStudents.value.filterNot { it.rollNo.uppercase() == cleanRoll }
    }

    fun markAllDirectStudents(status: String) {
        _directStudents.value = _directStudents.value.map { it.copy(status = status) }
    }

    fun clearDirectAttendance() {
        _directStudents.value = emptyList()
    }

    /**
     * Persists Direct Attendance into Room database as a saved class session
     */
    fun saveDirectAttendanceToDatabase(
        context: Context,
        lectureTitle: String,
        date: String,
        onSaved: (Boolean) -> Unit
    ) {
        val students = _directStudents.value
        if (students.isEmpty()) {
            Toast.makeText(context, "No students to save!", Toast.LENGTH_SHORT).show()
            onSaved(false)
            return
        }

        viewModelScope.launch {
            try {
                val cleanTitle = lectureTitle.ifBlank { "Direct Attendance" }
                // Create or find class
                val newClass = TeacherClass(
                    className = cleanTitle,
                    courseCode = "DIRECT",
                    department = _teacherProfile.value.department.ifBlank { "General" },
                    semester = "Direct Session",
                    section = "General",
                    colorHex = "#1E3A8A"
                )
                val classId = teacherRepository.insertTeacherClass(newClass)

                val classStudents = students.map { s ->
                    ClassStudent(
                        classId = classId,
                        studentName = s.studentName,
                        rollNo = s.rollNo
                    )
                }
                val insertedIds = teacherRepository.insertStudents(classStudents)

                val statusMap = mutableMapOf<Long, String>()
                insertedIds.forEachIndexed { index, studentId ->
                    if (index < students.size) {
                        statusMap[studentId] = students[index].status
                    }
                }
                teacherRepository.markBulkAttendanceForDate(classId, date, statusMap)

                Toast.makeText(context, "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
                onSaved(true)
            } catch (e: Exception) {
                Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                onSaved(false)
            }
        }
    }

    /**
     * Formats and shares attendance summary directly to WhatsApp
     */
    fun shareDirectAttendanceWhatsApp(
        context: Context,
        lectureTitle: String,
        date: String
    ) {
        val students = _directStudents.value
        if (students.isEmpty()) {
            Toast.makeText(context, "No attendance records to share!", Toast.LENGTH_SHORT).show()
            return
        }

        val presentList = students.filter { it.status == StudentAttendanceRecord.STATUS_PRESENT }
        val absentList = students.filter { it.status == StudentAttendanceRecord.STATUS_ABSENT }
        val leaveList = students.filter { it.status == StudentAttendanceRecord.STATUS_LEAVE }

        val cleanTitle = lectureTitle.ifBlank { "Direct Roll Call" }
        val pct = if (students.isNotEmpty()) (presentList.size * 100.0 / students.size) else 0.0

        val sb = StringBuilder()
        sb.append("📋 *ATTENDANCE REPORT*\n")
        sb.append("🏛 *Shyam Lal College (DU)*\n")
        sb.append("📖 *Lecture/Subject:* $cleanTitle\n")
        sb.append("📅 *Date:* $date\n")
        sb.append("👤 *Taken By:* ${_teacherProfile.value.name.ifBlank { "Faculty / CR" }}\n")
        sb.append("------------------------------------\n")
        sb.append("👥 *Total:* ${students.size} | ✅ *Present:* ${presentList.size} | ❌ *Absent:* ${absentList.size}")
        if (leaveList.isNotEmpty()) {
            sb.append(" | 🟡 *Leave:* ${leaveList.size}")
        }
        sb.append(" (${String.format(Locale.US, "%.1f", pct)}%)\n")
        sb.append("------------------------------------\n\n")

        if (absentList.isNotEmpty()) {
            sb.append("❌ *ABSENT STUDENTS (${absentList.size}):*\n")
            absentList.forEachIndexed { i, s ->
                sb.append("${i + 1}. *${s.rollNo}* - ${s.studentName}\n")
            }
            sb.append("\n")
        }

        sb.append("✅ *PRESENT STUDENTS (${presentList.size}):*\n")
        if (presentList.isEmpty()) {
            sb.append("None\n")
        } else {
            presentList.forEachIndexed { i, s ->
                sb.append("${i + 1}. ${s.rollNo} - ${s.studentName}\n")
            }
        }

        if (leaveList.isNotEmpty()) {
            sb.append("\n🟡 *ON LEAVE (${leaveList.size}):*\n")
            leaveList.forEachIndexed { i, s ->
                sb.append("${i + 1}. ${s.rollNo} - ${s.studentName}\n")
            }
        }

        sb.append("\n_Shared via CampusClock SLC DU_")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(sendIntent)
        } catch (_: Exception) {
            // If WhatsApp is not installed, open standard chooser
            val chooser = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                },
                "Share Attendance Summary"
            )
            context.startActivity(chooser)
        }
    }

    fun exportDirectAttendanceToExcel(
        context: Context,
        lectureTitle: String,
        date: String
    ) {
        val students = _directStudents.value
        if (students.isEmpty()) {
            Toast.makeText(context, "No attendance records to export!", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanTitle = lectureTitle.ifBlank { "Direct Attendance" }
        val csv = ExcelExportUtil.generateDirectAttendanceCsv(
            lectureTitle = cleanTitle,
            date = date,
            students = students
        )
        val tempClass = TeacherClass(
            className = cleanTitle,
            courseCode = "SLC",
            department = "DU",
            semester = "Sem 1",
            section = "A"
        )
        val uri = ExcelExportUtil.shareOrDownloadExcel(
            context = context,
            teacherClass = tempClass,
            csvContent = csv,
            fileSuffix = date,
            rangeLabel = date
        )
        if (uri != null) {
            Toast.makeText(context, "Excel sheet created for $date", Toast.LENGTH_SHORT).show()
        }
    }
}
