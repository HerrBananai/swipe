package com.swipeclean.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swipeclean.app.ui.components.PermissionRationale
import com.swipeclean.app.ui.components.PhotoCard
import com.swipeclean.app.ui.components.SessionScaffold
import com.swipeclean.app.ui.components.StatsBar
import com.swipeclean.app.ui.deck.DeleteStamp
import com.swipeclean.app.ui.deck.KeepStamp
import com.swipeclean.app.ui.deck.SwipeDeck
import com.swipeclean.app.ui.session.PhotoViewModel
import com.swipeclean.app.ui.session.rememberMediaPermissionState

@Composable
fun PhotoSwipeScreen(
    albumId: String,
    albumName: String,
    onBack: () -> Unit,
) {
    val permissions = rememberMediaPermissionState()

    if (!permissions.granted) {
        Column(Modifier.fillMaxSize()) {
            PermissionRationale(
                title = "Allow access to your photos",
                description = "SwipeClean needs to see your photos and videos so you can swipe through them. Nothing leaves your device.",
                buttonText = "Grant access",
                onRequest = { permissions.request() },
                note = if (Build.VERSION.SDK_INT >= 34) {
                    "You can also allow access to only selected photos."
                } else {
                    null
                },
            )
        }
        return
    }

    val viewModel: PhotoViewModel = viewModel(
        key = "photo-$albumId",
        factory = PhotoViewModel.factory(albumId),
    )
    val items by viewModel.items.collectAsState()
    val status by viewModel.status.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()

    SessionScaffold(
        title = albumName,
        subtitle = "${items.size} left to review",
        status = status,
        emptyText = "All done! You reviewed every photo in this album. 🎉",
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
                leftStamp = { DeleteStamp() },
                rightStamp = { KeepStamp() },
            ) { item ->
                PhotoCard(item)
            }

            if (permissions.partial) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "Showing only your selected photos",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = { permissions.request() }) {
                            Text("Choose more photos")
                        }
                    }
                }
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
