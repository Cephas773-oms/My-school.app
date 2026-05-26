package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.HTUGold
import com.example.ui.theme.HTUNavy
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database)

        // 2. Initialize ViewModel Factory
        val factory = PortalViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[PortalViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val screenState by viewModel.currentScreen.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()
                val otpPrompt by viewModel.otpAlert.collectAsState()

                // Automatic toast timeout
                LaunchedEffect(toastMsg) {
                    if (toastMsg != null) {
                        delay(3500)
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // --- SCREEN STATE SWITHCER ROTUER ---
                        AnimatedContent(
                            targetState = screenState,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_navigation"
                        ) { targetScreen ->
                            when (targetScreen) {
                                is Screen.Login -> {
                                    LoginScreen(viewModel = viewModel)
                                }
                                is Screen.Register -> {
                                    RegisterScreen(
                                        viewModel = viewModel,
                                        onBackToLogin = { viewModel.navigateTo(Screen.Login) }
                                    )
                                }
                                is Screen.VerifyOtp -> {
                                    VerifyOtpScreen(
                                        viewModel = viewModel,
                                        email = targetScreen.email
                                    )
                                }
                                is Screen.StudentDashboard -> {
                                    StudentDashboardScreen(viewModel = viewModel)
                                }
                                is Screen.AdminDashboard -> {
                                    AdminDashboardScreen(viewModel = viewModel)
                                }
                            }
                        }

                        // --- SIMULATED IN-APP SECURE OTP NOTIFICATION HUD ---
                        AnimatedVisibility(
                            visible = otpPrompt != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            otpPrompt?.let { text ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3FA)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE2F1)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_hud_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color(0xFFE0F2FF), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = "OTP Alert", tint = Color(0xFF005FB0))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = text,
                                            color = Color(0xFF1A1C1E),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { viewModel.closeOtpAlert() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close OTP HUD", tint = Color(0xFF73777F))
                                        }
                                    }
                                }
                            }
                        }

                        // --- CUSTOM ANIMATED FLOATING TOAST FEEDBACK ---
                        AnimatedVisibility(
                            visible = toastMsg != null,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 24.dp, vertical = 72.dp)
                                .fillMaxWidth()
                        ) {
                            toastMsg?.let { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier.testTag("in_app_feedback_toast")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Feedback icon", tint = Color(0xFF76D1FF))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = msg,
                                            color = Color(0xFFE2E2E9),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "Dismiss",
                                            color = Color(0xFF76D1FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier
                                                .clickable { viewModel.clearToast() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
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
