package com.focusguard.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a time range during which an app is allowed to be used.
 * Supports windows that cross midnight (e.g. 22:00–06:00).
 */
@Serializable
data class TimeWindow(
    val id: Long = 0L,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
) {
    /** Returns true if the given [hour]:[minute] falls within this window. */
    fun contains(hour: Int, minute: Int): Boolean {
        val currentMinutes = hour * 60 + minute
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Window crosses midnight
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    fun formattedStart(): String = "%02d:%02d".format(startHour, startMinute)

    fun formattedEnd(): String = "%02d:%02d".format(endHour, endMinute)

    override fun toString(): String = "${formattedStart()} – ${formattedEnd()}"
}
