package com.swipeclean.app.discord

/**
 * Settings for the Discord "swipe to leave servers" mode.
 *
 * To make it work:
 *  1. Create an app at https://discord.com/developers/applications
 *  2. Copy its **Application ID** into [CLIENT_ID]
 *  3. Under OAuth2 → Redirects, add `swipeclean://oauth`
 *  4. (Optional, public clients) Copy the **client secret** into [CLIENT_SECRET]
 *     — with PKCE it is usually not needed, but fill it if Discord rejects the token exchange
 */
object DiscordConfig {

    // TODO: replace with your Discord application ID
    const val CLIENT_ID = "YOUR_DISCORD_CLIENT_ID"

    // TODO: optional — only if Discord requires it for your app
    const val CLIENT_SECRET = ""

    const val REDIRECT_URI = "swipeclean://oauth"

    val needsSetup: Boolean get() = CLIENT_ID == "YOUR_DISCORD_CLIENT_ID"
}
