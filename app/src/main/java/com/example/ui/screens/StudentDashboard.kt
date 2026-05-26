package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.PortalViewModel
import com.example.ui.components.HtuLogo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(viewModel: PortalViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val questions by viewModel.pastQuestions.collectAsState()
    val featuredQuestions by viewModel.featuredQuestions.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()
    val downloadedDocIds by viewModel.downloadedDocIds.collectAsState()
    val activeViewingQuestion by viewModel.activeViewingQuestion.collectAsState()
    val recentNotifications by viewModel.recentNotifications.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedProg by viewModel.selectedProgramme.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Browse, 1: Downloads, 2: Announcements

    Scaffold(
        topBar = {
            TopAppBar(
                title = { HtuLogo(showSubtext = true) },
                actions = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE2F1)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified Icon", tint = Color(0xFF005FB0), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF005FB0))
                        }
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search Tab") },
                    label = { Text("Search Portal") },
                    modifier = Modifier.testTag("nav_search")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (downloadedDocIds.isNotEmpty()) {
                                    Badge { Text(downloadedDocIds.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = "Downloads Tab")
                        }
                    },
                    label = { Text("Offline Books") },
                    modifier = Modifier.testTag("nav_downloads")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                val unread = recentNotifications.filter { !it.isRead }.size
                                if (unread > 0) {
                                    Badge { Text(unread.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Bulletins Tab")
                        }
                    },
                    label = { Text("HTU Board") },
                    modifier = Modifier.testTag("nav_board")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                0 -> BrowseQuestionsTab(
                    questions = questions,
                    featured = featuredQuestions,
                    searchQuery = searchQuery,
                    selectedProg = selectedProg,
                    selectedSem = selectedSemester,
                    downloadingIds = downloadingIds,
                    downloadedDocIds = downloadedDocIds,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onProgSelect = { viewModel.updateProgrammeFilter(it) },
                    onSemSelect = { viewModel.updateSemesterFilter(it) },
                    onDownload = { viewModel.downloadQuestion(it) },
                    onRead = { viewModel.setViewingQuestion(it) },
                    userFullName = user?.fullName ?: "Student"
                )
                1 -> DownloadsTab(
                    questions = questions.filter { downloadedDocIds.contains(it.id) },
                    onRead = { viewModel.setViewingQuestion(it) }
                )
                2 -> AnnouncementsTab(
                    notifications = recentNotifications,
                    onMarkRead = { viewModel.markNotificationsAsRead() }
                )
            }

            // Simulated interactive Full-Screen Exam Paper Reader overlay
            activeViewingQuestion?.let { paper ->
                ExamPaperReaderDialog(
                    paper = paper,
                    onClose = { viewModel.setViewingQuestion(null) },
                    onLike = { viewModel.likeQuestion(paper.id) }
                )
            }
        }
    }
}

@Composable
fun MinimalistFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF005FB0) else Color(0xFFDDE2F1))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF1A1C1E)
        )
    }
}

@Composable
fun BrowseQuestionsTab(
    questions: List<PastQuestion>,
    featured: List<PastQuestion>,
    searchQuery: String,
    selectedProg: String?,
    selectedSem: Int?,
    downloadingIds: Set<Int>,
    downloadedDocIds: Set<Int>,
    onSearchChange: (String) -> Unit,
    onProgSelect: (String?) -> Unit,
    onSemSelect: (Int?) -> Unit,
    onDownload: (PastQuestion) -> Unit,
    onRead: (PastQuestion) -> Unit,
    userFullName: String
) {
    val programmes = listOf(
        "All Programmes",
        "BTech Computer Science",
        "HND Civil Engineering",
        "BTech Hospitality Management",
        "BTech Accountancy",
        "HND Fashion Design & Textiles"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcoming card (Clean Minimalism custom aesthetic card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Agoo! Hello $userFullName",
                        color = Color(0xFF005FB0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access verified course past papers, examiners guides, and notes directly offline.",
                        color = Color(0xFF43474E),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Search text-field (Strict Match of search container in spec)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("search_bar"),
                placeholder = { Text("Search by course code or title...", color = Color(0xFF73777F), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFF43474E)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFF43474E))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3FA),
                    unfocusedContainerColor = Color(0xFFF3F3FA),
                    focusedBorderColor = Color(0xFF005FB0),
                    unfocusedBorderColor = Color(0xFFDDE2F1),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E)
                )
            )
        }

        // Category Scroll filter chip row
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Departments & programmes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005FB0),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    programmes.forEach { prog ->
                        val isSelected = selectedProg == prog
                        MinimalistFilterChip(
                            text = prog,
                            selected = isSelected,
                            onClick = { onProgSelect(prog) }
                        )
                    }
                }
            }
        }

        // Semester selector filter chips
        item {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "Academic Semester Filter",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF005FB0),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to "All Semesters",
                        1 to "First Semester",
                        2 to "Second Semester"
                    ).forEach { (valSem, name) ->
                        val isSelected = selectedSem == valSem
                        MinimalistFilterChip(
                            text = name,
                            selected = isSelected,
                            onClick = { onSemSelect(valSem) }
                        )
                    }
                }
            }
        }

        // Featured slides if empty querying
        if (searchQuery.isBlank() && selectedProg == "All Programmes" && featured.isNotEmpty()) {
            item {
                Text(
                    text = "Recommended for You",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    featured.forEach { pq ->
                        FeaturedPaperCard(
                            pq = pq,
                            isDownloading = downloadingIds.contains(pq.id),
                            isDownloaded = downloadedDocIds.contains(pq.id),
                            onDownload = { onDownload(pq) },
                            onRead = { onRead(pq) }
                        )
                    }
                }
            }
        }

        // Header of listings
        item {
            Text(
                text = "Available Exam Papers (${questions.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Empty state view
        if (questions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFE0E0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindInPage,
                            contentDescription = "Empty icon",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF410002)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No questions match your current search constraints.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check spelling or switch the Programme Filters above.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF73777F)
                    )
                }
            }
        }

        // Full items list
        items(questions) { pq ->
            PastQuestionCard(
                pq = pq,
                isDownloading = downloadingIds.contains(pq.id),
                isDownloaded = downloadedDocIds.contains(pq.id),
                onDownload = { onDownload(pq) },
                onRead = { onRead(pq) }
            )
        }
    }
}

@Composable
fun FeaturedPaperCard(
    pq: PastQuestion,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onRead: () -> Unit
) {
    val upperCode = pq.courseCode.uppercase()
    val (bgColor, iconColor) = when {
        upperCode.contains("BENG") || upperCode.contains("ENG") || upperCode.contains("CIV") -> Color(0xFFF0E0FF) to Color(0xFF21005D)
        upperCode.contains("COMP") || upperCode.contains("INF") || upperCode.contains("DAT") -> Color(0xFFE0F2FF) to Color(0xFF001D35)
        upperCode.contains("ARCD") || upperCode.contains("FASH") || upperCode.contains("TEX") -> Color(0xFFFFE0E0) to Color(0xFF410002)
        else -> Color(0xFFF3F3FA) to Color(0xFF005FB0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0)),
        modifier = Modifier
            .width(280.dp)
            .height(115.dp)
            .clickable { onRead() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pq.courseCode}: ${pq.courseName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C1E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${pq.academicYear} • ${pq.fileType} • ${pq.fileSize}",
                    fontSize = 11.sp,
                    color = Color(0xFF73777F)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(contentAlignment = Alignment.Center) {
                if (isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, color = Color(0xFF005FB0))
                } else {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Filled.CheckCircle else Icons.Filled.Download,
                            contentDescription = "Download Paper",
                            tint = if (isDownloaded) Color(0xFF2E7D32) else Color(0xFF005FB0),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastQuestionCard(
    pq: PastQuestion,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onRead: () -> Unit
) {
    val upperCode = pq.courseCode.uppercase()
    val (bgColor, iconColor) = when {
        upperCode.contains("BENG") || upperCode.contains("ENG") || upperCode.contains("CIV") -> Color(0xFFF0E0FF) to Color(0xFF21005D)
        upperCode.contains("COMP") || upperCode.contains("INF") || upperCode.contains("DAT") -> Color(0xFFE0F2FF) to Color(0xFF001D35)
        upperCode.contains("ARCD") || upperCode.contains("FASH") || upperCode.contains("TEX") -> Color(0xFFFFE0E0) to Color(0xFF410002)
        else -> Color(0xFFF3F3FA) to Color(0xFF005FB0)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onRead() }
            .testTag("past_question_${pq.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pq.courseCode}: ${pq.courseName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${pq.programme} • Sem ${pq.semester}",
                    fontSize = 11.sp,
                    color = Color(0xFF43474E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pq.academicYear} • ${pq.fileType} • ${pq.fileSize}",
                    fontSize = 11.sp,
                    color = Color(0xFF73777F)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onRead, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Read Booklet",
                        tint = Color(0xFF005FB0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = Color(0xFF005FB0))
                    } else {
                        IconButton(onClick = onDownload) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Filled.CheckCircle else Icons.Filled.Download,
                                contentDescription = "Download Booklet",
                                tint = if (isDownloaded) Color(0xFF2E7D32) else Color(0xFF005FB0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadsTab(
    questions: List<PastQuestion>,
    onRead: (PastQuestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.OfflinePin, contentDescription = "Offline icon", tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Offine Revision Vault Enabled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                    Text("These saved exam papers are running directly from your device storage cache. No cellular data needed.", fontSize = 11.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        Text(
            text = "Downloaded Booklet Library (${questions.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Zero items", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your Vault is Empty", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Go search available subjects and tap Download to save them offline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(questions) { pq ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRead(pq) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "File Booklet", tint = HTUNavy, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pq.courseCode, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HTUNavy)
                                Text(pq.courseName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Year: ${pq.academicYear} | Size: ${pq.fileSize}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { onRead(pq) },
                                colors = ButtonDefaults.buttonColors(containerColor = HTUNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = "Open", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open PDF", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsTab(
    notifications: List<Notification>,
    onMarkRead: () -> Unit
) {
    LaunchedEffect(Unit) {
        onMarkRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "HTU Past Questions Bulletin Board",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No recent notifications", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HTUNavy)
                                Text(
                                    text = if (!notif.isRead) "NEW" else "",
                                    color = HTURedTorch,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(notif.message, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamPaperReaderDialog(
    paper: PastQuestion,
    onClose: () -> Unit,
    onLike: () -> Unit
) {
    var zoomScale by remember { mutableStateOf(1f) }
    var helpfulVotes by remember { mutableStateOf(paper.likesCount) }
    var voted by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
            .testTag("exam_reader_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HTUNavy)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(paper.courseCode, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                        Text(paper.fileName, fontSize = 11.sp, color = HTUGold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (!voted) {
                                onLike()
                                helpfulVotes++
                                voted = true
                            }
                        }
                    ) {
                        Icon(
                            if (voted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (voted) HTUGold else Color.White
                        )
                    }
                    Text(helpfulVotes.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // PDF Reader Controls (Zoom simulation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom Icon", modifier = Modifier.size(16.dp), tint = HTUNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reader Zoom", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = zoomScale,
                    onValueChange = { zoomScale = it },
                    valueRange = 0.8f..2.0f,
                    modifier = Modifier.width(200.dp)
                )
                Text(String.format("%.1fx", zoomScale), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HTUNavy)
            }

            // Paper Body Sheet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFEBEFF5)) // Simulated PDF canvas background
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = paper.previewQuestions,
                            fontFamily = FontFamily.Monospace,
                            fontSize = (13 * zoomScale).sp,
                            color = Color(0xFF1E293B), // Dark text for readability on off-white
                            lineHeight = (18 * zoomScale).sp
                        )
                    }
                }
            }

            // Bottom action footer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Secured", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HTU SECURED EXAM VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Text("Encrypted syllabus content copy protection is active.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = HTUNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Finish Study session", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
