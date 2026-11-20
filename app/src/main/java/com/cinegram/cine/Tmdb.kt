package com.cinegram.cine

import com.tmplayer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Consultas ao TMDB. Devolve null quando falta a chave ou a rede falha,
 * e a tela cai de volta nas cores chapadas.
 */
object Tmdb {
    private const val BASE = "https://api.themoviedb.org/3"
    private const val IMAGE = "https://image.tmdb.org/t/p"

    private suspend fun get(path: String): String? = withContext(Dispatchers.IO) {
        val key = BuildConfig.TMDB_API_KEY
        if (key.isBlank()) return@withContext null
        runCatching {
            val url = URL("$BASE$path${if (path.contains('?')) '&' else '?'}api_key=$key&language=pt-BR")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull()
    }

    suspend fun backdrop(tvId: Int): String? {
        val body = get("/tv/$tvId") ?: return null
        val path = runCatching { JSONObject(body).optString("backdrop_path") }.getOrNull()
        return if (path.isNullOrEmpty()) null else "$IMAGE/w1280$path"
    }

    suspend fun stills(tvId: Int, season: Int): List<String> {
        val body = get("/tv/$tvId/season/$season") ?: return emptyList()
        return runCatching {
            val episodes = JSONObject(body).optJSONArray("episodes") ?: return emptyList()
            (0 until episodes.length()).mapNotNull { i ->
                episodes.optJSONObject(i)
                    ?.optString("still_path")
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { "$IMAGE/w780$it" }
            }
        }.getOrDefault(emptyList())
    }
}
