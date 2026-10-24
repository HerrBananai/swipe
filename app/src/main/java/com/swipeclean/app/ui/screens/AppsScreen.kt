package com.swipeclean.app.ui.screens

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
import com.swipeclean.app.ui.components.AppCard
import com.swipeclean.app.ui.components.SessionScaffold
import com.swipeclean.app.ui.components.StatsBar
import com.swipeclean.app.ui.deck.DeleteStamp
import com.swipeclean.app.ui.deck.KeepStamp
import com.swipeclean.app.ui.deck.SwipeDeck
import com.swipeclean.app.ui.session.AppsViewModel

@Composable
fun AppsScreen(onBack: () -> Unit) {
    val viewModel: AppsViewModel = viewModel(factory = AppsViewModel.Factory)
    val items by viewModel.items.collectAsState()
    val status by viewModel.status.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()

    SessionScaffold(
        title = "App declutter",
        subtitle = "${items.size} left to review",
        status = status,
        emptyText = "All done! You reviewed every installed app. 🎉",
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
            ) { app ->
                AppCard(app)
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
