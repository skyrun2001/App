package com.focusguard.app.presentation.navigation

/** All navigation destinations in the app. */
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AppList : Screen("app_list")
    data object Settings : Screen("settings")

    /** Detail / edit screen; [packageName] is URL-encoded before use. */
    data object RestrictionEdit : Screen("restriction_edit/{packageName}") {
        fun createRoute(packageName: String): String = "restriction_edit/$packageName"
    }
}
