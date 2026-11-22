package com.cinegram.cine

import com.tmplayer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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

    data class Card(
        val id: Int,
        val title: String,
        val poster: String?,
        val backdrop: String? = null,
        val overview: String = "",
        val year: String? = null,
        val genres: String = "",
        val isSeries: Boolean = true,
    ) {
        val meta: String
            get() = listOfNotNull(
                if (isSeries) "Série" else "Filme",
                year,
                genres.ifBlank { null },
            ).joinToString(" • ")
    }

    private val GENRES = mapOf(
        28 to "Ação",
        12 to "Aventura",
        16 to "Animação",
        35 to "Comédia",
        80 to "Crime",
        99 to "Documentário",
        18 to "Drama",
        10751 to "Família",
        14 to "Fantasia",
        36 to "História",
        27 to "Terror",
        10402 to "Música",
        9648 to "Mistério",
        10749 to "Romance",
        878 to "Ficção científica",
        10770 to "Filme de TV",
        53 to "Suspense",
        10752 to "Guerra",
        37 to "Faroeste",
        10759 to "Ação e aventura",
        10762 to "Infantil",
        10763 to "Notícias",
        10764 to "Reality",
        10765 to "Sci-fi e fantasia",
        10766 to "Novela",
        10767 to "Talk show",
        10768 to "Guerra e política",
    )

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

    private fun yearOf(o: JSONObject): String? {
        val raw = o.optString("first_air_date").ifEmpty { o.optString("release_date") }
        return raw.take(4).takeIf { it.length == 4 }
    }

    private fun genreNames(ids: JSONArray?): String {
        if (ids == null) return ""
        return (0 until minOf(ids.length(), 2))
            .mapNotNull { GENRES[ids.optInt(it)] }
            .joinToString(" • ")
    }

    private suspend fun cards(path: String): List<Card> {
        val body = get(path) ?: return emptyList()
        return runCatching {
            val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
            (0 until results.length()).mapNotNull { i ->
                val o = results.optJSONObject(i) ?: return@mapNotNull null
                val poster = o.optString("poster_path")
                val backdrop = o.optString("backdrop_path")
                val title = o.optString("name").ifEmpty { o.optString("title") }
                if (poster.isEmpty() || title.isEmpty()) return@mapNotNull null
                Card(
                    id = o.optInt("id"),
                    title = title,
                    poster = "$IMAGE/w342$poster",
                    backdrop = if (backdrop.isEmpty()) null else "$IMAGE/w780$backdrop",
                    overview = o.optString("overview"),
                    year = yearOf(o),
                    genres = genreNames(o.optJSONArray("genre_ids")),
                    isSeries = path.contains("/tv/"),
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun popular(): List<Card> = cards("/tv/popular")

    suspend fun topRated(): List<Card> = cards("/tv/top_rated")
}
