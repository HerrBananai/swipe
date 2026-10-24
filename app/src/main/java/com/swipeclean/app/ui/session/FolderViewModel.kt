package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.data.media.Album
import com.swipeclean.app.data.media.MediaRepository

/**
 * Bulk folder-cleanup session: each card is a whole album, and a right swipe
 * moves every item in that folder to the system trash at once.
 */
class FolderViewModel(
    application: Application,
) : SwipeSessionViewModel<Album>(application) {

    private val repository = MediaRepository(application)

    override suspend fun loadItems(): List<Album> = repository.loadAlbums()

    override suspend fun executeRemoval(item: Album) {
        repository.trashAlbum(item.id)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                FolderViewModel(app)
            }
        }
    }
}
