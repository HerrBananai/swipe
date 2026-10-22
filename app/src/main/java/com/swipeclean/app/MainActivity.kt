package com.swipeclean.app

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
import androidx.compose.ui.Modifier
import com.swipeclean.app.ui.screens.AppsScreen
import com.swipeclean.app.ui.screens.AlbumsScreen
import com.swipeclean.app.ui.screens.ContactsScreen
import com.swipeclean.app.ui.screens.HomeScreen
import com.swipeclean.app.ui.screens.PhotoSwipeScreen
import com.swipeclean.app.ui.theme.SwipeCleanTheme
import com.swipeclean.app.util.ActivityProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    data object Contacts : Screen
    data object Apps : Screen
}

@Composable
private fun AppRoot() {
    val backStack = mutableStateListOf<Screen>(Screen.Home)

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
        Screen.Contacts -> ContactsScreen(onBack = ::pop)
        Screen.Apps -> AppsScreen(onBack = ::pop)
    }
}
