package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    data class VerifyOtp(val email: String) : Screen()
    object StudentDashboard : Screen()
    object AdminDashboard : Screen()
}

class PortalViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {

    // --- State Stream Managers ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProgramme = MutableStateFlow<String?>("All Programmes")
    val selectedProgramme: StateFlow<String?> = _selectedProgramme.asStateFlow()

    private val _selectedSemester = MutableStateFlow<Int?>(0) // 0 means All Semesters
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

    private val _recentNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val recentNotifications: StateFlow<List<Notification>> = _recentNotifications.asStateFlow()

    // --- Active Message Banner Feed (Toasts / Dialogs) ---
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _otpAlert = MutableStateFlow<String?>(null) // Popups simulated SMS/Email alert containing code
    val otpAlert: StateFlow<String?> = _otpAlert.asStateFlow()

    // --- State of Downloading Files (Active Download Spinner) ---
    private val _downloadingIds = MutableStateFlow<Set<Int>>(emptySet())
    val downloadingIds: StateFlow<Set<Int>> = _downloadingIds.asStateFlow()

    private val _downloadedDocIds = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedDocIds: StateFlow<Set<Int>> = _downloadedDocIds.asStateFlow()

    // --- Selected PDF for Viewing (Simulated Document Viewer overlay) ---
    private val _activeViewingQuestion = MutableStateFlow<PastQuestion?>(null)
    val activeViewingQuestion: StateFlow<PastQuestion?> = _activeViewingQuestion.asStateFlow()

    // Initialize data seed
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Pull notifications
            repository.allNotifications.collect {
                _recentNotifications.value = it
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Live Questions Streams ---
    val pastQuestions: StateFlow<List<PastQuestion>> = _searchQuery
        .debounce(100)
        .flatMapLatest { query ->
            repository.searchQuestions(query)
        }
        .combine(_selectedProgramme) { questions, prog ->
            if (prog == "All Programmes" || prog == null) questions
            else questions.filter { it.programme == prog }
        }
        .combine(_selectedSemester) { questions, sem ->
            if (sem == 0 || sem == null) questions
            else questions.filter { it.semester == sem }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val featuredQuestions: StateFlow<List<PastQuestion>> = repository.featuredQuestions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Track Logs (Total downloads stats) ---
    val downloadLogs: StateFlow<List<DownloadRecord>> = repository.allDownloadRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Auth Actions ---
    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            if (email.isBlank() || passwordHash.isBlank()) {
                _toastMessage.value = "Please fill in all credentials"
                return@launch
            }

            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user == null) {
                _toastMessage.value = "User not found. Try student@htu.edu.gh or register."
                return@launch
            }

            if (user.passwordHash != passwordHash) {
                _toastMessage.value = "Incorrect password"
                return@launch
            }

            if (!user.isVerified) {
                // Redirect back to OTP verification
                _currentScreen.value = Screen.VerifyOtp(user.email)
                _toastMessage.value = "Please verify your account OTP code which was sent."
                triggerOtpSimulatedAlert(user.email, user.verificationCode)
                return@launch
            }

            // Success login
            _currentUser.value = user
            if (user.role == "ADMIN") {
                _currentScreen.value = Screen.AdminDashboard
                _toastMessage.value = "Welcome Admin ${user.fullName}!"
            } else {
                _currentScreen.value = Screen.StudentDashboard
                _toastMessage.value = "Welcome ${user.fullName}!"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Login
        _toastMessage.value = "Successfully logged out"
    }

    fun register(fullName: String, email: String, studentId: String, role: String, passwordHash: String) {
        viewModelScope.launch {
            if (fullName.isBlank() || email.isBlank() || studentId.isBlank() || passwordHash.isBlank()) {
                _toastMessage.value = "All fields are required"
                return@launch
            }

            if (role == "STUDENT" && !email.trim().endsWith("@htu.edu.gh") && !email.trim().endsWith(".edu.gh") && !email.trim().lowercase().contains("htu")) {
                _toastMessage.value = "Use your official HTU student email (e.g. cephas@htu.edu.gh) to register"
                return@launch
            }

            val existing = repository.getUserByEmail(email.trim().lowercase())
            if (existing != null) {
                _toastMessage.value = "An account with this email already exists"
                return@launch
            }

            val otpCode = String.format("%04d", Random.nextInt(1000, 9999))
            val newUser = User(
                email = email.trim().lowercase(),
                fullName = fullName.trim(),
                studentId = studentId.trim().uppercase(),
                role = role,
                isVerified = false,
                verificationCode = otpCode,
                passwordHash = passwordHash
            )

            repository.registerUser(newUser)
            _toastMessage.value = "Account created! Verification code sent."
            _currentScreen.value = Screen.VerifyOtp(newUser.email)
            triggerOtpSimulatedAlert(newUser.email, otpCode)
        }
    }

    fun verifyOtp(email: String, enteredOtp: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                _toastMessage.value = "User session not found"
                return@launch
            }

            if (user.verificationCode == enteredOtp || enteredOtp == "1234" || enteredOtp == "9999") { // Bypass defaults or match
                val verifiedUser = user.copy(isVerified = true)
                repository.updateUser(verifiedUser)
                _toastMessage.value = "Verification successful! You can now log in."
                _currentScreen.value = Screen.Login
                _otpAlert.value = null
                // Trigger welcoming notification
                repository.addNotification(
                    "Welcome ${verifiedUser.fullName}!",
                    "Your portal registration has been successfully verified. Browse hundreds of past questions now!"
                )
            } else {
                _toastMessage.value = "Invalid OTP code. Try again."
            }
        }
    }

    private fun triggerOtpSimulatedAlert(email: String, code: String) {
        _otpAlert.value = "⚡ [OTP SIMULATOR]\nVerification code for $email:\n🔑 CODE: $code\n(Use this code or default '1234' to verify instantly)"
    }

    fun closeOtpAlert() {
        _otpAlert.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Search & Filtering Actions ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateProgrammeFilter(prog: String?) {
        _selectedProgramme.value = prog
    }

    fun updateSemesterFilter(sem: Int?) {
        _selectedSemester.value = sem
    }

    // --- Simulated File Downloading & View Tracks ---
    fun downloadQuestion(question: PastQuestion) {
        val user = _currentUser.value ?: return
        if (_downloadingIds.value.contains(question.id)) return

        viewModelScope.launch {
            // Add downloading indicator
            _downloadingIds.value = _downloadingIds.value + question.id
            
            // Artificial network/download delay to simulate HTU server traffic loading
            kotlinx.coroutines.delay(1200)

            // Finish download
            _downloadingIds.value = _downloadingIds.value - question.id
            _downloadedDocIds.value = _downloadedDocIds.value + question.id

            // Increment database download metrics
            repository.incrementDownloadCount(question.id)

            // Log tracks securely in the schema database
            repository.recordDownload(
                DownloadRecord(
                    questionId = question.id,
                    courseCode = question.courseCode,
                    courseName = question.courseName,
                    userEmail = user.email
                )
            )

            // Broad notify
            _toastMessage.value = "Paper '${question.courseCode}' saved directly to Device (Downloads file directory)"
            
            repository.addNotification(
                "File Download Success",
                "${user.fullName} downloaded ${question.courseCode} (${question.courseName}) on their local device."
            )
        }
    }

    fun likeQuestion(questionId: Int) {
        viewModelScope.launch {
            repository.incrementLikeCount(questionId)
            _toastMessage.value = "Voted helpful!"
        }
    }

    fun setViewingQuestion(question: PastQuestion?) {
        _activeViewingQuestion.value = question
    }

    // --- Admin Control Panel Actions ---
    fun uploadPastQuestion(
        code: String,
        name: String,
        prog: String,
        year: String,
        sem: Int,
        examiner: String,
        type: String,
        fileName: String,
        fileSize: String,
        syllabus: String
    ) {
        viewModelScope.launch {
            if (code.isBlank() || name.isBlank() || prog.isBlank() || year.isBlank() || examiner.isBlank()) {
                _toastMessage.value = "All fields except syllabus content are compulsory"
                return@launch
            }

            val newPq = PastQuestion(
                courseCode = code.trim().uppercase(),
                courseName = name.trim(),
                programme = prog,
                academicYear = year.trim(),
                semester = sem,
                examiner = examiner.trim(),
                fileType = type,
                fileName = if (fileName.isBlank()) "${code.lowercase().replace(" ","")}_exam.$type" else fileName,
                fileSize = if (fileSize.isBlank()) "1.2 MB" else fileSize,
                downloadsCount = 0,
                likesCount = 0,
                isFeatured = Random.nextBoolean(),
                previewQuestions = if (syllabus.isBlank()) "HTU PAST PAPER\n\nCOURSE: $name ($code)\nYEAR: $year\n\nSAMPLE QUESTION 1\nDiscuss the primary topics covered under the $name study plan." else syllabus
            )

            repository.insertQuestion(newPq)
            _toastMessage.value = "New Paper for '${code}' uploaded successfully to HTU Hub!"
            
            // Broadcast notification to student portals
            repository.addNotification(
                "New Exam Question Paper Added!",
                "Admin uploaded official exam question booklet for $code - $name ($year, Sem $sem)."
            )
        }
    }

    fun editPastQuestion(updated: PastQuestion) {
        viewModelScope.launch {
            repository.updateQuestion(updated)
            _toastMessage.value = "Successfully edited paper indices for '${updated.courseCode}'"
            repository.addNotification(
                "Past Paper Updated",
                "Administrative changes updated in Syllabus index for ${updated.courseCode}."
            )
        }
    }

    fun deletePastQuestion(pq: PastQuestion) {
        viewModelScope.launch {
            repository.deleteQuestion(pq)
            _toastMessage.value = "Exam paper '${pq.courseCode}' deleted from directory"
            repository.addNotification(
                "Administrative Deleted Paper",
                "Past Question workbook for ${pq.courseCode} removed by Administrative review."
            )
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markNotificationsAsRead()
        }
    }
}

// Factory
@Suppress("UNCHECKED_CAST")
class PortalViewModelFactory(
    private val application: Application,
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortalViewModel::class.java)) {
            return PortalViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
