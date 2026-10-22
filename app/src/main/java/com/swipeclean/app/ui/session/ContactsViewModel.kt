package com.swipeclean.app.ui.session

import android.app.Application
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.swipeclean.app.data.contacts.ContactPerson
import com.swipeclean.app.data.contacts.ContactsRepository

/** Swipe session over the device's contacts. Left swipe = delete contact. */
class ContactsViewModel(application: Application) :
    SwipeSessionViewModel<ContactPerson>(application) {

    private val repository = ContactsRepository(application)

    override suspend fun loadItems(): List<ContactPerson> = repository.loadContacts()

    override suspend fun executeRemoval(item: ContactPerson) {
        repository.deleteContact(item.id)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ContactsViewModel(get<Application>()) }
        }
    }
}
