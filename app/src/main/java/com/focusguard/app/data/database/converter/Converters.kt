package com.focusguard.app.data.database.converter

import androidx.room.TypeConverter
import com.focusguard.app.domain.model.TimeWindow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room type converters for non-primitive fields. */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun timeWindowsToJson(windows: List<TimeWindow>): String =
        json.encodeToString(windows)

    @TypeConverter
    fun jsonToTimeWindows(value: String): List<TimeWindow> =
        json.decodeFromString(value)
}
