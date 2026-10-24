package com.swipeclean.app.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swipeclean.app.ui.components.ContactCard
import com.swipeclean.app.ui.components.PermissionRationale
import com.swipeclean.app.ui.components.SessionScaffold
import com.swipeclean.app.ui.components.StatsBar
import com.swipeclean.app.ui.deck.DeleteStamp
import com.swipeclean.app.ui.deck.KeepStamp
import com.swipeclean.app.ui.deck.SwipeDeck
import com.swipeclean.app.ui.session.ContactsViewModel
import com.swipeclean.app.ui.session.rememberPermissionState

@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val permissions = rememberPermissionState(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)

    if (!permissions.granted) {
        PermissionRationale(
            title = "Allow access to your contacts",
            description = "SwipeClean needs to read your contacts so you can swipe through them. Left swipe deletes a contact — with a 2-second undo.",
            buttonText = "Grant access",
            onRequest = { permissions.request() },
        )
        return
    }

    val viewModel: ContactsViewModel = viewModel(factory = ContactsViewModel.Factory)
    val items by viewModel.items.collectAsState()
    val status by viewModel.status.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()

    SessionScaffold(
        title = "Contacts",
        subtitle = "${items.size} left to review",
        status = status,
        emptyText = "All done! No contacts left to review. 🎉",
        onBack = onBack,
        onRetry = viewModel::refresh,
    ) {
        Box(Modifier.fillMaxSize()) {
            SwipeDeck(
                items = items,
                onSwiped = viewModel::onSwiped,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                leftStamp = { KeepStamp() },
                rightStamp = { DeleteStamp() },
            ) { person ->
                ContactCard(person)
            }

            StatsBar(
                stats = stats,
                canUndo = canUndo,
                onUndo = viewModel::undoLast,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
