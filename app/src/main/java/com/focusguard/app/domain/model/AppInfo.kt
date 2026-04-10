package com.focusguard.app.domain.model

import android.graphics.drawable.Drawable

/**
 * Lightweight representation of an installed app used in the app-picker list.
 *
 * [icon] is intentionally kept as an Android [Drawable] so the domain model
 * stays decoupled from a specific image-loading library while still being
 * usable directly from Compose via `rememberDrawablePainter` helpers.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val hasRestriction: Boolean = false,
)
