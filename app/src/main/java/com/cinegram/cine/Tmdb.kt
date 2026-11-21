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

    data class Hero(
        val id: Int,
        val title: String,
        val overview: String,
        val backdrop: String?,
    )

    data class Card(val id: Int, val title: String, val poster: String?)

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

    /**
     * Stills por episódio. A lista mantém o tamanho da temporada e usa null
     * no episódio sem imagem, para o índice continuar casando com o episódio.
     */
    suspend fun stills(tvId: Int, season: Int): List<String?> {
        val body = get("/tv/$tvId/season/$season") ?: return emptyList()
        return runCatching {
            val episodes = JSONObject(body).optJSONArray("episodes") ?: return emptyList()
            (0 until episodes.length()).map { i ->
                episodes.optJSONObject(i)
                    ?.optString("still_path")
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { "$IMAGE/w780$it" }
            }
        }.getOrDefault(emptyList())
    }

    /** Destaques da semana, usados no carrossel do topo da home. */
    suspend fun trending(): List<Hero> {
        val body = get("/trending/tv/week") ?: return emptyList()
        return runCatching {
            val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
            (0 until results.length()).mapNotNull { i ->
                val o = results.optJSONObject(i) ?: return@mapNotNull null
                val title = o.optString("name").ifEmpty { o.optString("title") }
                if (title.isEmpty()) return@mapNotNull null
                val path = o.optString("backdrop_path")
                Hero(
                    id = o.optInt("id"),
                    title = title,
                    overview = o.optString("overview"),
                    backdrop = if (path.isEmpty()) null else "$IMAGE/w1280$path",
                )
            }
        }.getOrDefault(emptyList())
    }

    private suspend fun cards(path: String): List<Card> {
        val body = get(path) ?: return emptyList()
        return runCatching {
            val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
            (0 until results.length()).mapNotNull { i ->
                val o = results.optJSONObject(i) ?: return@mapNotNull null
                val poster = o.optString("poster_path")
                val title = o.optString("name").ifEmpty { o.optString("title") }
                if (poster.isEmpty() || title.isEmpty()) return@mapNotNull null
                Card(id = o.optInt("id"), title = title, poster = "$IMAGE/w342$poster")
            }
        }.getOrDefault(emptyList())
    }

    suspend fun popular(): List<Card> = cards("/tv/popular")

    suspend fun topRated(): List<Card> = cards("/tv/top_rated")
}
