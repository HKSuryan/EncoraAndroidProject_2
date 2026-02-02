package com.example.takeanote1.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.takeanote1.workers.NoteReminderWorker
import java.util.concurrent.TimeUnit

class WorkManagerNotificationRepository(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule a notification for a note at a specific future time.
     *
     * @param noteId Unique ID of the note
     * @param title Notification title
     * @param content Notification content
     * @param triggerTimeMillis Time in milliseconds when the notification should fire
     */
    fun scheduleNotificationAt(
        noteId: String,
        title: String,
        content: String,
        triggerTimeMillis: Long
    ) {
        val now = System.currentTimeMillis()
        val delay = triggerTimeMillis - now

        // If the trigger time is in the past, do NOT schedule the notification
        if (delay <= 0) {
            Log.w("WorkManagerRepo", "Reminder time for noteId=$noteId is in the past. Skipping notification.")
            return
        }

        val data = Data.Builder()
            .putString(NoteReminderWorker.NOTE_ID_KEY, noteId)
            .putString(NoteReminderWorker.NOTE_TITLE_KEY, title)
            .putString(NoteReminderWorker.NOTE_CONTENT_KEY, content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NoteReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            // Remove .setExpedited() for production; only use for testing if needed
            .addTag("note_reminder_$noteId")
            .build()

        // Enqueue unique work: replaces any existing reminder for the same note
        workManager.enqueueUniqueWork(
            "note_reminder_$noteId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d("WorkManagerRepo", "Scheduled notification for noteId=$noteId in ${delay}ms")
    }

    /**
     * Cancel a scheduled notification for a note.
     */
    fun cancelNotification(noteId: String) {
        workManager.cancelUniqueWork("note_reminder_$noteId")
        Log.d("WorkManagerRepo", "Cancelled worker for noteId=$noteId")
    }
}
