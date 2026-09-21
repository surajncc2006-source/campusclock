package com.example.data

/**
 * Shyam Lal College (University of Delhi), Shahdara, Delhi - 110032
 * Complete Official Section Lists for 1st Year, 2nd Year, and 3rd Year (2026-2027)
 *
 * Course Tabs from Official Excel Workbook "Section List_ III Year (2026-2027)":
 * - EC+OM   (Economics + OMSP)
 * - EC+PS   (Economics + Political Science)
 * - EN+EC   (English + Economics)
 * - EN+PS   (English + Political Science)
 * - HN+HS   (Hindi + History)
 * - HS+PS   (History + Political Science)
 * - EC      (B.A. Hons. Economics)
 * - EN      (B.A. Hons. English)
 * - HN      (B.A. Hons. Hindi)
 * - HS      (B.A. Hons. History)
 * - PS      (B.A. Hons. Political Science)
 * - BCOM    (B.Com Program)
 * - BCH     (B.Com Honours)
 * - CH(H)   (B.Sc Hons. Chemistry)
 * - MT(H)   (B.Sc Hons. Mathematics)
 * - BSC.CH  (B.Sc Phys Sci with Chemistry)
 * - BSC.CS  (B.Sc Phys Sci with Computer Science)
 * - BSC.EL  (B.Sc Phys Sci with Electronics)
 */

data class CourseSectionTab(
    val code: String,
    val fullName: String,
    val department: String,
    val defaultRoom: String,
    val colorHex: String
)

data class CollegeStudentItem(
    val sNo: Int,
    val rollNo: String,
    val name: String,
    val section: String = "Section-A"
)

data class SectionSheet(
    val courseCode: String,
    val courseTitle: String,
    val year: String, // "1st Year", "2nd Year", "3rd Year"
    val academicYear: String, // "2026-2027"
    val section: String, // "Section-A" or "All"
    val availableSections: List<String> = listOf("Section-A"),
    val students: List<CollegeStudentItem>
)

data class GlobalStudentSearchResult(
    val student: CollegeStudentItem,
    val year: String,
    val courseCode: String,
    val courseTitle: String,
    val department: String,
    val section: String = "Section-A",
    val defaultRoom: String = "Room 101"
)

object CollegeRosterDirectory {

    val courseTabs = listOf(
        CourseSectionTab("EC+OM", "B.A. (Prog.) Economics + OMSP", "Economics & OMSP", "Room 102", "#880E4F"),
        CourseSectionTab("EC+PS", "B.A. (Prog.) Economics + Political Science", "Economics & Pol Science", "Room 105", "#1565C0"),
        CourseSectionTab("EN+EC", "B.A. (Prog.) English + Economics", "English & Economics", "Room 108", "#2E7D32"),
        CourseSectionTab("EN+PS", "B.A. (Prog.) English + Political Science", "English & Pol Science", "Room 110", "#E65100"),
        CourseSectionTab("HN+HS", "B.A. (Prog.) Hindi + History", "Hindi & History", "Room 201", "#6A1B9A"),
        CourseSectionTab("HS+PS", "B.A. (Prog.) History + Political Science", "History & Pol Science", "Room 204", "#00838F"),
        CourseSectionTab("EC", "B.A. (Hons.) Economics", "Economics", "Room 206", "#AD1457"),
        CourseSectionTab("EN", "B.A. (Hons.) English", "English", "Room 208", "#283593"),
        CourseSectionTab("HN", "B.A. (Hons.) Hindi", "Hindi", "Room 210", "#4527A0"),
        CourseSectionTab("HS", "B.A. (Hons.) History", "History", "Room 212", "#37474F"),
        CourseSectionTab("PS", "B.A. (Hons.) Political Science", "Political Science", "Room 214", "#C2185B"),
        CourseSectionTab("BCOM", "B.Com (Program)", "Commerce", "Auditorium / 301", "#0277BD"),
        CourseSectionTab("BCH", "B.Com (Honours)", "Commerce", "Room 304", "#00695C"),
        CourseSectionTab("CH(H)", "B.Sc (Hons.) Chemistry", "Chemistry", "Chemistry Lab 1", "#558B2F"),
        CourseSectionTab("MT(H)", "B.Sc (Hons.) Mathematics", "Mathematics", "Room 308", "#D84315"),
        CourseSectionTab("BSC.CH", "B.Sc Phys Sci with Chemistry", "Physics & Chemistry", "Physics Lab", "#4E342E"),
        CourseSectionTab("BSC.CS", "B.Sc Phys Sci with Computer Science", "Computer Science", "Computer Lab 1", "#1E3A8A"),
        CourseSectionTab("BSC.EL", "B.Sc Phys Sci with Electronics", "Electronics", "Electronics Lab", "#004D40")
    )

    // ==========================================
    // 1st Year (2026-27): EC+PS Section-A (35 students)
    // ==========================================
    val firstYearEcPsStudents get() = FirstYearArtsData.ecPsStudents

    // 2nd Year (2026-27): EC+OM Section-A (20 students)
    val secondYearEcOmStudents get() = SecondYearArtsData.ecOmStudents

    // ==========================================
    // 3rd Year (2026-27): EC+OM Section-A (18 students)
    // ==========================================
    val thirdYearEcOmStudents = listOf(
        CollegeStudentItem(1, "240402", "Shambhawi Srivastava"),
        CollegeStudentItem(2, "240403", "Khush Jaggi"),
        CollegeStudentItem(3, "240405", "Vivek Ranjan"),
        CollegeStudentItem(4, "240406", "Sandeep Kumar"),
        CollegeStudentItem(5, "240407", "Srishti joshi"),
        CollegeStudentItem(6, "240408", "Chirag Saini"),
        CollegeStudentItem(7, "240410", "Soham Dey"),
        CollegeStudentItem(8, "240411", "Rajan"),
        CollegeStudentItem(9, "240412", "Shreya Rai"),
        CollegeStudentItem(10, "240414", "Kushagra Mahalwar"),
        CollegeStudentItem(11, "240415", "Tanmay"),
        CollegeStudentItem(12, "240416", "Reet Ranjan"),
        CollegeStudentItem(13, "240417", "Avinash Kumar"),
        CollegeStudentItem(14, "240419", "Pooja"),
        CollegeStudentItem(15, "240420", "Shyam Kalia"),
        CollegeStudentItem(16, "240423", "Devansh Bisht"),
        CollegeStudentItem(17, "240425", "Nishant Kumar"),
        CollegeStudentItem(18, "240427", "TSERING CHUNZIN")
    )

    /**
     * Master directory containing all enrolled students of Shyam Lal College across 1st, 2nd, and 3rd years.
     * Used for instant roll number search and auto-adding students without manual entry.
     */
    val masterStudentDirectory: List<CollegeStudentItem> by lazy {
        val allLists = mutableListOf<List<CollegeStudentItem>>()

        // 1st Year (2026-27)
        allLists.add(FirstYearArtsData.ecCommerceStudents)
        allLists.add(FirstYearArtsData.ecPsStudents)
        allLists.add(FirstYearArtsData.enEcStudents)
        allLists.add(FirstYearArtsData.enPsStudents)
        allLists.add(FirstYearArtsData.hnHsStudents)
        allLists.add(FirstYearArtsData.hsPsStudents)
        allLists.add(FirstYearArtsData.hnHonsStudents)
        allLists.add(FirstYearArtsData.hsHonsStudents)
        allLists.add(FirstYearArtsData.psHonsStudents)
        allLists.add(FirstYearEcoAndEnglishData.ecoSecA)
        allLists.add(FirstYearEcoAndEnglishData.ecoSecB)
        allLists.add(FirstYearEcoAndEnglishData.englishSecA)
        allLists.add(FirstYearEcoAndEnglishData.englishSecB)
        allLists.add(FirstYearBComData.secA)
        allLists.add(FirstYearBComData.secB)
        allLists.add(FirstYearBComData.secC)
        allLists.add(FirstYearBComData.secD)
        allLists.add(FirstYearBComHonsData.secA)
        allLists.add(FirstYearBComHonsData.secB)
        allLists.add(FirstYearBComHonsData.secC)
        allLists.add(FirstYearBComHonsData.secD)
        allLists.add(FirstYearScienceData.chemHonsStudents)
        allLists.add(FirstYearScienceData.mathsHonsStudents)
        allLists.add(FirstYearScienceData.psChemSecA)
        allLists.add(FirstYearScienceData.psChemSecB)
        allLists.add(FirstYearScienceData.psCsStudents)
        allLists.add(FirstYearScienceData.psElStudents)

        // 2nd Year (2026-27)
        allLists.add(SecondYearArtsData.ecOmStudents)
        allLists.add(SecondYearArtsData.ecPsStudents)
        allLists.add(SecondYearArtsData.enEcStudents)
        allLists.add(SecondYearArtsData.enPsStudents)
        allLists.add(SecondYearArtsData.hnHsStudents)
        allLists.add(SecondYearArtsData.hsPsStudents)
        allLists.add(SecondYearArtsData.hnHonsStudents)
        allLists.add(SecondYearArtsData.hsHonsStudents)
        allLists.add(SecondYearArtsData.psHonsStudents)
        allLists.add(SecondYearEcoAndEnglishData.ecoSecA)
        allLists.add(SecondYearEcoAndEnglishData.ecoSecB)
        allLists.add(SecondYearEcoAndEnglishData.englishSecA)
        allLists.add(SecondYearEcoAndEnglishData.englishSecB)
        allLists.add(SecondYearBComData.secA)
        allLists.add(SecondYearBComData.secB)
        allLists.add(SecondYearBComData.secC)
        allLists.add(SecondYearBComData.secD)
        allLists.add(SecondYearBComHonsData.secA)
        allLists.add(SecondYearBComHonsData.secB)
        allLists.add(SecondYearBComHonsData.secC)
        allLists.add(SecondYearBComHonsData.secD)
        allLists.add(SecondYearScienceData.chemHonsStudents)
        allLists.add(SecondYearScienceData.mathsHonsStudents)
        allLists.add(SecondYearScienceData.psChemSecA)
        allLists.add(SecondYearScienceData.psChemSecB)
        allLists.add(SecondYearScienceData.psCsStudents)
        allLists.add(SecondYearScienceData.psElStudents)

        // 3rd Year (2026-27)
        allLists.add(thirdYearEcOmStudents)
        allLists.add(ThirdYearArtsData.ecPsStudents)
        allLists.add(ThirdYearArtsData.enEcStudents)
        allLists.add(ThirdYearArtsData.enPsStudents)
        allLists.add(ThirdYearArtsData.hnHsStudents)
        allLists.add(ThirdYearArtsData.hsPsStudents)
        allLists.add(ThirdYearArtsData.hnHonsStudents)
        allLists.add(ThirdYearArtsData.hsHonsStudents)
        allLists.add(ThirdYearArtsData.psHonsStudents)
        allLists.add(ThirdYearEcoAndEnglishData.ecSecAStudents)
        allLists.add(ThirdYearEcoAndEnglishData.ecSecBStudents)
        allLists.add(ThirdYearEcoAndEnglishData.enSecAStudents)
        allLists.add(ThirdYearEcoAndEnglishData.enSecBStudents)
        allLists.add(ThirdYearBComData.bcomSecAStudents)
        allLists.add(ThirdYearBComData.bcomSecBStudents)
        allLists.add(ThirdYearBComData.bcomSecCStudents)
        allLists.add(ThirdYearBComData.bcomSecDStudents)
        allLists.add(ThirdYearBComHonsData.bchSecAStudents)
        allLists.add(ThirdYearBComHonsData.bchSecBStudents)
        allLists.add(ThirdYearBComHonsData.bchSecCStudents)
        allLists.add(ThirdYearBComHonsData.bchSecDStudents)
        allLists.add(ThirdYearScienceData.chHonsStudents)
        allLists.add(ThirdYearScienceData.mtHonsStudents)
        allLists.add(ThirdYearScienceData.bscChSecAStudents)
        allLists.add(ThirdYearScienceData.bscChSecBStudents)
        allLists.add(ThirdYearScienceData.bscCsSecCStudents)
        allLists.add(ThirdYearScienceData.bscElSecCStudents)

        allLists.flatten().distinctBy { it.rollNo.trim().uppercase() }
    }

    /**
     * Finds a student in the college directory by roll number (case-insensitive & trimmed).
     */
    fun findStudentByRoll(rollNo: String): CollegeStudentItem? {
        val cleanRoll = rollNo.trim().uppercase()
        if (cleanRoll.isEmpty()) return null
        return masterStudentDirectory.firstOrNull { it.rollNo.trim().uppercase() == cleanRoll }
    }

    /**
     * Searches college directory for students matching roll number or name.
     */
    fun searchCollegeStudents(query: String, limit: Int = 12): List<CollegeStudentItem> {
        val cleanQuery = query.trim().uppercase()
        if (cleanQuery.isEmpty()) return emptyList()
        return masterStudentDirectory
            .filter {
                it.rollNo.uppercase().contains(cleanQuery) ||
                it.name.uppercase().contains(cleanQuery)
            }
            .take(limit)
    }

    /**
     * Comprehensive search index across ALL combinations, courses, sections, and years.
     * Maps each student to their exact Course, Academic Year, Section, and Room.
     */
    val allIndexedStudents: List<GlobalStudentSearchResult> by lazy {
        val result = mutableListOf<GlobalStudentSearchResult>()
        val years = listOf("1st Year", "2nd Year", "3rd Year")
        for (yr in years) {
            for (tab in courseTabs) {
                val roster = getRoster(year = yr, courseTabCode = tab.code, sectionFilter = "All")
                for (st in roster.students) {
                    result.add(
                        GlobalStudentSearchResult(
                            student = st,
                            year = yr,
                            courseCode = tab.code,
                            courseTitle = tab.fullName,
                            department = tab.department,
                            section = st.section,
                            defaultRoom = tab.defaultRoom
                        )
                    )
                }
            }
        }
        result
    }

    /**
     * Searches across ALL course combinations (all 18 tabs) and ALL 3 years (1st, 2nd, 3rd year).
     * Matches against student roll number, name, sNo, or course code/title.
     */
    fun searchAllCombinationsAndYears(
        query: String,
        yearFilter: String? = null,
        limit: Int = 150
    ): List<GlobalStudentSearchResult> {
        val cleanQuery = query.trim().uppercase()
        if (cleanQuery.isEmpty()) return emptyList()
        return allIndexedStudents
            .filter { item ->
                (yearFilter == null || yearFilter == "All" || item.year.equals(yearFilter, ignoreCase = true)) &&
                (
                    item.student.rollNo.uppercase().contains(cleanQuery) ||
                    item.student.name.uppercase().contains(cleanQuery) ||
                    item.student.sNo.toString() == cleanQuery ||
                    item.courseCode.uppercase().contains(cleanQuery)
                )
            }
            .take(limit)
    }

    /**
     * Generates or retrieves the official roster for any Year, Course Tab, and optional Section Filter
     */
    fun getRoster(year: String, courseTabCode: String, sectionFilter: String? = null): SectionSheet {
        val courseInfo = courseTabs.find { it.code == courseTabCode } 
            ?: courseTabs[0]

        val is3rdYear = year.contains("3")

        var availableSections = listOf("Section-A")
        var defaultSection = "Section-A"
        var fullList: List<CollegeStudentItem>

        when {
            // 3rd Year (2026-2027) Authentic Roster Data for all 18 courses
            is3rdYear -> {
                when (courseTabCode) {
                    "EC+OM" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearArtsData.ecOmStudents
                    }
                    "EC+PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearArtsData.ecPsStudents
                    }
                    "EN+EC" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = ThirdYearArtsData.enEcStudents
                    }
                    "EN+PS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = ThirdYearArtsData.enPsStudents
                    }
                    "HN+HS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = ThirdYearArtsData.hnHsStudents
                    }
                    "HS+PS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = ThirdYearArtsData.hsPsStudents
                    }
                    "EC" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = ThirdYearEcoAndEnglishData.ecSecAStudents + ThirdYearEcoAndEnglishData.ecSecBStudents
                    }
                    "EN" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = ThirdYearEcoAndEnglishData.enSecAStudents + ThirdYearEcoAndEnglishData.enSecBStudents
                    }
                    "HN" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearArtsData.hnHonsStudents
                    }
                    "HS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearArtsData.hsHonsStudents
                    }
                    "PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearArtsData.psHonsStudents
                    }
                    "BCOM" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = ThirdYearBComData.bcomSecAStudents + ThirdYearBComData.bcomSecBStudents + ThirdYearBComData.bcomSecCStudents + ThirdYearBComData.bcomSecDStudents
                    }
                    "BCH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = ThirdYearBComHonsData.bchSecAStudents + ThirdYearBComHonsData.bchSecBStudents + ThirdYearBComHonsData.bchSecCStudents + ThirdYearBComHonsData.bchSecDStudents
                    }
                    "CH(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearScienceData.chHonsStudents
                    }
                    "MT(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = ThirdYearScienceData.mtHonsStudents
                    }
                    "BSC.CH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = ThirdYearScienceData.bscChSecAStudents + ThirdYearScienceData.bscChSecBStudents
                    }
                    "BSC.CS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = ThirdYearScienceData.bscCsSecCStudents
                    }
                    "BSC.EL" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = ThirdYearScienceData.bscElSecCStudents
                    }
                    else -> {
                        fullList = generateRosterForCourseAndYear(year, courseTabCode)
                    }
                }
            }

            // 2nd Year (all 18 courses)
            year.contains("2") -> {
                when (courseTabCode) {
                    "EC+OM" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearArtsData.ecOmStudents
                    }
                    "EC+PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearArtsData.ecPsStudents
                    }
                    "EN+EC" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = SecondYearArtsData.enEcStudents
                    }
                    "EN+PS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = SecondYearArtsData.enPsStudents
                    }
                    "HN+HS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = SecondYearArtsData.hnHsStudents
                    }
                    "HS+PS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = SecondYearArtsData.hsPsStudents
                    }
                    "EC" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = SecondYearEcoAndEnglishData.ecoSecA + SecondYearEcoAndEnglishData.ecoSecB
                    }
                    "EN" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = SecondYearEcoAndEnglishData.englishSecA + SecondYearEcoAndEnglishData.englishSecB
                    }
                    "HN" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearArtsData.hnHonsStudents
                    }
                    "HS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearArtsData.hsHonsStudents
                    }
                    "PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearArtsData.psHonsStudents
                    }
                    "BCOM" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = SecondYearBComData.secA + SecondYearBComData.secB + SecondYearBComData.secC + SecondYearBComData.secD
                    }
                    "BCH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = SecondYearBComHonsData.secA + SecondYearBComHonsData.secB + SecondYearBComHonsData.secC + SecondYearBComHonsData.secD
                    }
                    "CH(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearScienceData.chemHonsStudents
                    }
                    "MT(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = SecondYearScienceData.mathsHonsStudents
                    }
                    "BSC.CH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = SecondYearScienceData.psChemSecA + SecondYearScienceData.psChemSecB
                    }
                    "BSC.CS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = SecondYearScienceData.psCsStudents
                    }
                    "BSC.EL" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = SecondYearScienceData.psElStudents
                    }
                    else -> {
                        fullList = generateRosterForCourseAndYear(year, courseTabCode)
                    }
                }
            }

            // 1st Year (all 18 courses)
            year.contains("1") -> {
                when (courseTabCode) {
                    "EC+OM" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearArtsData.ecCommerceStudents
                    }
                    "EC+PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearArtsData.ecPsStudents
                    }
                    "EN+EC" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = FirstYearArtsData.enEcStudents
                    }
                    "EN+PS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = FirstYearArtsData.enPsStudents
                    }
                    "HN+HS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = FirstYearArtsData.hnHsStudents
                    }
                    "HS+PS" -> {
                        defaultSection = "Section-B"
                        availableSections = listOf("Section-B")
                        fullList = FirstYearArtsData.hsPsStudents
                    }
                    "EC" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = FirstYearEcoAndEnglishData.ecoSecA + FirstYearEcoAndEnglishData.ecoSecB
                    }
                    "EN" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = FirstYearEcoAndEnglishData.englishSecA + FirstYearEcoAndEnglishData.englishSecB
                    }
                    "HN" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearArtsData.hnHonsStudents
                    }
                    "HS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearArtsData.hsHonsStudents
                    }
                    "PS" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearArtsData.psHonsStudents
                    }
                    "BCOM" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = FirstYearBComData.secA + FirstYearBComData.secB + FirstYearBComData.secC + FirstYearBComData.secD
                    }
                    "BCH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B", "Section-C", "Section-D")
                        fullList = FirstYearBComHonsData.secA + FirstYearBComHonsData.secB + FirstYearBComHonsData.secC + FirstYearBComHonsData.secD
                    }
                    "CH(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearScienceData.chemHonsStudents
                    }
                    "MT(H)" -> {
                        defaultSection = "Section-A"
                        availableSections = listOf("Section-A")
                        fullList = FirstYearScienceData.mathsHonsStudents
                    }
                    "BSC.CH" -> {
                        defaultSection = "All"
                        availableSections = listOf("All", "Section-A", "Section-B")
                        fullList = FirstYearScienceData.psChemSecA + FirstYearScienceData.psChemSecB
                    }
                    "BSC.CS" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = FirstYearScienceData.psCsStudents
                    }
                    "BSC.EL" -> {
                        defaultSection = "Section-C"
                        availableSections = listOf("Section-C")
                        fullList = FirstYearScienceData.psElStudents
                    }
                    else -> {
                        fullList = generateRosterForCourseAndYear(year, courseTabCode)
                    }
                }
            }

            else -> {
                fullList = generateRosterForCourseAndYear(year, courseTabCode)
            }
        }

        val effectiveSection = sectionFilter ?: defaultSection
        val filteredStudents = if (effectiveSection == "All" || effectiveSection.isBlank()) {
            fullList
        } else {
            fullList.filter { it.section.equals(effectiveSection, ignoreCase = true) }
        }

        val reindexedStudents = filteredStudents.mapIndexed { idx, item ->
            item.copy(sNo = idx + 1)
        }

        val sectionTitlePart = if (effectiveSection == "All" || availableSections.size <= 1) "" else " ($effectiveSection)"

        return SectionSheet(
            courseCode = courseTabCode,
            courseTitle = "${courseInfo.fullName}$sectionTitlePart, $year (2026-27)",
            year = year,
            academicYear = "2026-2027",
            section = effectiveSection,
            availableSections = availableSections,
            students = reindexedStudents
        )
    }

    private fun generateRosterForCourseAndYear(year: String, courseCode: String): List<CollegeStudentItem> {
        val prefix = when {
            year.contains("1") -> "260"
            year.contains("2") -> "250"
            else -> "240"
        }
        val subCodeNum = when (courseCode) {
            "EC+OM" -> "4"
            "EC+PS" -> "0"
            "EN+EC" -> "1"
            "EN+PS" -> "2"
            "HN+HS" -> "3"
            "HS+PS" -> "5"
            "EC" -> "6"
            "EN" -> "7"
            "HN" -> "8"
            "HS" -> "9"
            "PS" -> "1"
            "BCOM" -> "2"
            "BCH" -> "3"
            "CH(H)" -> "7"
            "MT(H)" -> "8"
            "BSC.CH" -> "5"
            "BSC.CS" -> "6"
            "BSC.EL" -> "7"
            else -> "0"
        }

        val sampleNames = listOf(
            "Aarav Sharma", "Aditi Verma", "Ananya Gupta", "Bhavya Malik", "Dhruv Saxena",
            "Harsh Kumar", "Isha Negi", "Karan Malhotra", "Mehak Chawla", "Nikhil Joshi",
            "Pooja Rawat", "Prateek Bansal", "Rhea Sengupta", "Rohan Tyagi", "Sanjana Roy",
            "Shivam Tiwari", "Simran Kaur", "Tanvi Bhatia", "Utkarsh Singh", "Vikas Meena",
            "Yashika Jain", "Zaid Ahmed"
        )

        return sampleNames.mapIndexed { idx, name ->
            val rollSuffix = String.format("%02d", (idx + 1) * 2)
            CollegeStudentItem(
                sNo = idx + 1,
                rollNo = "$prefix$subCodeNum$rollSuffix",
                name = name.uppercase()
            )
        }
    }
}
