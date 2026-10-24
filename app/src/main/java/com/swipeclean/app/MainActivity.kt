package com.swipeclean.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.swipeclean.app.discord.DiscordOAuthState
import com.swipeclean.app.ui.screens.AppsScreen
import com.swipeclean.app.ui.screens.AlbumsScreen
import com.swipeclean.app.ui.screens.ContactsScreen
import com.swipeclean.app.ui.screens.DiscordScreen
import com.swipeclean.app.ui.screens.FolderSwipeScreen
import com.swipeclean.app.ui.screens.HomeScreen
import com.swipeclean.app.ui.screens.PhotoSwipeScreen
import com.swipeclean.app.ui.theme.SwipeCleanTheme
import com.swipeclean.app.util.ActivityProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Discord OAuth redirect (swipeclean://oauth?code=...) if we were launched by it
        DiscordOAuthState.submit(intent)
        setContent {
            SwipeCleanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DiscordOAuthState.submit(intent)
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.onActivityResumed(this)
    }

    override fun onPause() {
        ActivityProvider.onActivityPaused(this)
        super.onPause()
    }
}

/** App navigation: a tiny manual back stack (no navigation library needed). */
sealed interface Screen {
    data object Home : Screen
    data object Albums : Screen
    data class PhotoAlbum(val albumId: String, val albumName: String) : Screen
    data object AllPhotos : Screen
    data object Folders : Screen
    data object Contacts : Screen
    data object Apps : Screen
    data object Discord : Screen
}

@Composable
private fun AppRoot() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    fun push(screen: Screen) {
        backStack.add(screen)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    BackHandler(enabled = backStack.size > 1) { pop() }

    when (val screen = backStack.last()) {
        Screen.Home -> HomeScreen(onOpen = ::push)
        Screen.Albums -> AlbumsScreen(onBack = ::pop, onAlbum = { id, name ->
            push(Screen.PhotoAlbum(id, name))
        })
        is Screen.PhotoAlbum -> PhotoSwipeScreen(
            albumId = screen.albumId,
            albumName = screen.albumName,
            onBack = ::pop,
        )
        Screen.AllPhotos -> PhotoSwipeScreen(
            albumId = null,
            albumName = "All photos & videos",
            onBack = ::pop,
        )
        Screen.Folders -> FolderSwipeScreen(onBack = ::pop)
        Screen.Contacts -> ContactsScreen(onBack = ::pop)
        Screen.Apps -> AppsScreen(onBack = ::pop)
        Screen.Discord -> DiscordScreen(onBack = ::pop)
    }
}
