package com.swipeclean.app.discord

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Thrown when Discord rejects the token (401) — the session should reconnect. */
class DiscordUnauthorized : Exception("Discord token rejected")

/** A Discord server (guild) the user is a member of. */
data class Guild(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val memberCount: Long?,
    val isOwner: Boolean,
)

/**
 * Minimal Discord REST client: OAuth (PKCE via Custom Tabs), guild listing,
 * and leaving servers. Uses HttpURLConnection + org.json to stay dependency-free.
 * The access token is kept in SharedPreferences — fine for a side-loaded personal
 * tool, do NOT ship this to the Play Store like this.
 */
class DiscordRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("discord", Context.MODE_PRIVATE)

    val needsSetup: Boolean get() = DiscordConfig.needsSetup

    fun hasToken(): Boolean = !prefs.getString(KEY_TOKEN, null).isNullOrEmpty()

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    /** Opens the Discord authorization page in a Chrome Custom Tab. */
    fun launchAuthorization(activity: Activity) {
        val (verifier, challenge) = DiscordOAuth.pkcePair()
        prefs.edit().putString(KEY_VERIFIER, verifier).apply()
        val url = DiscordOAuth.authorizationUrl(challenge)
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(activity, Uri.parse(url))
    }

    /** Exchanges the OAuth redirect code for an access token (PKCE). */
    suspend fun exchangeCode(code: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = buildString {
                append("grant_type=authorization_code")
                append("&code=${Uri.encode(code)}")
                append("&redirect_uri=${Uri.encode(DiscordConfig.REDIRECT_URI)}")
                append("&client_id=${Uri.encode(DiscordConfig.CLIENT_ID)}")
                append("&code_verifier=${Uri.encode(prefs.getString(KEY_VERIFIER, "") ?: "")}")
                if (DiscordConfig.CLIENT_SECRET.isNotEmpty()) {
                    append("&client_secret=${Uri.encode(DiscordConfig.CLIENT_SECRET)}")
                }
            }.toByteArray(Charsets.UTF_8)

            val conn = open(DiscordOAuth.TOKEN_URL, "POST").apply {
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
            }
            conn.outputStream.use { it.write(body) }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val token = JSONObject(response).getString("access_token")
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }
    }

    /** All guilds the authorized user is in (newest name first is up to the deck). */
    fun loadGuilds(): List<Guild> {
        val token = prefs.getString(KEY_TOKEN, null)
            ?: throw IllegalStateException("Not connected to Discord")

        val conn = open("https://discord.com/api/users/@me/guilds?with_counts=true", "GET").apply {
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 15_000
            readTimeout = 15_000
        }

        val code = conn.responseCode
        if (code == 401) {
            conn.disconnect()
            clearToken()
            throw DiscordUnauthorized()
        }
        if (code !in 200..299) {
            conn.disconnect()
            error("Discord API error $code")
        }

        val raw = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()

        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val icon = obj.optString("icon").takeIf { it.isNotEmpty() }
            Guild(
                id = obj.getString("id"),
                name = obj.getString("name"),
                iconUrl = icon?.let { "https://cdn.discordapp.com/icons/${obj.getString("id")}/$it.png?size=128" },
                memberCount = if (obj.has("approximate_member_count")) obj.getLong("approximate_member_count") else null,
                isOwner = obj.optBoolean("owner"),
            )
        }
    }

    /** Leaves a server. Discord does NOT ask for confirmation — the 2s undo in the app is the safety net. */
    fun leaveGuild(guildId: String) {
        val token = prefs.getString(KEY_TOKEN, null)
            ?: throw IllegalStateException("Not connected to Discord")

        val conn = open("https://discord.com/api/users/@me/guilds/$guildId", "DELETE").apply {
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 15_000
            readTimeout = 15_000
        }
        val code = conn.responseCode
        conn.disconnect()
        when (code) {
            in 200..299 -> Log.d(TAG, "left guild $guildId")
            401 -> throw DiscordUnauthorized()
            else -> error("Discord API error $code")
        }
    }

    private fun open(url: String, method: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply { requestMethod = method }

    companion object {
        private const val TAG = "DiscordRepo"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_VERIFIER = "code_verifier"
    }
}
