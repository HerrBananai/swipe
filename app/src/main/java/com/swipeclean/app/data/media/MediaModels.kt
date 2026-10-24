package com.swipeclean.app.data.media

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val albumId: String,
)

data class Album(
    val id: String,
    val name: String,
    val count: Int,
    val cover: MediaItem?,
)
