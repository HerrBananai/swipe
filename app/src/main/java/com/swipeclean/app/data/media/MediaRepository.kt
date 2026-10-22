package com.swipeclean.app.data.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * Reads images/videos from MediaStore grouped into albums (folders),
 * and trashes/deletes them on request.
 */
class MediaRepository(private val context: Context) {

    private val resolver get() = context.contentResolver

    // BUCKET_ID / BUCKET_DISPLAY_NAME live on ImageColumns but are the same columns
    // when querying the Files table (they cover both images and videos).
    private val bucketIdColumn = MediaStore.Images.ImageColumns.BUCKET_ID
    private val bucketNameColumn = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME

    /** All media albums (folders), newest first, with a cover image. */
    fun loadAlbums(): List<Album> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            bucketIdColumn,
            bucketNameColumn,
            MediaStore.Files.FileColumns.DATE_ADDED,
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?,?)"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )

        val albums = LinkedHashMap<String, MutableList<MediaItem>>()
        resolver.query(
            MediaStore.Files.getContentUri(VOLUME),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketIdCol = cursor.getColumnIndexOrThrow(bucketIdColumn)
            val bucketNameCol =
                cursor.getColumnIndexOrThrow(bucketNameColumn)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucketId = cursor.getString(bucketIdCol) ?: "unknown"
                val bucketName = cursor.getString(bucketNameCol) ?: "Unknown"
                val item = MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(filesUri(), id),
                    displayName = bucketName,
                    isVideo = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    durationMs = 0,
                    sizeBytes = 0,
                    dateAddedSec = cursor.getLong(dateCol),
                    albumId = bucketId,
                )
                val list = albums.getOrPut(bucketId) { mutableListOf() }
                if (list.size < 1) list.add(item) // first item = newest = cover
            }
        }

        return albums.map { (bucketId, covers) ->
            val cover = covers.first()
            Album(
                id = bucketId,
                name = cover.displayName,
                count = countInAlbum(bucketId),
                cover = cover,
            )
        }.sortedBy { it.name.lowercase() }
    }

    /** All photos & videos in one album, newest first. */
    fun loadMedia(albumId: String): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DURATION,
            bucketIdColumn,
        )
        val selection =
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?,?) AND ${bucketIdColumn} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            albumId,
        )

        val result = mutableListOf<MediaItem>()
        resolver.query(
            filesUri(),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val durationCol = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)
            val bucketIdCol = cursor.getColumnIndexOrThrow(bucketIdColumn)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                result.add(
                    MediaItem(
                        id = id,
                        uri = ContentUris.withAppendedId(filesUri(), id),
                        displayName = cursor.getString(nameCol) ?: "item $id",
                        isVideo = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        durationMs = if (durationCol >= 0) cursor.getLong(durationCol) else 0L,
                        sizeBytes = cursor.getLong(sizeCol),
                        dateAddedSec = cursor.getLong(dateCol),
                        albumId = cursor.getString(bucketIdCol) ?: albumId,
                    ),
                )
            }
        }
        return result
    }

    /**
     * Moves an item to the trash on Android 10+ (recoverable, asks the system to confirm
     * when required), or hard-deletes it on older versions.
     */
    fun trash(id: Long): Boolean {
        val uri = ContentUris.withAppendedId(filesUri(), id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createTrashRequest(resolver, listOf(uri), true)
            pendingIntent.send()
            true
        } else {
            resolver.delete(uri, null, null) > 0
        }
    }

    private fun countInAlbum(bucketId: String): Int {
        var count = 0
        resolver.query(
            filesUri(),
            arrayOf(MediaStore.Files.FileColumns._ID),
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?,?) AND ${bucketIdColumn} = ?",
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                bucketId,
            ),
            null,
        )?.use { count = it.count }
        return count
    }

    private fun filesUri(): Uri = MediaStore.Files.getContentUri(VOLUME)

    companion object {
        private const val VOLUME = MediaStore.VOLUME_EXTERNAL_PRIMARY
    }
}
