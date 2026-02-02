package com.example.takeanote1.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.takeanote1.R

class NoteReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getString(NOTE_ID_KEY) ?: return Result.failure()
        val title = inputData.getString(NOTE_TITLE_KEY) ?: "Reminder"
        val content = inputData.getString(NOTE_CONTENT_KEY) ?: "You have a note to check."

        Log.d("NoteReminderWorker", "Running worker for noteId=$noteId")

        try {
            showNotification(noteId, title, content)
        } catch (e: Exception) {
            Log.e("NoteReminderWorker", "Failed to show notification", e)
            return Result.failure()
        }

        return Result.success()
    }

    private fun showNotification(noteId: String, title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH // High importance ensures sound/heads-up
            ).apply {
                description = "Channel for Note Reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-up notification
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        // Use unique ID per note
        notificationManager.notify(noteId.hashCode(), builder.build())
        Log.d("NoteReminderWorker", "Notification shown for noteId=$noteId")
    }

    companion object {
        const val NOTE_ID_KEY = "NOTE_ID"
        const val NOTE_TITLE_KEY = "NOTE_TITLE"
        const val NOTE_CONTENT_KEY = "NOTE_CONTENT"
        private const val CHANNEL_ID = "NOTE_REMINDER_CHANNEL"
    }
}
