package com.dev.silverchat.helpers

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val fullFormattedTime = SimpleDateFormat("d MMM\nh:mm a", Locale.US)
    private val onlyTime = SimpleDateFormat("h:mm a", Locale.US)
    private val onlyDate = SimpleDateFormat("d MMM", Locale.US)

    fun getFormattedTimeLatestMessage(timeInMillis: Long): String {
        val date = Date(timeInMillis)

        return when {
            isToday(date) -> onlyTime.format(date)
            isYesterday(date) -> "Yesterday"
            else -> onlyDate.format(date)
        }

    }

    fun getFormattedTimeChatLog(timeInMillis: Long): String {
        val date = Date(timeInMillis)

        return when {
            isToday(date) -> onlyTime.format(date)
            else -> fullFormattedTime.format(date)
        }

    }

    private fun isYesterday(d: Date): Boolean {
        return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
    }

    private fun isToday(d: Date): Boolean {
        return DateUtils.isToday(d.time)
    }
}