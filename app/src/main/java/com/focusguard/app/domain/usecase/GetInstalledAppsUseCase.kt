package com.focusguard.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.focusguard.app.domain.model.AppInfo
import com.focusguard.app.domain.repository.AppRestrictionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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

        // Use MATCH_ALL only on older APIs; on API 30+ PackageManager visibility rules apply
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            0
        }
        val resolvedApps: List<ResolveInfo> = packageManager.queryIntentActivities(launchIntent, flags)

        // .first() takes one snapshot from the Room Flow and cancels immediately,
        // avoiding the infinite-hang that .collect{} would cause on a never-completing Flow.
        val restrictedPackages = restrictionRepository.observeAll()
            .first()
            .map { it.packageName }
            .toSet()

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
