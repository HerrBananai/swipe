package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.data.media.Album
import com.swipeclean.app.data.media.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Loads the album (folder) list for the gallery picker. */
class AlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private val _status = MutableStateFlow(LoadStatus.LOADING)
    val status: StateFlow<LoadStatus> = _status.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _status.value = LoadStatus.LOADING
            runCatching { withContext(Dispatchers.IO) { repository.loadAlbums() } }
                .onSuccess { albums ->
                    _albums.value = albums
                    _status.value = if (albums.isEmpty()) LoadStatus.EMPTY else LoadStatus.READY
                }
                .onFailure { _status.value = LoadStatus.ERROR }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumsViewModel(get<Application>()) }
        }
    }
}
