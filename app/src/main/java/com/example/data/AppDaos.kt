package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int
}

@Dao
interface PastQuestionDao {
    @Query("SELECT * FROM past_questions ORDER BY uploadTimestamp DESC")
    fun getAllQuestions(): Flow<List<PastQuestion>>

    @Query("SELECT * FROM past_questions WHERE courseCode LIKE '%' || :query || '%' OR courseName LIKE '%' || :query || '%' OR programme LIKE '%' || :query || '%' OR examiner LIKE '%' || :query || '%' ORDER BY uploadTimestamp DESC")
    fun searchQuestions(query: String): Flow<List<PastQuestion>>

    @Query("SELECT * FROM past_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Int): PastQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: PastQuestion): Long

    @Update
    suspend fun updateQuestion(question: PastQuestion)

    @Delete
    suspend fun deleteQuestion(question: PastQuestion)

    @Query("UPDATE past_questions SET downloadsCount = downloadsCount + 1 WHERE id = :id")
    suspend fun incrementDownloadCount(id: Int)

    @Query("UPDATE past_questions SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLikeCount(id: Int)

    @Query("SELECT * FROM past_questions WHERE isFeatured = 1")
    fun getFeaturedQuestions(): Flow<List<PastQuestion>>
}

@Dao
interface DownloadRecordDao {
    @Query("SELECT * FROM download_records ORDER BY downloadTimestamp DESC")
    fun getAllDownloadRecords(): Flow<List<DownloadRecord>>

    @Query("SELECT * FROM download_records WHERE userEmail = :email ORDER BY downloadTimestamp DESC")
    fun getDownloadRecordsByUser(email: String): Flow<List<DownloadRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadRecord(record: DownloadRecord): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
