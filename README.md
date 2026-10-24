# SwipeClean 🗑️

A Tinder-style Android app for cleaning up your device — swipe left to **keep**, swipe right to **delete**.

Built with **Kotlin + Jetpack Compose** (Material 3, dynamic color). Videos play inline on the
card (muted by default — tap the speaker icon for sound).

## Modes

| Mode | Left swipe | Right swipe |
|---|---|---|
| **Photos & videos** | Keep | Move to trash (Android 10+ system trash, hard delete on older) |
| **Swipe everything** | Keep | Move to trash — one deck with every photo/video on the device |
| **Folder cleanup** | Keep | Trash the **whole folder** in one request |
| **Contacts** | Keep | Delete the contact |
| **App declutter** | Keep | Open the system uninstall dialog |
| **Discord servers** | Stay | Leave the server (needs setup, see below) |

Every mode has a **2-second undo window** after a delete swipe — press *Undo* before the
grace period ends and the card comes back and nothing is deleted (Gmail-style undo).

## Discord mode setup

The Discord server-swiping mode ships as a scaffold: it works once you register a tiny
Discord application of your own (5 minutes, free):

1. Go to https://discord.com/developers/applications → **New Application**
2. Copy the **Application ID** into `DiscordConfig.CLIENT_ID`
   (`app/src/main/java/com/swipeclean/app/discord/DiscordConfig.kt`)
3. In **OAuth2 → Redirects**, add `swipeclean://oauth`
4. Rebuild and install. Open *Discord servers* → *Connect with Discord*, sign in, swipe.

Notes:
- If the token exchange fails, also copy the **client secret** into `DiscordConfig.CLIENT_SECRET`
  (PKCE is used, but Discord can be picky depending on app settings).
- The access token is stored in plain `SharedPreferences` — fine for a personal side-loaded
  tool, do not ship this to the Play Store as-is.
- Right swipe leaves the server immediately after the undo grace period. Rejoining is manual
  (Discord sends no "you left" notice to the server).

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
- Discord: `INTERNET` (only used when you open the Discord mode).

Nothing ever leaves the device.

## Ideas for v2 (not built yet)

- Email triage (archive vs keep), bookmark cleanup, task cleanup
- "Large videos" smart filter, adjustable undo window
- Settings: swap swipe directions, dark/light theme toggle
- Discord: guild icons → member-level triage, per-server exceptions
