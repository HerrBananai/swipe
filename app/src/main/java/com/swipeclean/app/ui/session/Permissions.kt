package com.swipeclean.app.ui.session

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Hand-rolled runtime permission helper.
 *
 * Returns whether [permissions] are currently granted plus a lambda that launches
 * the system request dialog and updates the result.
 */
@Composable
fun rememberPermissionState(vararg permissions: String): PermissionState {
    val context = LocalContext.current

    fun check(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(permissions.all { check(it) }) }

    // Re-check when returning from system settings, in case the user granted
    // the permission manually instead of via the in-app dialog.
    val lifecycleOwner = LocalContext.current as? LifecycleOwner
    if (lifecycleOwner != null) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = permissions.all { check(it) }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        granted = results.values.all { it }
    }

    return remember(permissions.toList()) {
        PermissionState(isGranted = { granted }, request = { launcher.launch(permissions.toList().toTypedArray()) })
    }
}

/**
 * Photo/video access across OS versions, including Android 14+ partial ("selected
 * photos") access, which counts as usable access.
 */
@Composable
fun rememberMediaPermissionState(): MediaPermissionState {
    val context = LocalContext.current
    val sdk = Build.VERSION.SDK_INT

    fun check(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun fullAccess() = when {
        sdk >= 33 -> check(Manifest.permission.READ_MEDIA_IMAGES) &&
            check(Manifest.permission.READ_MEDIA_VIDEO)
        else -> check(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun partialAccess() =
        sdk >= 34 && check(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

    var granted by remember {
        mutableStateOf(fullAccess() || partialAccess())
    }

    // Re-check when returning from system settings (manual grant flow).
    val lifecycleOwner = LocalContext.current as? LifecycleOwner
    if (lifecycleOwner != null) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = fullAccess() || partialAccess()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        granted = fullAccess() || partialAccess()
    }

    return remember {
        MediaPermissionState(
            isGranted = { granted },
            isPartial = { !fullAccess() && partialAccess() },
            request = { launcher.launch(requiredMediaPermissions(sdk)) },
        )
    }
}

/** The right photo/video permissions to request for this OS version. */
fun requiredMediaPermissions(sdk: Int = Build.VERSION.SDK_INT): Array<String> =
    when {
        sdk >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        )
        sdk >= 33 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
        )
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

class PermissionState internal constructor(
    private val isGranted: () -> Boolean,
    val request: () -> Unit,
) {
    val granted: Boolean get() = isGranted()
}

class MediaPermissionState internal constructor(
    private val isGranted: () -> Boolean,
    private val isPartial: () -> Boolean,
    val request: () -> Unit,
) {
    val granted: Boolean get() = isGranted()
    /** True when the user granted access to only some photos/videos (Android 14+). */
    val partial: Boolean get() = isPartial()
}
