package com.swipeclean.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swipeclean.app.Screen

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("SwipeClean", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Swipe left to keep, right to delete.\nEverything has a 2-second undo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        ModeCard(
            icon = Icons.Outlined.PhotoLibrary,
            title = "Photos & videos",
            description = "Pick an album, then swipe through it. Deleted items go to the trash.",
            onClick = { onOpen(Screen.Albums) },
        )
        ModeCard(
            icon = Icons.Outlined.Collections,
            title = "Swipe everything",
            description = "One deck with every photo and video on the device. No album picking.",
            onClick = { onOpen(Screen.AllPhotos) },
        )
        ModeCard(
            icon = Icons.Outlined.FolderDelete,
            title = "Folder cleanup",
            description = "Swipe whole folders. Right swipe trashes everything inside at once.",
            onClick = { onOpen(Screen.Folders) },
        )
        ModeCard(
            icon = Icons.Outlined.People,
            title = "Contacts",
            description = "Swipe through your address book. Right swipe deletes the contact.",
            onClick = { onOpen(Screen.Contacts) },
        )
        ModeCard(
            icon = Icons.Outlined.DeleteSweep,
            title = "App declutter",
            description = "Find apps you never use. Right swipe opens the uninstall dialog.",
            onClick = { onOpen(Screen.Apps) },
        )
        ModeCard(
            icon = Icons.Outlined.Forum,
            title = "Discord servers",
            description = "Sign in, then swipe: right swipe leaves the server. Setup required.",
            onClick = { onOpen(Screen.Discord) },
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "More modes coming later: email triage, bookmarks, task cleanup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
