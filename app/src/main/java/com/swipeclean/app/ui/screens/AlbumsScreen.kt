package com.swipeclean.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swipeclean.app.ui.components.AlbumCover
import com.swipeclean.app.ui.session.AlbumsViewModel
import com.swipeclean.app.ui.session.LoadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onBack: () -> Unit,
    onAlbum: (albumId: String, albumName: String) -> Unit,
) {
    val viewModel: AlbumsViewModel = viewModel(factory = AlbumsViewModel.Factory)
    val status by viewModel.status.collectAsState()
    val albums by viewModel.albums.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose an album", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (status) {
                LoadStatus.LOADING -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                LoadStatus.EMPTY, LoadStatus.ERROR -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        if (status == LoadStatus.EMPTY) {
                            "No photos or videos found on this device."
                        } else {
                            "Could not read your gallery."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                LoadStatus.READY -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCover(
                            item = album.cover,
                            name = album.name,
                            count = album.count,
                            modifier = Modifier.clickable { onAlbum(album.id, album.name) },
                        )
                    }
                }
            }
        }
    }
}
