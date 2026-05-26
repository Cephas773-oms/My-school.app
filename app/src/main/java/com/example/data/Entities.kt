package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val fullName: String,
    val studentId: String,
    val role: String, // "STUDENT" or "ADMIN"
    val isVerified: Boolean = false,
    val verificationCode: String = "",
    val passwordHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "past_questions")
data class PastQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCode: String,
    val courseName: String,
    val programme: String,
    val academicYear: String,
    val semester: Int, // 1 or 2
    val examiner: String,
    val fileType: String, // "PDF", "DOCX", "PNG"
    val fileName: String,
    val fileSize: String,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val downloadsCount: Int = 0,
    val likesCount: Int = 0,
    val isFeatured: Boolean = false,
    val previewQuestions: String = "" // Multi-line string simulating the full syllabus preview/quiz
) : Serializable

@Entity(tableName = "download_records")
data class DownloadRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionId: Int,
    val courseCode: String,
    val courseName: String,
    val userEmail: String,
    val downloadTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Serializable
