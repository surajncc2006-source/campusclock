package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class TeacherRepository(private val teacherDao: TeacherDao) {

    val allTeacherClasses: Flow<List<TeacherClass>> = teacherDao.getAllTeacherClasses()

    fun getStudentsForClass(classId: Long): Flow<List<ClassStudent>> {
        return teacherDao.getStudentsForClass(classId)
    }

    fun getAttendanceForClass(classId: Long): Flow<List<StudentAttendanceRecord>> {
        return teacherDao.getAttendanceForClass(classId)
    }

    fun getAttendanceForClassAndDate(classId: Long, date: String): Flow<List<StudentAttendanceRecord>> {
        return teacherDao.getAttendanceForClassAndDate(classId, date)
    }

    /**
     * Flow providing teacher classes with live student count & sessions held
     */
    fun getClassesWithStudentCounts(): Flow<List<ClassWithStudentCount>> {
        return combine(
            teacherDao.getAllTeacherClasses(),
            teacherDao.getAllStudents()
        ) { classes, allStudents ->
            classes.map { tc ->
                val studentsInClass = allStudents.filter { it.classId == tc.id }
                ClassWithStudentCount(
                    teacherClass = tc,
                    studentCount = studentsInClass.size,
                    totalSessionsHeld = 0, // updated dynamically or calculated in details
                    averageAttendancePercentage = 0.0
                )
            }
        }
    }

    suspend fun insertTeacherClass(teacherClass: TeacherClass): Long = withContext(Dispatchers.IO) {
        teacherDao.insertTeacherClass(teacherClass)
    }

    suspend fun updateTeacherClass(teacherClass: TeacherClass) = withContext(Dispatchers.IO) {
        teacherDao.updateTeacherClass(teacherClass)
    }

    suspend fun deleteTeacherClass(teacherClass: TeacherClass) = withContext(Dispatchers.IO) {
        teacherDao.deleteTeacherClass(teacherClass)
    }

    suspend fun insertStudent(student: ClassStudent): Long = withContext(Dispatchers.IO) {
        teacherDao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<ClassStudent>): List<Long> = withContext(Dispatchers.IO) {
        teacherDao.insertStudents(students)
    }

    suspend fun updateStudent(student: ClassStudent) = withContext(Dispatchers.IO) {
        teacherDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: ClassStudent) = withContext(Dispatchers.IO) {
        teacherDao.deleteStudent(student)
    }

    suspend fun markStudentAttendance(
        classId: Long,
        studentId: Long,
        date: String,
        status: String,
        remark: String = ""
    ) = withContext(Dispatchers.IO) {
        teacherDao.insertAttendanceRecord(
            StudentAttendanceRecord(
                classId = classId,
                studentId = studentId,
                date = date,
                status = status,
                remark = remark
            )
        )
    }

    suspend fun markBulkAttendanceForDate(
        classId: Long,
        date: String,
        statusMap: Map<Long, String>
    ) = withContext(Dispatchers.IO) {
        val records = statusMap.map { (studentId, status) ->
            StudentAttendanceRecord(
                classId = classId,
                studentId = studentId,
                date = date,
                status = status
            )
        }
        teacherDao.insertAttendanceRecords(records)
    }

    suspend fun getClassDataForExport(classId: Long): Triple<TeacherClass?, List<ClassStudent>, List<StudentAttendanceRecord>> = withContext(Dispatchers.IO) {
        val cls = teacherDao.getTeacherClassById(classId)
        val students = teacherDao.getStudentsForClassSync(classId)
        val records = teacherDao.getAttendanceForClassSync(classId)
        Triple(cls, students, records)
    }

    suspend fun prepopulateDefaultTeacherClassIfEmpty() = withContext(Dispatchers.IO) {
        // Class 1: 1st Year (2026-27) - B.A. (Prog.) (Economics + Political Science) Section A (35 students)
        val baProgClassId = teacherDao.insertTeacherClass(
            TeacherClass(
                className = "B.A. (Prog.) Economics + Political Science",
                courseCode = "EC+PS",
                department = "Economics & Political Science",
                semester = "1st Year (Sem 1)",
                section = "Section-A",
                academicYear = "2026-2027",
                colorHex = "#1565C0"
            )
        )

        val firstYearStudents = CollegeRosterDirectory.firstYearEcPsStudents.map {
            ClassStudent(classId = baProgClassId, studentName = it.name, rollNo = it.rollNo)
        }
        teacherDao.insertStudents(firstYearStudents)

        // Class 2: 2nd Year (2026-27) - B.A. (Prog.) (Economics + OMSP) Section A (20 students)
        val secondYrClassId = teacherDao.insertTeacherClass(
            TeacherClass(
                className = "B.A. (Prog.) Economics + OMSP",
                courseCode = "EC+OM",
                department = "Economics & OMSP",
                semester = "2nd Year (Sem 3)",
                section = "Section-A",
                academicYear = "2026-2027",
                colorHex = "#880E4F"
            )
        )
        val secondYearStudents = CollegeRosterDirectory.secondYearEcOmStudents.map {
            ClassStudent(classId = secondYrClassId, studentName = it.name, rollNo = it.rollNo)
        }
        teacherDao.insertStudents(secondYearStudents)

        // Class 3: 3rd Year (2026-27) - B.A. (Prog.) (Economics + OMSP) Section A (18 students)
        val thirdYrClassId = teacherDao.insertTeacherClass(
            TeacherClass(
                className = "B.A. (Prog.) Economics + OMSP",
                courseCode = "EC+OM",
                department = "Economics & OMSP",
                semester = "3rd Year (Sem 5)",
                section = "Section-A",
                academicYear = "2026-2027",
                colorHex = "#00695C"
            )
        )
        val thirdYearStudents = CollegeRosterDirectory.thirdYearEcOmStudents.map {
            ClassStudent(classId = thirdYrClassId, studentName = it.name, rollNo = it.rollNo)
        }
        teacherDao.insertStudents(thirdYearStudents)
    }

    /**
     * Loads or imports any Section Sheet from the 18 Course Tabs & 1st/2nd/3rd Year into active teacher classes
     */
    suspend fun importSectionSheetToTeacherClasses(year: String, courseTabCode: String): Long = withContext(Dispatchers.IO) {
        val roster = CollegeRosterDirectory.getRoster(year, courseTabCode)
        val courseInfo = CollegeRosterDirectory.courseTabs.find { it.code == courseTabCode } 
            ?: CollegeRosterDirectory.courseTabs[0]

        val classId = teacherDao.insertTeacherClass(
            TeacherClass(
                className = courseInfo.fullName,
                courseCode = courseTabCode,
                department = courseInfo.department,
                semester = "$year (2026-27)",
                section = roster.section,
                academicYear = roster.academicYear,
                colorHex = courseInfo.colorHex
            )
        )

        val students = roster.students.map {
            ClassStudent(classId = classId, studentName = it.name, rollNo = it.rollNo)
        }
        teacherDao.insertStudents(students)
        classId
    }

    /**
     * Ensures all 1st, 2nd, and 3rd year classes are loaded and available
     */
    suspend fun loadShyamLalCollegeAllYearsClasses(): List<Long> = withContext(Dispatchers.IO) {
        val existing = teacherDao.getAllTeacherClassesSync()
        val ids = mutableListOf<Long>()

        // Check if 1st Year EC+PS exists
        var c1 = existing.find { it.semester.contains("1") && it.courseCode.contains("EC+PS") }
        if (c1 == null) {
            val id1 = importSectionSheetToTeacherClasses("1st Year", "EC+PS")
            ids.add(id1)
        } else {
            ids.add(c1.id)
        }

        // Check if 2nd Year EC+OM exists
        var c2 = existing.find { it.semester.contains("2") && it.courseCode.contains("EC+OM") }
        if (c2 == null) {
            val id2 = importSectionSheetToTeacherClasses("2nd Year", "EC+OM")
            ids.add(id2)
        } else {
            ids.add(c2.id)
        }

        // Check if 3rd Year EC+OM exists
        var c3 = existing.find { it.semester.contains("3") && it.courseCode.contains("EC+OM") }
        if (c3 == null) {
            val id3 = importSectionSheetToTeacherClasses("3rd Year", "EC+OM")
            ids.add(id3)
        } else {
            ids.add(c3.id)
        }

        ids
    }

    suspend fun loadShyamLalCollegeFirstYearClass(): Long = withContext(Dispatchers.IO) {
        val existing = teacherDao.getAllTeacherClassesSync()
        val c1 = existing.find { it.semester.contains("1") && (it.courseCode.contains("EC+PS") || it.courseCode.contains("SLC-BA-1YR")) }
        if (c1 != null) {
            c1.id
        } else {
            importSectionSheetToTeacherClasses("1st Year", "EC+PS")
        }
    }

    private fun baProgClassIdIfMatch(id: Long) = id
}
