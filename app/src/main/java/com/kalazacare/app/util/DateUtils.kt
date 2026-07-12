package com.kalazacare.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DateUtils {

    private val DATE_FORMATTER     = DateTimeFormatter.ofPattern("d/M/yyyy")
    private val DATE_LONG          = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val TIME_FORMATTER     = DateTimeFormatter.ofPattern("hh:mm a")
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a")
    private val MONTH_YEAR         = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatDate(date: LocalDate): String       = date.format(DATE_FORMATTER)
    fun formatDateLong(date: LocalDate): String   = date.format(DATE_LONG)
    fun formatTime(time: LocalTime): String       = time.format(TIME_FORMATTER)
    fun formatMonthYear(date: LocalDate): String  = date.format(MONTH_YEAR)
    fun formatDateTime(dt: LocalDateTime): String = dt.format(DATETIME_FORMATTER)

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun timeAgo(dt: LocalDateTime): String {
        val now = LocalDateTime.now()
        val minutes = java.time.Duration.between(dt, now).toMinutes()
        return when {
            minutes < 1   -> "Just now"
            minutes < 60  -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else           -> "${minutes / 1440}d ago"
        }
    }
}
