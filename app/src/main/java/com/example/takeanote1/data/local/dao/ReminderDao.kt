package com.example.takeanote1.data.local.dao

import androidx.room.*
import com.example.takeanote1.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    // Update operations
    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Update
    suspend fun updateReminders(reminders: List<Reminder>)

    // Delete operations
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: Int)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteRemindersByNoteId(noteId: Int)

    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun deleteAllUserReminders(userId: String)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    // Query operations - Active reminders
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 0 ORDER BY reminderDateTime ASC")
    fun getActiveReminders(userId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 0 ORDER BY reminderDateTime ASC")
    suspend fun getActiveRemindersList(userId: String): List<Reminder>

    // Query operations - Completed reminders
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY reminderDateTime DESC")
    fun getCompletedReminders(userId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY reminderDateTime DESC")
    suspend fun getCompletedRemindersList(userId: String): List<Reminder>

    // Query operations - All reminders
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY reminderDateTime ASC")
    fun getAllReminders(userId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY reminderDateTime ASC")
    suspend fun getAllRemindersList(userId: String): List<Reminder>

    // Query by note
    @Query("SELECT * FROM reminders WHERE noteId = :noteId ORDER BY reminderDateTime ASC")
    fun getRemindersByNote(noteId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE noteId = :noteId ORDER BY reminderDateTime ASC")
    suspend fun getRemindersByNoteList(noteId: Int): List<Reminder>

    // Query by ID
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Int): Reminder?

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    fun getReminderByIdFlow(reminderId: Int): Flow<Reminder?>

    // Query pending reminders (for notifications)
    @Query("SELECT * FROM reminders WHERE reminderDateTime <= :currentTime AND isNotified = 0 AND isCompleted = 0")
    suspend fun getPendingReminders(currentTime: Long): List<Reminder>

    @Query("SELECT * FROM reminders WHERE reminderDateTime BETWEEN :startTime AND :endTime AND userId = :userId ORDER BY reminderDateTime ASC")
    suspend fun getRemindersBetweenDates(userId: String, startTime: Long, endTime: Long): List<Reminder>

    @Query("SELECT * FROM reminders WHERE reminderDateTime BETWEEN :startTime AND :endTime AND userId = :userId ORDER BY reminderDateTime ASC")
    fun getRemindersBetweenDatesFlow(userId: String, startTime: Long, endTime: Long): Flow<List<Reminder>>

    // Query overdue reminders
    @Query("SELECT * FROM reminders WHERE reminderDateTime < :currentTime AND isCompleted = 0 AND userId = :userId ORDER BY reminderDateTime DESC")
    fun getOverdueReminders(userId: String, currentTime: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE reminderDateTime < :currentTime AND isCompleted = 0 AND userId = :userId ORDER BY reminderDateTime DESC")
    suspend fun getOverdueRemindersList(userId: String, currentTime: Long): List<Reminder>

    // Query upcoming reminders (next 24 hours)
    @Query("SELECT * FROM reminders WHERE reminderDateTime BETWEEN :currentTime AND :endTime AND isCompleted = 0 AND userId = :userId ORDER BY reminderDateTime ASC")
    fun getUpcomingReminders(userId: String, currentTime: Long, endTime: Long): Flow<List<Reminder>>

    // Count queries
    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isCompleted = 0")
    fun getActiveRemindersCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isCompleted = 0")
    suspend fun getActiveRemindersCountSuspend(userId: String): Int

    @Query("SELECT COUNT(*) FROM reminders WHERE reminderDateTime < :currentTime AND isCompleted = 0 AND userId = :userId")
    fun getOverdueRemindersCount(userId: String, currentTime: Long): Flow<Int>

    // Search reminders
    @Query("SELECT * FROM reminders WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY reminderDateTime ASC")
    fun searchReminders(userId: String, query: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY reminderDateTime ASC")
    suspend fun searchRemindersList(userId: String, query: String): List<Reminder>

    // Mark as complete
    @Query("UPDATE reminders SET isCompleted = 1, updatedAt = :updatedAt WHERE id = :reminderId")
    suspend fun markReminderComplete(reminderId: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET isCompleted = 0, updatedAt = :updatedAt WHERE id = :reminderId")
    suspend fun markReminderIncomplete(reminderId: Int, updatedAt: Long = System.currentTimeMillis())

    // Mark as notified
    @Query("UPDATE reminders SET isNotified = 1, updatedAt = :updatedAt WHERE id = :reminderId")
    suspend fun markReminderNotified(reminderId: Int, updatedAt: Long = System.currentTimeMillis())

    // Bulk operations
    @Query("UPDATE reminders SET isCompleted = 1, updatedAt = :updatedAt WHERE id IN (:reminderIds)")
    suspend fun markRemindersComplete(reminderIds: List<Int>, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM reminders WHERE id IN (:reminderIds)")
    suspend fun deleteReminders(reminderIds: List<Int>)

    // Get today's reminders
    @Query("SELECT * FROM reminders WHERE userId = :userId AND reminderDateTime BETWEEN :startOfDay AND :endOfDay ORDER BY reminderDateTime ASC")
    fun getTodayReminders(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND reminderDateTime BETWEEN :startOfDay AND :endOfDay ORDER BY reminderDateTime ASC")
    suspend fun getTodayRemindersList(userId: String, startOfDay: Long, endOfDay: Long): List<Reminder>
}