package com.focusguard.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeWindowTest {

    // 08:00 – 18:00  (no midnight crossing)
    private val morningWindow = TimeWindow(startHour = 8, startMinute = 0, endHour = 18, endMinute = 0)

    // 22:00 – 06:00  (crosses midnight)
    private val nightWindow = TimeWindow(startHour = 22, startMinute = 0, endHour = 6, endMinute = 0)

    // ─── Normal window (no midnight crossing) ───────────────────────────────

    @Test
    fun `contains returns true for time inside normal window`() {
        assertThat(morningWindow.contains(hour = 12, minute = 0)).isTrue()
    }

    @Test
    fun `contains returns true for exact start of normal window`() {
        assertThat(morningWindow.contains(hour = 8, minute = 0)).isTrue()
    }

    @Test
    fun `contains returns true for exact end of normal window`() {
        assertThat(morningWindow.contains(hour = 18, minute = 0)).isTrue()
    }

    @Test
    fun `contains returns false for time before normal window`() {
        assertThat(morningWindow.contains(hour = 7, minute = 59)).isFalse()
    }

    @Test
    fun `contains returns false for time after normal window`() {
        assertThat(morningWindow.contains(hour = 18, minute = 1)).isFalse()
    }

    // ─── Midnight-crossing window ────────────────────────────────────────────

    @Test
    fun `contains returns true for time after start of midnight-crossing window`() {
        assertThat(nightWindow.contains(hour = 23, minute = 30)).isTrue()
    }

    @Test
    fun `contains returns true for midnight itself in midnight-crossing window`() {
        assertThat(nightWindow.contains(hour = 0, minute = 0)).isTrue()
    }

    @Test
    fun `contains returns true for time before end of midnight-crossing window`() {
        assertThat(nightWindow.contains(hour = 5, minute = 59)).isTrue()
    }

    @Test
    fun `contains returns false for time outside midnight-crossing window`() {
        assertThat(nightWindow.contains(hour = 10, minute = 0)).isFalse()
    }

    // ─── Formatting ──────────────────────────────────────────────────────────

    @Test
    fun `formattedStart pads single-digit hours and minutes`() {
        val window = TimeWindow(startHour = 8, startMinute = 5, endHour = 9, endMinute = 0)
        assertThat(window.formattedStart()).isEqualTo("08:05")
    }

    @Test
    fun `formattedEnd pads single-digit hours and minutes`() {
        val window = TimeWindow(startHour = 9, startMinute = 0, endHour = 8, endMinute = 9)
        assertThat(window.formattedEnd()).isEqualTo("08:09")
    }

    @Test
    fun `toString returns dash-separated start and end`() {
        val window = TimeWindow(startHour = 8, startMinute = 0, endHour = 18, endMinute = 0)
        assertThat(window.toString()).isEqualTo("08:00 – 18:00")
    }
}
