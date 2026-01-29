package com.example.takeanote1.util

import java.text.SimpleDateFormat
import java.util.*

object ReminderUtils {

    //Formats a timestamp to a readable date string
    fun formatDate(timestamp: Long, pattern: String = "MMM dd, yyyy"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    //Formats a timestamp to a readable time string
    fun formatTime(timestamp: Long, pattern: String = "hh:mm a"): String {
        val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    //Formats a timestamp to a full date and time string
    fun formatDateTime(timestamp: Long, pattern: String = "MMM dd, yyyy 'at' hh:mm a"): String {
        val dateTimeFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateTimeFormat.format(Date(timestamp))
    }

    //Checks if a reminder is overdue
    fun isOverdue(reminderDateTime: Long): Boolean {
        return reminderDateTime < System.currentTimeMillis()
    }

    // Gets the start of today (00:00:00)
    fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Gets the end of today (23:59:59)
     */
    fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Gets timestamp for a specific number of days from now
     */
    fun getDaysFromNow(days: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, days)
        }.timeInMillis
    }

    /**
     * Gets timestamp for a specific number of hours from now
     */
    fun getHoursFromNow(hours: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, hours)
        }.timeInMillis
    }

    /**
     * Checks if a timestamp is today
     */
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val reminderCalendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return calendar.get(Calendar.YEAR) == reminderCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == reminderCalendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Checks if a timestamp is tomorrow
     */
    fun isTomorrow(timestamp: Long): Boolean {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val reminderCalendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        return tomorrow.get(Calendar.YEAR) == reminderCalendar.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == reminderCalendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Gets a relative time string (e.g., "in 2 hours", "tomorrow", "overdue")
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = timestamp - now

        return when {
            diff < 0 -> "Overdue"
            isToday(timestamp) -> {
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                when {
                    hours > 0 -> "In $hours hour${if (hours > 1) "s" else ""}"
                    minutes > 0 -> "In $minutes minute${if (minutes > 1) "s" else ""}"
                    else -> "Now"
                }
            }
            isTomorrow(timestamp) -> "Tomorrow"
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = diff / (1000 * 60 * 60 * 24)
                "In $days day${if (days > 1) "s" else ""}"
            }
            else -> formatDate(timestamp, "MMM dd")
        }
    }

    /**
     * Combines date and time into a single timestamp
     */
    fun combineDateAndTime(dateInMillis: Long, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = dateInMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Extracts hour from timestamp
     */
    fun getHourFromTimestamp(timestamp: Long): Int {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * Extracts minute from timestamp
     */
    fun getMinuteFromTimestamp(timestamp: Long): Int {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(Calendar.MINUTE)
    }

    /**
     * Gets the start of a week
     */
    fun getStartOfWeek(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Gets the end of a week
     */
    fun getEndOfWeek(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Gets the start of a month
     */
    fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Gets the end of a month
     */
    fun getEndOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}