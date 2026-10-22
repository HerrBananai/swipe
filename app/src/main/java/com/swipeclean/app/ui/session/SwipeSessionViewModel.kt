package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swipeclean.app.ui.deck.SwipeDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LoadStatus { LOADING, READY, EMPTY, ERROR }

data class SwipeStats(val kept: Int = 0, val deleted: Int = 0)

private data class PendingAction<T>(val item: T, val job: Job)

/**
 * Shared brains for every swipe mode (photos, contacts, apps).
 *
 * Swipe RIGHT = keep, swipe LEFT = delete. Left-swipes get a [GRACE_PERIOD_MS] grace
 * window before the destructive action actually executes, so the Undo button can
 * bring the card back. This mirrors the "Gmail undo" pattern and makes deletion feel safe.
 */
abstract class SwipeSessionViewModel<T : Any>(application: Application) :
    AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _status = MutableStateFlow(LoadStatus.LOADING)
    val status: StateFlow<LoadStatus> = _status.asStateFlow()

    private val _stats = MutableStateFlow(SwipeStats())
    val stats: StateFlow<SwipeStats> = _stats.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private var pending: PendingAction<T>? = null

    init {
        refresh()
    }

    /** Load the full list of items to review. Called on a background dispatcher. */
    protected abstract suspend fun loadItems(): List<T>

    /** Execute the destructive action (delete/uninstall) for a left-swiped item. */
    protected abstract suspend fun executeRemoval(item: T)

    fun refresh() {
        viewModelScope.launch {
            _status.value = LoadStatus.LOADING
            runCatching { withContext(Dispatchers.IO) { loadItems() } }
                .onSuccess { loaded ->
                    _items.value = loaded
                    _status.value = if (loaded.isEmpty()) LoadStatus.EMPTY else LoadStatus.READY
                }
                .onFailure { _status.value = LoadStatus.ERROR }
        }
    }

    /** Call when the top card has animated off-screen. */
    fun onSwiped(direction: SwipeDirection) {
        val list = _items.value
        if (list.isEmpty()) return
        val item = list.first()
        _items.value = list.drop(1)

        if (direction == SwipeDirection.RIGHT) {
            _stats.value = _stats.value.copy(kept = _stats.value.kept + 1)
            return
        }

        // Left swipe: schedule the actual deletion after a grace period so it can be undone.
        _stats.value = _stats.value.copy(deleted = _stats.value.deleted + 1)
        val job = viewModelScope.launch {
            delay(GRACE_PERIOD_MS)
            pending = null
            _canUndo.value = false
            runCatching { withContext(Dispatchers.IO) { executeRemoval(item) } }
        }
        pending = PendingAction(item, job)
        _canUndo.value = true
    }

    /** Undo the last left-swipe. Only possible during the grace period. */
    fun undoLast(): Boolean {
        val action = pending ?: return false
        pending = null
        action.job.cancel()
        _canUndo.value = false
        _items.value = listOf(action.item) + _items.value
        _stats.value = _stats.value.copy(deleted = (_stats.value.deleted - 1).coerceAtLeast(0))
        return true
    }

    companion object {
        /** How long a left-swiped item waits before it is actually deleted. */
        const val GRACE_PERIOD_MS = 2_000L
    }
}
