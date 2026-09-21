package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.floor

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSafeBunkCalculation() {
    val attended = 18
    val held = 20
    val targetFrac = 0.75
    val maxPossibleHeld = floor(attended / targetFrac).toInt()
    val safeBunks = (maxPossibleHeld - held).coerceAtLeast(0)
    assertEquals(4, safeBunks)

    // After 4 misses, percentage is still >= 75%
    val newPercentage = (attended.toDouble() / (held + safeBunks)) * 100.0
    assertTrue(newPercentage >= 75.0)
  }

  @Test
  fun testClassesNeededCalculation() {
    val attended = 10
    val held = 20
    val targetFrac = 0.75
    val classesNeeded = ceil((targetFrac * held - attended) / (1.0 - targetFrac)).toInt()
    assertEquals(20, classesNeeded)

    // After 20 attended, percentage hits 75%
    val newPercentage = ((attended + classesNeeded).toDouble() / (held + classesNeeded)) * 100.0
    assertTrue(newPercentage >= 75.0)
  }

  @Test
  fun testExcelCsvGenerationFormat() {
    val teacherClass = com.example.data.TeacherClass(
      id = 1L,
      className = "Data Structures",
      courseCode = "CS-301",
      department = "Computer Science",
      semester = "Semester 3",
      section = "A"
    )
    val students = listOf(
      com.example.data.ClassStudent(id = 101L, classId = 1L, studentName = "Aarav Sharma", rollNo = "01"),
      com.example.data.ClassStudent(id = 102L, classId = 1L, studentName = "Aditi Verma", rollNo = "02")
    )
    val records = listOf(
      com.example.data.StudentAttendanceRecord(id = 1L, classId = 1L, studentId = 101L, date = "2026-09-10", status = "PRESENT"),
      com.example.data.StudentAttendanceRecord(id = 2L, classId = 1L, studentId = 102L, date = "2026-09-10", status = "ABSENT"),
      com.example.data.StudentAttendanceRecord(id = 3L, classId = 1L, studentId = 101L, date = "2026-09-11", status = "PRESENT"),
      com.example.data.StudentAttendanceRecord(id = 4L, classId = 1L, studentId = 102L, date = "2026-09-11", status = "PRESENT")
    )

    val csv = com.example.util.ExcelExportUtil.generateClassAttendanceCsv(teacherClass, students, records)

    // Verify CSV headers and structure
    assertTrue(csv.contains("ATTENDANCE REGISTER - SHYAM LAL COLLEGE"))
    assertTrue(csv.contains("Data Structures"))
    assertTrue(csv.contains("CS-301"))
    assertTrue(csv.contains("Aarav Sharma"))
    assertTrue(csv.contains("Aditi Verma"))
    assertTrue(csv.contains("2026-09-10"))
    assertTrue(csv.contains("2026-09-11"))
    assertTrue(csv.contains("Total Present"))
    assertTrue(csv.contains("Attendance %"))
  }

  @Test
  fun testFirstYearStudentsRosterDetails() {
    val students = listOf(
      "260001" to "STANZIN CHOSPAL",
      "260003" to "UTKARSH BARI",
      "260004" to "HIMANSHU",
      "260005" to "DAKSHITA",
      "260007" to "TEJASAV MISHRA",
      "260008" to "ROSHAN RAJ",
      "260010" to "KIRIT PAL",
      "260011" to "MANISH",
      "260015" to "PRIYADARSHINI",
      "260017" to "SARAH ZIA",
      "260020" to "NAMAN LAKRA",
      "260022" to "JIPMO HEYO",
      "260023" to "NEHA",
      "260027" to "VAIBHAV SINGH",
      "260028" to "AQSA PARVEEN",
      "260032" to "NANDANI KUMARI",
      "260033" to "ABHINANDAN SINGH",
      "260035" to "YUVRAJ",
      "260039" to "SHUBHAM KUMAR",
      "260041" to "KASHISH",
      "260042" to "SHEKHAR SINGH",
      "260043" to "MANSI YADAV",
      "260045" to "AARAV PANDEY",
      "260046" to "ADITYA CHADHA",
      "260048" to "HIMANSHU GAUTAM",
      "260050" to "MOHD HAMMAD",
      "260051" to "KARTIKEY CHAUHAN",
      "260053" to "RANDEEP TEWATIA",
      "260054" to "H S NIRANJAN",
      "260055" to "TAMANNA",
      "260058" to "WASHIFA SAIFI",
      "260059" to "SANDEEP PAL",
      "260060" to "MOHD AAMIR CHOUDHARY",
      "260061" to "NISHANT YADAV",
      "260062" to "MOHIT YADAV"
    )

    assertEquals(35, students.size)
    assertEquals("STANZIN CHOSPAL", students.first().second)
    assertEquals("260001", students.first().first)
    assertEquals("MOHIT YADAV", students.last().second)
    assertEquals("260062", students.last().first)
  }

  @Test
  fun testFacultyDirectoryAndTimetableSuggestions() {
    val allFaculty = com.example.data.FacultyDirectory.allFaculty
    assertTrue(allFaculty.size >= 140)

    // Test specific faculty from Timetable Print System
    val majumdar = allFaculty.find { it.id == 413 }
    assertNotNull(majumdar)
    assertEquals("Dr. Kinshuk Majumdar", majumdar?.name)
    assertEquals("Chemistry", majumdar?.department)

    val reeta = allFaculty.find { it.id == 201 }
    assertNotNull(reeta)
    assertEquals("Dr. Reeta Sharma", reeta?.name)

    val seema = allFaculty.find { it.id == 131 }
    assertNotNull(seema)
    assertEquals("Dr. Seema Guglani", seema?.name)
    assertEquals("Mathematics", seema?.department)

    val srinivas = allFaculty.find { it.id == 72 }
    assertNotNull(srinivas)
    assertEquals("Dr. Srinivas Misra", srinivas?.name)
    assertEquals("Political Science", srinivas?.department)

    val manila = allFaculty.find { it.id == 407 }
    assertNotNull(manila)
    assertEquals("Ms. Manila Kohli", manila?.name)
    assertEquals("Economics", manila?.department)

    // Search query test
    val searchResults = com.example.data.FacultyDirectory.searchFaculty("Kinshuk")
    assertTrue(searchResults.any { it.name.contains("Kinshuk") })

    val idSearchResults = com.example.data.FacultyDirectory.searchFaculty("413")
    assertEquals(1, idSearchResults.size)
    assertEquals(413, idSearchResults[0].id)
  }

  @Test
  fun testWhatsAppShareUtilFormatting() {
    val teacherClass = com.example.data.TeacherClass(
      id = 10L,
      className = "B.Sc (H) Computer Science",
      courseCode = "CS-101",
      department = "Computer Science",
      semester = "Sem 1",
      section = "A"
    )
    val students = listOf(
      com.example.data.ClassStudent(id = 1L, classId = 10L, studentName = "Aarav Sharma", rollNo = "260001"),
      com.example.data.ClassStudent(id = 2L, classId = 10L, studentName = "Aditi Verma", rollNo = "260002"),
      com.example.data.ClassStudent(id = 3L, classId = 10L, studentName = "Rohan Gupta", rollNo = "260003")
    )
    val statusMap = mapOf(
      1L to "PRESENT",
      2L to "ABSENT",
      3L to "PRESENT"
    )

    // 1. Full Report Mode
    val fullReport = com.example.util.WhatsAppShareUtil.buildDailyAttendanceSummary(
      teacherClass = teacherClass,
      selectedDate = "2026-09-20",
      students = students,
      statusMap = statusMap,
      shareMode = com.example.util.ShareFormatMode.FULL_REPORT
    )

    assertTrue(fullReport.contains("B.Sc (H) Computer Science"))
    assertTrue(fullReport.contains("2026-09-20"))
    assertTrue(fullReport.contains("260001"))
    assertTrue(fullReport.contains("Aarav Sharma"))
    assertTrue(fullReport.contains("260002"))
    assertTrue(fullReport.contains("Aditi Verma"))
    assertTrue(fullReport.contains("ABSENT STUDENTS"))
    assertTrue(fullReport.contains("PRESENT STUDENTS"))

    // 2. Absent Only Mode
    val absentReport = com.example.util.WhatsAppShareUtil.buildDailyAttendanceSummary(
      teacherClass = teacherClass,
      selectedDate = "2026-09-20",
      students = students,
      statusMap = statusMap,
      shareMode = com.example.util.ShareFormatMode.ABSENT_ONLY
    )

    assertTrue(absentReport.contains("B.Sc (H) Computer Science"))
    assertTrue(absentReport.contains("260002"))
    assertTrue(absentReport.contains("Aditi Verma"))
    assertFalse(absentReport.contains("PRESENT STUDENTS"))
  }

  @Test
  fun testExcelDateRangeExport() {
    val teacherClass = com.example.data.TeacherClass(
      id = 5L,
      className = "Discrete Mathematics",
      courseCode = "MATH-201",
      department = "Mathematics",
      semester = "Sem 3",
      section = "B"
    )
    val students = listOf(
      com.example.data.ClassStudent(id = 11L, classId = 5L, studentName = "Kavita Rao", rollNo = "240011"),
      com.example.data.ClassStudent(id = 12L, classId = 5L, studentName = "Dev Patel", rollNo = "240012")
    )
    val records = listOf(
      com.example.data.StudentAttendanceRecord(id = 1L, classId = 5L, studentId = 11L, date = "2026-08-15", status = "PRESENT"),
      com.example.data.StudentAttendanceRecord(id = 2L, classId = 5L, studentId = 12L, date = "2026-08-15", status = "ABSENT"),
      com.example.data.StudentAttendanceRecord(id = 3L, classId = 5L, studentId = 11L, date = "2026-09-01", status = "PRESENT"),
      com.example.data.StudentAttendanceRecord(id = 4L, classId = 5L, studentId = 12L, date = "2026-09-01", status = "PRESENT"),
      com.example.data.StudentAttendanceRecord(id = 5L, classId = 5L, studentId = 11L, date = "2026-09-15", status = "ABSENT"),
      com.example.data.StudentAttendanceRecord(id = 6L, classId = 5L, studentId = 12L, date = "2026-09-15", status = "PRESENT")
    )

    // Test month extraction
    val months = com.example.util.ExcelExportUtil.extractAvailableMonths(records)
    assertEquals(2, months.size)
    assertTrue(months.any { it.label.contains("Aug") })
    assertTrue(months.any { it.label.contains("Sep") })

    // Test filtering by September 2026 range
    val sepCsv = com.example.util.ExcelExportUtil.generateClassAttendanceCsv(
      teacherClass = teacherClass,
      students = students,
      attendanceRecords = records,
      startDate = "2026-09-01",
      endDate = "2026-09-30",
      rangeLabel = "September 2026"
    )

    assertTrue(sepCsv.contains("Discrete Mathematics"))
    assertTrue(sepCsv.contains("Attendance Period / Date Range,September 2026"))
    assertTrue(sepCsv.contains("2026-09-01"))
    assertTrue(sepCsv.contains("2026-09-15"))
    // August date should NOT be included in September CSV columns
    assertFalse(sepCsv.contains("2026-08-15"))
    assertTrue(sepCsv.contains("Kavita Rao"))
    assertTrue(sepCsv.contains("Dev Patel"))
  }

  @Test
  fun testSearchAllCombinationsAndYears() {
    // 1. Verify all indexed students collection is populated across all 3 years and 18 course tabs
    val indexed = com.example.data.CollegeRosterDirectory.allIndexedStudents
    assertTrue(indexed.size > 1000)

    // 2. Test search by specific roll number (e.g. 240402 - Shambhawi Srivastava in 3rd Year EC+OM)
    val rollResults = com.example.data.CollegeRosterDirectory.searchAllCombinationsAndYears("240402")
    assertTrue(rollResults.isNotEmpty())
    val match = rollResults.first()
    assertEquals("240402", match.student.rollNo)
    assertEquals("Shambhawi Srivastava", match.student.name)
    assertEquals("3rd Year", match.year)
    assertEquals("EC+OM", match.courseCode)

    // 3. Test search by student name across combinations and years (e.g. "Yadav" or "Mohit")
    val nameResults = com.example.data.CollegeRosterDirectory.searchAllCombinationsAndYears("Yadav")
    assertTrue(nameResults.size >= 5)
    assertTrue(nameResults.any { it.student.name.contains("YADAV", ignoreCase = true) })

    // 4. Test search with Year Filter
    val firstYearOnly = com.example.data.CollegeRosterDirectory.searchAllCombinationsAndYears("Aarav", yearFilter = "1st Year")
    firstYearOnly.forEach {
      assertEquals("1st Year", it.year)
    }

    // 5. Test search for 1st year roll number prefix (e.g. "2600")
    val firstYearRolls = com.example.data.CollegeRosterDirectory.searchAllCombinationsAndYears("2600")
    assertTrue(firstYearRolls.isNotEmpty())
    assertTrue(firstYearRolls.all { it.student.rollNo.startsWith("2600") })
  }
}

