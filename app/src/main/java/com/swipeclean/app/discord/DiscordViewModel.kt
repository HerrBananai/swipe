package com.swipeclean.app.discord

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.ui.session.SwipeSessionViewModel
import com.swipeclean.app.util.ActivityProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session over the user's Discord servers. Right swipe = leave the server
 * (with the shared 2-second undo grace period).
 */
class DiscordViewModel(
    application: Application,
) : SwipeSessionViewModel<Guild>(application) {

    private val repository = DiscordRepository(application)

    private val _connected = MutableStateFlow(repository.hasToken())
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val needsSetup: Boolean get() = repository.needsSetup

    /** Opens the Discord authorization page in a Custom Tab. */
    fun startAuth() {
        val activity = ActivityProvider.activity
        if (activity == null) {
            _authError.value = "No screen available to open Discord — try again in a moment."
            return
        }
        _authError.value = null
        repository.launchAuthorization(activity)
    }

    /** Handles the OAuth redirect code that MainActivity picked up. */
    fun connectWithCode(rawCode: String) {
        if (rawCode.startsWith("error:")) {
            _authError.value = "Discord said no: ${rawCode.removePrefix("error:")}"
            return
        }
        _authError.value = null
        repository.exchangeCode(rawCode)
            .onSuccess {
                _connected.value = true
                refresh()
            }
            .onFailure { e ->
                _authError.value = "Token exchange failed: ${e.message ?: e::class.simpleName}"
            }
    }

    override suspend fun loadItems(): List<Guild> {
        if (!repository.hasToken()) {
            _connected.value = false
            return emptyList()
        }
        _connected.value = true
        return runCatching { repository.loadGuilds() }
            .getOrElse { e ->
                if (e is DiscordUnauthorized) {
                    _connected.value = false
                    emptyList()
                } else {
                    throw e
                }
            }
    }

    override suspend fun executeRemoval(item: Guild) {
        repository.leaveGuild(item.id)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                DiscordViewModel(app)
            }
        }
    }
}
