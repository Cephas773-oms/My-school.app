package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val questionDao = db.pastQuestionDao()
    private val downloadRecordDao = db.downloadRecordDao()
    private val notificationDao = db.notificationDao()

    // --- User Management ---
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: User): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    // --- Past Questions Management ---
    val allQuestions: Flow<List<PastQuestion>> = questionDao.getAllQuestions()
    val featuredQuestions: Flow<List<PastQuestion>> = questionDao.getFeaturedQuestions()

    fun searchQuestions(query: String): Flow<List<PastQuestion>> {
        return if (query.isBlank()) {
            questionDao.getAllQuestions()
        } else {
            questionDao.searchQuestions(query)
        }
    }

    suspend fun getQuestionById(id: Int): PastQuestion? = withContext(Dispatchers.IO) {
        questionDao.getQuestionById(id)
    }

    suspend fun insertQuestion(question: PastQuestion): Long = withContext(Dispatchers.IO) {
        questionDao.insertQuestion(question)
    }

    suspend fun updateQuestion(question: PastQuestion) = withContext(Dispatchers.IO) {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: PastQuestion) = withContext(Dispatchers.IO) {
        questionDao.deleteQuestion(question)
    }

    suspend fun incrementDownloadCount(id: Int) = withContext(Dispatchers.IO) {
        questionDao.incrementDownloadCount(id)
    }

    suspend fun incrementLikeCount(id: Int) = withContext(Dispatchers.IO) {
        questionDao.incrementLikeCount(id)
    }

    // --- Download Tracking ---
    val allDownloadRecords: Flow<List<DownloadRecord>> = downloadRecordDao.getAllDownloadRecords()

    fun getDownloadRecordsForUser(email: String): Flow<List<DownloadRecord>> {
        return downloadRecordDao.getDownloadRecordsByUser(email)
    }

    suspend fun recordDownload(record: DownloadRecord): Long = withContext(Dispatchers.IO) {
        downloadRecordDao.insertDownloadRecord(record)
    }

    // --- Notifications ---
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()

    suspend fun addNotification(title: String, message: String): Long = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(
            Notification(title = title, message = message)
        )
    }

    suspend fun markNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // --- Initialize Default App Data ---
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        // Only seed if users are empty
        if (userDao.getUsersCount() == 0) {
            // 1. Create Default Admin
            userDao.insertUser(
                User(
                    email = "admin@htu.edu.gh",
                    fullName = "Prof. Emmanuel Kwabla",
                    studentId = "HTU-ADM-2026",
                    role = "ADMIN",
                    isVerified = true,
                    verificationCode = "9999",
                    passwordHash = "admin" // For simplicity in visual proof-of-concept
                )
            )

            // 2. Create Default Student
            userDao.insertUser(
                User(
                    email = "student@htu.edu.gh",
                    fullName = "Cephas Amuyao",
                    studentId = "HTU-BCT-23-009",
                    role = "STUDENT",
                    isVerified = true,
                    verificationCode = "1234",
                    passwordHash = "student"
                )
            )

            // 3. Pre-populate beautiful Past exam papers
            val sampleQuestions = listOf(
                PastQuestion(
                    courseCode = "COMP 301",
                    courseName = "Database Systems & Design",
                    programme = "BTech Computer Science",
                    academicYear = "2023/2024",
                    semester = 1,
                    examiner = "Dr. J. F. Kofi",
                    fileType = "PDF",
                    fileName = "comp301_database_systems_2023.pdf",
                    fileSize = "1.2 MB",
                    downloadsCount = 42,
                    likesCount = 18,
                    isFeatured = true,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY (HTU)
                        FACULTY OF APPLIED SCIENCES & TECHNOLOGY
                        DEPARTMENT OF COMPUTER SCIENCE
                        END OF RE-SIT EXAMINATIONS - 2023/2024
                        
                        COURSE: DATABASE SYSTEMS & DESIGN (COMP 301)
                        CLASS: BTECH COMPUTER SCIENCE LEVEL 300
                        TIME ALLOWED: 2 HOURS 30 MINUTES
                        
                        INSTRUCTIONS: SECTION A IS COMPULSORY. ANSWER TWO QUESTIONS FROM SECTION B.
                        
                        SECTION A (40 MARKS)
                        
                        Q1. (a) Briefly explain why the traditional File Processing System is prone to data inconsistency compared to a Database Management System. [10 Marks]
                        
                        (b) Outline the responsibilities of a Database Administrator (DBA). [10 Marks]
                        
                        (c) With appropriate SQL schemas or ER Diagrams, explain the difference between a Weak Entity and a Strong Entity. [10 Marks]
                        
                        (d) Show the mathematical relational algebra formula for SELECT and PROJECT operations. [10 Marks]
                        
                        SECTION B (60 MARKS - ANSWER ONLY TWO QUESTIONS)
                        
                        Q2. (a) Construct an Entity-Relationship (ER) Diagram representing the Ho Technical University past questions portal. Assume that a student can download many questions, and each question has details like course code, programme, and year. Highlight primary keys and cardinalities. [15 Marks]
                        
                        (b) What are insertion, deletion, and update anomalies? Illustrate with a simple unnormalized table structure. [15 Marks]
                        
                        Q3. Database Normalization:
                        (a) Define First Normal Form (1NF), Second Normal Form (2NF), and Third Normal Form (3NF). [15 Marks]
                        
                        (b) Normalize the following student registration schedule into 3NF:
                        StudentId -> StudentName, DormRoom, RoomFee, CourseCode, CourseTitle, Grade. [15 Marks]
                    """.trimIndent()
                ),
                PastQuestion(
                    courseCode = "COMP 305",
                    courseName = "Advanced Web Technologies",
                    programme = "BTech Computer Science",
                    academicYear = "2022/2023",
                    semester = 2,
                    examiner = "Prof. E. Asante",
                    fileType = "PDF",
                    fileName = "web_tech_2023_sem2.pdf",
                    fileSize = "980 KB",
                    downloadsCount = 59,
                    likesCount = 31,
                    isFeatured = true,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY (HTU)
                        DEPARTMENT OF COMPUTER SCIENCE
                        END OF ACADEMIC YEAR EXAMINATIONS - SEMESTER 2
                        
                        COURSE: ADVANCED WEB TECHNOLOGIES (COMP 305)
                        CLASS: BTECH COMPUTER SCIENCE LEVEL 300
                        TIME ALLOWED: 2 HOURS
                        
                        SECTION A (MULITPLE CHOICE - 20 MARKS)
                        1. Which HTTP status code represents 'Internal Server Error'?
                           A) 404   B) 403   C) 500   D) 502
                        2. Which CSS Layout model is designed for one-dimensional layouts?
                           A) Grid  B) Flexbox  C) Float D) Inline
                        
                        SECTION B (ESSAY - 40 MARKS)
                        Q1. (a) Discuss the Architecture of a modern single page application (SPA) backed by REST APIs. Compare React and Angular lifecycle states. [15 Marks]
                        
                        (b) Write a comprehensive JavaScript/Kotlin code snippets illustrating how you would prevent race conditions when writing a debounced search input fetching past questions at HTU. [15 Marks]
                        
                        Q2. Explain state management strategies (e.g. Redux, Jetpack StateFlow) and detailed Web Security basics: CSRF, XSS, and SQL Injection prevention. [10 Marks]
                    """.trimIndent()
                ),
                PastQuestion(
                    courseCode = "CIVL 201",
                    courseName = "Theory of Structures",
                    programme = "HND Civil Engineering",
                    academicYear = "2023/2024",
                    semester = 1,
                    examiner = "Eng. Mawutor Agbe",
                    fileType = "DOCX",
                    fileName = "civil_structures201.docx",
                    fileSize = "2.3 MB",
                    downloadsCount = 18,
                    likesCount = 7,
                    isFeatured = false,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY
                        FACULTY OF ENGINEERING
                        DEPARTMENT OF CIVIL ENGINEERING
                        END OF FIRST SEMESTER EXAMINATION
                        
                        COURSE: THEORY OF STRUCTURES (CIVL 201)
                        TIME ALLOWED: 3 HOURS
                        
                        Q1. Fig Q1 shows a continuous beam ABCD with fixed ends at A and D. It carries a uniformly distributed load of 15kN/m over AB and BC, and a point load of 30kN at the midpoint of CD. 
                        Calculate:
                        a) Support reactions and shear forces.
                        b) Bending moments at supports using the Moment Distribution Method.
                        c) Sketch the Bending Moment and Shear Force diagrams.
                        
                        Q2. Discuss Euler's Column buckling theory. Detail key assumptions and limitations of the Euler formula for slender structural steel columns.
                    """.trimIndent()
                ),
                PastQuestion(
                    courseCode = "HOSP 302",
                    courseName = "Food & Beverage Cost Control",
                    programme = "BTech Hospitality Management",
                    academicYear = "2023/2024",
                    semester = 2,
                    examiner = "Mrs. Abigail Boateng",
                    fileType = "PDF",
                    fileName = "hosp_cost_control_2024.pdf",
                    fileSize = "1.1 MB",
                    downloadsCount = 27,
                    likesCount = 14,
                    isFeatured = false,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY
                        DEPARTMENT OF HOSPITALITY & TOURISM MANAGEMENT
                        BTECH DEGREE EXAMINATIONS - SEMESTER 2
                        
                        COURSE: FOOD & BEVERAGE COST CONTROL (HOSP 302)
                        
                        Q1. Explain the cost-control cycle starting from purchasing, receiving, storing, issuing, to final cooking production in a commercial five-star kitchen outlet. Compare inventory turnover ratios.
                        
                        Q2. Calculate the Food Cost Percentage from the following information:
                        - Food Sales: GH¢35,400.00
                        - Opening Inventory: GH¢4,200.00
                        - Purchases: GH¢12,500.00
                        - Closing Inventory: GH¢3,800.00
                        - Employee Meals Credit: GH¢500.00
                    """.trimIndent()
                ),
                PastQuestion(
                    courseCode = "ACCT 310",
                    courseName = "Auditing & Assurance Services",
                    programme = "BTech Accountancy",
                    academicYear = "2022/2023",
                    semester = 1,
                    examiner = "Mr. Richard Dogbe",
                    fileType = "PDF",
                    fileName = "auditing_assurance_310.pdf",
                    fileSize = "1.5 MB",
                    downloadsCount = 35,
                    likesCount = 19,
                    isFeatured = false,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY
                        BUSINESS SCHOOL - DEPARTMENT OF ACCOUNTANCY
                        END OF SEMESTER EXAMINATIONS
                        
                        COURSE: AUDITING & ASSURANCE SERVICES (ACCT 310)
                        
                        Q1. (a) Discuss 'Auditor Independence' and the conceptual framework for threats (Self-interest, Self-review, Advocacy, Familiarity, and Intimidation). Provide structural safeguards.
                        
                        Q2. List and explain the five key elements of an Auditing engagement: (1) Three-party relationship, (2) Subject matter, (3) Suitable criteria, (4) Sufficient appropriate evidence, and (5) Written assurance report.
                    """.trimIndent()
                ),
                PastQuestion(
                    courseCode = "FASH 201",
                    courseName = "History of Fashion & Costume",
                    programme = "HND Fashion Design & Textiles",
                    academicYear = "2023/2024",
                    semester = 1,
                    examiner = "Ms. Grace Sika",
                    fileType = "PNG",
                    fileName = "fash201_costume_plates.png",
                    fileSize = "4.2 MB",
                    downloadsCount = 11,
                    likesCount = 5,
                    isFeatured = false,
                    previewQuestions = """
                        HO TECHNICAL UNIVERSITY
                        DEPARTMENT OF FASHION DESIGN & TEXTILES
                        EXAMINATION QUESTIONS
                        
                        COURSE: HISTORY OF FASHION & COSTUME (FASH 201)
                        
                        Q1. Critically analyze the evolution of traditional West African fabrics (such as Kente and Gonja cloth) from historical royalty ceremonial usages to modern global fashion designs. 
                        
                        Q2. Discuss the industrial revolution's impact on textile production line mechanisms and the subsequent emergence of haute couture houses in the early 20th century.
                    """.trimIndent()
                )
            )

            for (pq in sampleQuestions) {
                questionDao.insertQuestion(pq)
            }

            // Add starting notifications
            notificationDao.insertNotification(
                Notification(
                    title = "System Online!",
                    message = "Welcome to the Ho Technical University Past Questions Portal. Browse and download past questions safely with full offline capability."
                )
            )
            notificationDao.insertNotification(
                Notification(
                    title = "New Uploads for BTech CS",
                    message = "Dr. J. F. Kofi has added 'COMP 301 - Database Systems & Design' for 2023/2024 Academic Year."
                )
            )
        }
    }
}
