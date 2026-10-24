package com.swipeclean.app.data.contacts

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

/** Reads the contact list and deletes contacts on request. */
class ContactsRepository(private val context: Context) {

    private val resolver get() = context.contentResolver

    fun loadContacts(): List<ContactPerson> {
        val result = mutableListOf<ContactPerson>()
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.STARRED,
            ),
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            val photoCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
            val phoneCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            val starredCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameCol) ?: continue
                result.add(
                    ContactPerson(
                        id = cursor.getLong(idCol),
                        name = name,
                        thumbnailUri = cursor.getString(photoCol)?.let(Uri::parse),
                        hasPhoneNumber = cursor.getInt(phoneCol) > 0,
                        starred = cursor.getInt(starredCol) > 0,
                    ),
                )
            }
        }
        return result
    }

    /** Deletes the whole contact (all data rows). Requires WRITE_CONTACTS. */
    fun deleteContact(id: Long): Boolean {
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
        return resolver.delete(uri, null, null) > 0
    }
}
