package com.swipeclean.app.discord

import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom

/** OAuth2 URLs and PKCE helpers for the Discord flow. */
object DiscordOAuth {

    const val AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize"
    const val TOKEN_URL = "https://discord.com/api/oauth2/token"

    /** Returns (code_verifier, code_challenge) — S256 pair for PKCE. */
    fun pkcePair(): Pair<String, String> {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        val verifier = base64Url(bytes)
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.UTF_8))
        return verifier to base64Url(digest)
    }

    fun authorizationUrl(codeChallenge: String): String =
        "$AUTHORIZE_URL" +
            "?client_id=${Uri.encode(DiscordConfig.CLIENT_ID)}" +
            "&response_type=code" +
            "&redirect_uri=${Uri.encode(DiscordConfig.REDIRECT_URI)}" +
            "&scope=${Uri.encode("identify guilds")}" +
            "&state=${randomState()}" +
            "&code_challenge=${Uri.encode(codeChallenge)}" +
            "&code_challenge_method=S256"

    private fun randomState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return base64Url(bytes)
    }

    private fun base64Url(data: ByteArray): String =
        Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

/**
 * Hand-off between the Custom Tab redirect and the Compose UI.
 * MainActivity submits the incoming `swipeclean://oauth?code=...` intent here;
 * DiscordScreen collects [code] and exchanges it, then clears it.
 */
object DiscordOAuthState {

    private val _code = MutableStateFlow<String?>(null)
    val code: StateFlow<String?> = _code.asStateFlow()

    /** Extracts the OAuth code (or error) from a VIEW intent, if it is our redirect. */
    fun submit(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme != "swipeclean" || data.host != "oauth") return
        data.getQueryParameter("error")?.let { error ->
            _code.value = "error:$error"
            return
        }
        data.getQueryParameter("code")?.let { _code.value = it }
    }

    fun clear() {
        _code.value = null
    }
}
