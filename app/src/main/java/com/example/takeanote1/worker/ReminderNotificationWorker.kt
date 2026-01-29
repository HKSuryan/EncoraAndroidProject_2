package com.example.takeanote1.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.takeanote1.MainActivity
import com.example.takeanote1.R
import com.example.takeanote1.data.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: NotesRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "reminder_notifications"
        const val CHANNEL_NAME = "Reminder Notifications"
        const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            createNotificationChannel()

            // Get all pending reminders that should be triggered
            val currentTime = System.currentTimeMillis()
            val pendingReminders = repository.getPendingReminders(currentTime)

            // Send notification for each reminder
            pendingReminders.forEach { reminder ->
                sendNotification(reminder.id, reminder.title, reminder.description)

                // Mark reminder as notified
                repository.updateReminder(
                    reminder.copy(isNotified = true)
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Notifications for reminders"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(reminderId: Int, title: String, description: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure to add this icon
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + reminderId, notification)
    }
}