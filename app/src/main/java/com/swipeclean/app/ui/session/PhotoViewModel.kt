package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.data.media.MediaItem
import com.swipeclean.app.data.media.MediaRepository

/** Swipe session over the photos and videos of one album. */
class PhotoViewModel(
    application: Application,
    private val albumId: String,
) : SwipeSessionViewModel<MediaItem>(application) {

    private val repository = MediaRepository(application)

    override suspend fun loadItems(): List<MediaItem> = repository.loadMedia(albumId)

    override suspend fun executeRemoval(item: MediaItem) {
        repository.trash(item.id)
    }

    companion object {
        fun factory(albumId: String) = viewModelFactory {
            initializer { PhotoViewModel(get<Application>(), albumId) }
        }
    }
}
