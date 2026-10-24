package com.swipeclean.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.swipeclean.app.discord.DiscordOAuthState
import com.swipeclean.app.discord.DiscordViewModel
import com.swipeclean.app.discord.Guild
import com.swipeclean.app.ui.components.PermissionRationale
import com.swipeclean.app.ui.components.SessionScaffold
import com.swipeclean.app.ui.components.StatsBar
import com.swipeclean.app.ui.deck.SwipeDeck
import com.swipeclean.app.ui.deck.SwipeStamp
import com.swipeclean.app.ui.session.LoadStatus

/**
 * Discord mode: connect once via OAuth, then swipe through your server list.
 * Left = stay, right = leave the server.
 */
@Composable
fun DiscordScreen(onBack: () -> Unit) {
    val viewModel: DiscordViewModel = viewModel(factory = DiscordViewModel.Factory)

    // OAuth redirect picked up by MainActivity
    val oauthCode by DiscordOAuthState.code.collectAsState()
    LaunchedEffect(oauthCode) {
        oauthCode?.let {
            DiscordOAuthState.clear()
            viewModel.connectWithCode(it)
        }
    }

    val connected by viewModel.connected.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val items by viewModel.items.collectAsState()
    val status by viewModel.status.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()

    when {
        viewModel.needsSetup -> PermissionRationale(
            title = "Connect your Discord app first",
            description = buildString {
                appendLine("SwipeClean needs its own Discord application to sign you in:")
                appendLine()
                appendLine("1. Create an app at discord.com/developers/applications")
                appendLine("2. Put its Application ID in DiscordConfig.CLIENT_ID")
                appendLine("3. Under OAuth2 → Redirects add swipeclean://oauth")
                appendLine("4. Rebuild the app")
            },
            buttonText = "Got it",
            onRequest = onBack,
        )

        !connected -> PermissionRationale(
            title = "Swipe through your servers",
            description = "Sign in with Discord, then swipe: left = stay, right = leave the server. " +
                "You get a 2-second undo before the app actually leaves. Only servers you can " +
                "leave are shown.",
            buttonText = "Connect with Discord",
            onRequest = viewModel::startAuth,
            note = authError,
        )

        else -> SessionScaffold(
            title = "Discord servers",
            subtitle = "${items.size} left to review",
            status = status,
            emptyText = if (status == LoadStatus.ERROR) {
                "Couldn't load your servers — check your connection and retry."
            } else {
                "No servers left to review. 🎉"
            },
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
                    leftStamp = { SwipeStamp("STAY", MaterialTheme.colorScheme.primary) },
                    rightStamp = { SwipeStamp("LEAVE", MaterialTheme.colorScheme.error) },
                ) { guild ->
                    GuildCard(guild)
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
}

@Composable
private fun GuildCard(guild: Guild, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        if (guild.iconUrl != null) {
            AsyncImage(
                model = guild.iconUrl,
                contentDescription = guild.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        ),
                    ),
            )
            Text(
                guild.name.take(2).uppercase(),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        if (guild.isOwner) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = MaterialTheme.shapes.small,
                color = Color.Black.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "  OWNER",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(14.dp),
        ) {
            Column {
                Text(
                    guild.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                guild.memberCount?.let {
                    Text(
                        "$it members",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    "Right swipe leaves this server",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
