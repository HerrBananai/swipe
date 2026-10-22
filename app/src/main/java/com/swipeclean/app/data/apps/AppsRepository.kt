package com.swipeclean.app.data.apps

import android.content.ActivityNotFoundException
import android.content.ApplicationInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.swipeclean.app.util.ActivityProvider

/** Lists launchable apps and asks the system to uninstall them. */
class AppsRepository(private val context: Context) {

    fun loadApps(): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { info ->
                AppEntry(
                    packageName = info.packageName,
                    label = info.loadLabel(pm)?.toString() ?: info.packageName,
                    icon = info.loadIcon(pm),
                    isSystemApp = (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /**
     * Opens the system uninstall confirmation dialog. Android does not let normal apps
     * uninstall silently, so the user always confirms.
     */
    fun requestUninstall(packageName: String) {
        val activity = ActivityProvider.activity ?: run {
            Log.w(TAG, "No activity available to launch uninstall for $packageName")
            return
        }
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Could not launch uninstall for $packageName", e)
        }
    }

    companion object {
        private const val TAG = "AppsRepository"
    }
}
