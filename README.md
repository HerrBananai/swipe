# SwipeClean 🗑️

A Tinder-style Android app for cleaning up your device — swipe right to **keep**, swipe left to **delete**.

Built with **Kotlin + Jetpack Compose** (Material 3, dynamic color).

## Modes (v1)

| Mode | Right swipe | Left swipe |
|---|---|---|
| **Photos & videos** | Keep | Move to trash (Android 10+ system trash, hard delete on older) |
| **Contacts** | Keep | Delete the contact |
| **App declutter** | Keep | Open the system uninstall dialog |

Every mode has a **2-second undo window** after a delete swipe — press *Undo* before the
grace period ends and the card comes back and nothing is deleted (Gmail-style undo).

## How it works

- `ui/deck/SwipeDeck.kt` — the reusable Tinder-style card engine (drag, rotation, spring-back,
  fly-out animation, drag stamps). Any future mode (email triage, bookmarks, Discord servers…)
  can reuse it as-is.
- `ui/session/SwipeSessionViewModel.kt` — shared brain for all modes: loads items, keeps
  stats, schedules deletions with an undo grace period.
- `data/` — MediaStore, ContactsContract, and PackageManager repositories.

## Running it

1. Install **Android Studio** (Ladybug or newer): https://developer.android.com/studio
2. Open this folder (`swipe-app`) as a project and let Gradle sync.
3. Run on a device or emulator (Android 8.0 / API 26+).

The Gradle wrapper (Gradle 8.7) is included, so the first sync downloads everything it needs.

## Permissions

- Photos: `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (Android 13+), `READ_EXTERNAL_STORAGE`
  (older), plus `READ_MEDIA_VISUAL_USER_SELECTED` for Android 14+ *partial photo access*.
- Contacts: `READ_CONTACTS` + `WRITE_CONTACTS`.
- Apps: `QUERY_ALL_PACKAGES` — **note:** this permission is restricted on Google Play, so this
  app is meant to be side-loaded (debug build / APK). If you ever publish it, replace it with a
  `<queries>` manifest block.

Nothing ever leaves the device.

## Ideas for v2 (not built yet)

- Discord: OAuth + swipe through your server list (right = keep, left = leave server)
- Email triage (archive vs keep), bookmark cleanup, task cleanup
- Bulk album cleanup (swipe whole folders), "large videos" smart filter
- Settings: swap swipe directions, adjustable undo window, dark/light theme toggle
