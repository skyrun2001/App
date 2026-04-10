package com.focusguard.app.domain.model

/** Result of an access check for a given app at a specific point in time. */
sealed class AccessResult {
    /** The app is allowed to run right now. */
    data object Allowed : AccessResult()

    /**
     * The app is blocked.
     *
     * @property reason      Why access was denied.
     * @property stepsNeeded Populated when [reason] is [DenialReason.INSUFFICIENT_STEPS];
     *   contains how many additional steps are required.
     */
    data class Denied(
        val reason: DenialReason,
        val stepsNeeded: Int = 0,
    ) : AccessResult()
}

enum class DenialReason {
    /** The current time is outside all configured allowed windows. */
    OUTSIDE_TIME_WINDOW,

    /** The user has not yet walked enough steps today. */
    INSUFFICIENT_STEPS,

    /** The configured daily usage limit has been reached. */
    DAILY_LIMIT_EXCEEDED,
}
