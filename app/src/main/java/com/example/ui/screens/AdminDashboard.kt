package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: PortalViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val questions by viewModel.pastQuestions.collectAsState()
    val downloadLogs by viewModel.downloadLogs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Catalog & Upload, 1: Download Tracking Logs, 2: Server Traffic Metrics
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedPaperForEdit by remember { mutableStateOf<PastQuestion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { HtuLogo(showSubtext = true) },
                actions = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Icon", tint = Color(0xFFC5221F), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("System Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC5221F))
                        }
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = MaterialTheme.colorScheme.error)
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
                containerColor = Color(0xFFF3F3FA)
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Catalog Tab") },
                    label = { Text("App Catalog") },
                    modifier = Modifier.testTag("admin_nav_catalog")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Logs Tab") },
                    label = { Text("Download Audit") },
                    modifier = Modifier.testTag("admin_nav_stats")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Dns, contentDescription = "Server Tab") },
                    label = { Text("Network Traffic") },
                    modifier = Modifier.testTag("admin_nav_traffic")
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showUploadDialog = true },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Upload Book button") },
                    text = { Text("Upload Past Paper") },
                    containerColor = Color(0xFF005FB0),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_upload_paper")
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
                0 -> CatalogManagerTab(
                    questions = questions,
                    onEdit = { selectedPaperForEdit = it },
                    onDelete = { viewModel.deletePastQuestion(it) }
                )
                1 -> DownloadLogsTab(
                    logs = downloadLogs
                )
                2 -> ServerMetricsTab()
            }

            // Upload Paper Overlay Drawer Dialog
            if (showUploadDialog) {
                UploadPaperDialog(
                    onDismiss = { showUploadDialog = false },
                    onConfirm = { code, name, prog, year, sem, examiner, type, fName, fSize, syllabus ->
                        viewModel.uploadPastQuestion(code, name, prog, year, sem, examiner, type, fName, fSize, syllabus)
                        showUploadDialog = false
                    }
                )
            }

            // Edit Paper Overlay Drawer Dialog
            selectedPaperForEdit?.let { paper ->
                EditPaperDialog(
                    paper = paper,
                    onDismiss = { selectedPaperForEdit = null },
                    onConfirm = { updated ->
                        viewModel.editPastQuestion(updated)
                        selectedPaperForEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun CatalogManagerTab(
    questions: List<PastQuestion>,
    onEdit: (PastQuestion) -> Unit,
    onDelete: (PastQuestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("HTU Academic Catalog Manager", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF005FB0))
                Text("Displaying active examination question booklets currently visible to students. Modify properties or delete indexes.", fontSize = 12.sp, color = Color(0xFF43474E))
            }
        }

        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No past questions in system. Click the FAB button to add.", fontSize = 13.sp, color = Color(0xFF73777F))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(questions) { pq ->
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        text = pq.courseCode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF005FB0)
                                    )
                                    Text(
                                        text = pq.courseName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1A1C1E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { onEdit(pq) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Paper", tint = Color(0xFF005FB0), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { onDelete(pq) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC5221F), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFDDE2F1))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Programme: ${pq.programme}", fontSize = 11.sp, color = Color(0xFF43474E))
                                    Text("Year: ${pq.academicYear} | Sem: ${pq.semester} | Examiner: ${pq.examiner}", fontSize = 11.sp, color = Color(0xFF73777F))
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDDE2F1)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download counter", modifier = Modifier.size(12.dp), tint = Color(0xFF005FB0))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${pq.downloadsCount} DLs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF005FB0))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadLogsTab(logs: List<DownloadRecord>) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE0F2FF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = "Stats Tracker", tint = Color(0xFF005FB0), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Secure Download Audit Tracker", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF005FB0))
                    Text("Real-time logging of students downloading past questions, monitoring file distribution metrics.", fontSize = 11.sp, color = Color(0xFF43474E))
                }
            }
        }

        Text(
            text = "Distribution Logs History (${logs.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No students have downloaded question sheets yet.", fontSize = 13.sp, color = Color(0xFF73777F))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0F2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = "DL Log", tint = Color(0xFF005FB0), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = log.userEmail,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1A1C1E)
                                    )
                                    Text(
                                        text = "Downloaded ${log.courseCode} - ${log.courseName}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF43474E),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Text(
                                text = sdf.format(Date(log.downloadTimestamp)),
                                fontSize = 10.sp,
                                color = Color(0xFF73777F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerMetricsTab() {
    val rand = remember { Random(System.currentTimeMillis()) }

    var visitorsCount by remember { mutableStateOf(485) }
    var serverLoad by remember { mutableStateOf(24) }
    var queriesPerSec by remember { mutableStateOf(89) }
    var cacheHitRatio by remember { mutableStateOf(97) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            visitorsCount += rand.nextInt(-10, 15)
            serverLoad = rand.nextInt(18, 38)
            queriesPerSec = rand.nextInt(75, 125)
            cacheHitRatio = rand.nextInt(95, 100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("HTU Central DB Traffic Node", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF005FB0))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(4.dp)) {
                        Text("SERVER ONLINE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Scale system monitors showing parallel visitor logs, concurrent downloads capacity modeling, and SQL connection pools.", fontSize = 12.sp, color = Color(0xFF43474E))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, "Users", tint = Color(0xFF005FB0), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Students", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                    }
                    Text(visitorsCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF005FB0))
                    Text("Parallel connections", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, "Load", tint = Color(0xFFC5221F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("System Load", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                    }
                    Text("$serverLoad%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFFC5221F))
                    Text("CPU core allocation", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, "DB Queries", tint = Color(0xFF005FB0), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Queries / Sec", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                    }
                    Text(queriesPerSec.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF005FB0))
                    Text("SQL Connection Pool", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC3C7D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cached, "Cache", tint = Color(0xFF005FB0), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Query-Cache Hit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                    }
                    Text("$cacheHitRatio%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF005FB0))
                    Text("Redis Memory status", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, "Lightning", tint = Color(0xFF005FB0), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Balanced Clustering Activated", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF005FB0))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "System implements modern clustering and SQLite Write-Ahead Logging (WAL) allowing up to 5,000 independent parallel requests without structural schema blocking or API performance drops. Ideal for extreme peak conditions during End-Of-Semester exams.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF43474E)
                )
            }
        }
    }
}

@Composable
fun UploadPaperDialog(
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var prog by remember { mutableStateOf("BTech Computer Science") }
    var year by remember { mutableStateOf("2023/2024") }
    var sem by remember { mutableStateOf(1) }
    var examiner by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PDF") }
    var fileName by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf("1.2 MB") }
    var syllabus by remember { mutableStateOf("") }

    var expandedProg by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    val programmes = listOf(
        "BTech Computer Science",
        "HND Civil Engineering",
        "BTech Hospitality Management",
        "BTech Accountancy",
        "HND Fashion Design & Textiles"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Past Exam Paper", fontWeight = FontWeight.Bold, color = Color(0xFF005FB0)) },
        text = {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code (e.g. COMP 301)") }, placeholder = { Text("COMP 301") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Title") }, placeholder = { Text("Database Design") })

                // Programme selection
                Box {
                    OutlinedTextField(
                        value = prog,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Academic Programme") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown", modifier = Modifier.clickable { expandedProg = true }) },
                        modifier = Modifier.clickable { expandedProg = true }
                    )
                    DropdownMenu(expanded = expandedProg, onDismissRequest = { expandedProg = false }) {
                        programmes.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { prog = item; expandedProg = false })
                        }
                    }
                }

                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Academic Year") }, placeholder = { Text("2023/2024") })

                // Semester row selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { sem = 1 },
                        colors = ButtonDefaults.textButtonColors(containerColor = if (sem == 1) Color(0xFFDDE2F1) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Semester 1", color = if (sem == 1) Color(0xFF005FB0) else MaterialTheme.colorScheme.onSurface, fontWeight = if (sem == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(
                        onClick = { sem = 2 },
                        colors = ButtonDefaults.textButtonColors(containerColor = if (sem == 2) Color(0xFFDDE2F1) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Semester 2", color = if (sem == 2) Color(0xFF005FB0) else MaterialTheme.colorScheme.onSurface, fontWeight = if (sem == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                OutlinedTextField(value = examiner, onValueChange = { examiner = it }, label = { Text("Examiner Name") }, placeholder = { Text("Dr. Kofi Asante") })

                // File type selection
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Document format") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown", modifier = Modifier.clickable { expandedType = true }) },
                        modifier = Modifier.clickable { expandedType = true }
                    )
                    DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        listOf("PDF", "DOCX", "PNG").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { type = item; expandedType = false })
                        }
                    }
                }

                OutlinedTextField(value = fileName, onValueChange = { fileName = it }, label = { Text("File Name (Optional)") }, placeholder = { Text("comp301_db_exam.pdf") })
                OutlinedTextField(value = fileSize, onValueChange = { fileSize = it }, label = { Text("Simulated File Size") }, placeholder = { Text("1.2 MB") })

                OutlinedTextField(
                    value = syllabus,
                    onValueChange = { syllabus = it },
                    label = { Text("Questions Preview Contents") },
                    placeholder = { Text("Type full questions or section breakdowns here...") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(code, name, prog, year, sem, examiner, type, fileName, fileSize, syllabus) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Publish to Student Hub", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF43474E)) }
        }
    )
}

@Composable
fun EditPaperDialog(
    paper: PastQuestion,
    onDismiss: () -> Unit,
    onConfirm: (PastQuestion) -> Unit
) {
    var code by remember { mutableStateOf(paper.courseCode) }
    var name by remember { mutableStateOf(paper.courseName) }
    var prog by remember { mutableStateOf(paper.programme) }
    var year by remember { mutableStateOf(paper.academicYear) }
    var sem by remember { mutableStateOf(paper.semester) }
    var examiner by remember { mutableStateOf(paper.examiner) }
    var syllabus by remember { mutableStateOf(paper.previewQuestions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booklet Indexes", fontWeight = FontWeight.Bold, color = Color(0xFF005FB0)) },
        text = {
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Title") })
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Academic Year") })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { sem = 1 },
                        colors = ButtonDefaults.textButtonColors(containerColor = if (sem == 1) Color(0xFFDDE2F1) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Semester 1", color = if (sem == 1) Color(0xFF005FB0) else MaterialTheme.colorScheme.onSurface, fontWeight = if (sem == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(
                        onClick = { sem = 2 },
                        colors = ButtonDefaults.textButtonColors(containerColor = if (sem == 2) Color(0xFFDDE2F1) else Color.Transparent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Semester 2", color = if (sem == 2) Color(0xFF005FB0) else MaterialTheme.colorScheme.onSurface, fontWeight = if (sem == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                OutlinedTextField(value = examiner, onValueChange = { examiner = it }, label = { Text("Examiner Name") })
                OutlinedTextField(value = syllabus, onValueChange = { syllabus = it }, label = { Text("Preview Content") }, minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        paper.copy(
                            courseCode = code.trim().uppercase(),
                            courseName = name.trim(),
                            programme = prog,
                            academicYear = year.trim(),
                            semester = sem,
                            examiner = examiner.trim(),
                            previewQuestions = syllabus
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF43474E)) }
        }
    )
}
