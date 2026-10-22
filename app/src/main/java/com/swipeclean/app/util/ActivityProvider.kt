package com.swipeclean.app.util

import android.app.Activity
import androidx.annotation.MainThread
import java.lang.ref.WeakReference

/**
 * Small helper that gives non-UI code (e.g. ViewModels) a safe way to launch
 * system UI flows (trash confirmations, uninstall dialogs) that require an Activity.
 */
object ActivityProvider {
    @Volatile
    private var ref: WeakReference<Activity>? = null

    val activity: Activity?
        get() = ref?.get()

    @MainThread
    fun onActivityResumed(activity: Activity) {
        ref = WeakReference(activity)
    }

    @MainThread
    fun onActivityPaused(activity: Activity) {
        if (ref?.get() === activity) ref = null
    }
}
