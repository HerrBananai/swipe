package com.swipeclean.app.data.contacts

import android.net.Uri

data class ContactPerson(
    val id: Long,
    val name: String,
    val thumbnailUri: Uri?,
    val hasPhoneNumber: Boolean,
    val starred: Boolean,
)
