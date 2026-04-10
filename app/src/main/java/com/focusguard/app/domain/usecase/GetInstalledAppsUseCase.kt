package com.focusguard.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.focusguard.app.domain.model.AppInfo
import com.focusguard.app.domain.repository.AppRestrictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Returns all user-launchable apps installed on the device, enriched with
 * information about whether a restriction has been configured for each.
 */
class GetInstalledAppsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restrictionRepository: AppRestrictionRepository,
) {
    suspend operator fun invoke(): List<AppInfo> {
        val packageManager = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedApps = packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
        val restrictedPackages = restrictionRepository.observeAll()
            .let { flow ->
                // Collect once for a snapshot
                val list = mutableListOf<String>()
                flow.collect { list.addAll(it.map { r -> r.packageName }) }
                list.toSet()
            }

        return resolvedApps
            .asSequence()
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                AppInfo(
                    packageName = packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString(),
                    icon = runCatching { resolveInfo.loadIcon(packageManager) }.getOrNull(),
                    hasRestriction = packageName in restrictedPackages,
                )
            }
            .filter { it.packageName != context.packageName }
            .sortedWith(compareBy({ !it.hasRestriction }, { it.appName.lowercase() }))
            .toList()
    }
}
