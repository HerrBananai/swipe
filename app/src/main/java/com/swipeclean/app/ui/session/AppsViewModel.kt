package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.data.apps.AppEntry
import com.swipeclean.app.data.apps.AppsRepository

/** Swipe session over installed launchable apps. Left swipe = request uninstall. */
class AppsViewModel(application: Application) : SwipeSessionViewModel<AppEntry>(application) {

    private val repository = AppsRepository(application)

    override suspend fun loadItems(): List<AppEntry> = repository.loadApps()

    override suspend fun executeRemoval(item: AppEntry) {
        repository.requestUninstall(item.packageName)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                AppsViewModel(app)
            }
        }
    }
}
