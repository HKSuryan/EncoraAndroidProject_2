package com.example.takeanote1.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
        var delay = triggerTimeMillis - now

        // Ensure delay is not negative
        if (delay < 0) delay = 0

        val data = Data.Builder()
            .putString(NoteReminderWorker.NOTE_ID_KEY, noteId)
            .putString(NoteReminderWorker.NOTE_TITLE_KEY, title)
            .putString(NoteReminderWorker.NOTE_CONTENT_KEY, content)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NoteReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Use milliseconds
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "note_reminder_$noteId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel a scheduled notification for a note.
     */
    fun cancelNotification(noteId: String) {
        workManager.cancelUniqueWork("note_reminder_$noteId")
    }
}
