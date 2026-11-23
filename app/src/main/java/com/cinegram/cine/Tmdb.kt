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
        val service: String? = null,
    ) {
        val media: String
            get() = if (isSeries) "tv" else "movie"

        val meta: String
            get() = listOfNotNull(year, genres.ifBlank { null }).joinToString("  ·  ")
    }

    data class Details(
        val rating: Double,
        val seasons: Int,
        val runtime: Int?,
    )

    data class Episode(
        val number: Int,
        val name: String,
        val overview: String,
        val still: String?,
    )

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
     * Arte oficial do titulo, aquela com a tipografia propria de cada obra.
     * Prefere portugues, depois ingles, depois qualquer idioma.
     */
    suspend fun logo(media: String, id: Int): String? {
        val body = get("/$media/$id/images?include_image_language=pt,en,null") ?: return null
        return runCatching {
            val logos = JSONObject(body).optJSONArray("logos") ?: return@runCatching null
            val found = (0 until logos.length()).mapNotNull { i ->
                val o = logos.optJSONObject(i) ?: return@mapNotNull null
                val path = o.optString("file_path")
                if (path.isEmpty()) null else o.optString("iso_639_1") to path
            }
            val chosen = found.firstOrNull { it.first == "pt" }?.second
                ?: found.firstOrNull { it.first == "en" }?.second
                ?: found.firstOrNull()?.second
            chosen?.let { "$IMAGE/w500$it" }
        }.getOrNull()
    }

    /** Nota, temporadas e duracao, usados na tela de detalhe. */
    suspend fun details(media: String, id: Int): Details? {
        val body = get("/$media/$id") ?: return null
        return runCatching {
            val o = JSONObject(body)
            Details(
                rating = o.optDouble("vote_average", 0.0),
                seasons = o.optInt("number_of_seasons", 0),
                runtime = o.optInt("runtime", 0).takeIf { it > 0 },
            )
        }.getOrNull()
    }

    suspend fun season(tvId: Int, season: Int): List<Episode> {
        val body = get("/tv/$tvId/season/$season") ?: return emptyList()
        return runCatching {
            val episodes = JSONObject(body).optJSONArray("episodes") ?: return emptyList()
            (0 until episodes.length()).mapNotNull { i ->
                val o = episodes.optJSONObject(i) ?: return@mapNotNull null
                val still = o.optString("still_path")
                Episode(
                    number = o.optInt("episode_number"),
                    name = o.optString("name"),
                    overview = o.optString("overview"),
                    still = if (still.isEmpty()) null else "$IMAGE/w300$still",
                )
            }
        }.getOrDefault(emptyList())
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

    /** Destaques da semana, usados quando ainda não há biblioteca local. */
    suspend fun trending(media: String): List<Hero> {
        val body = get("/trending/$media/week") ?: return emptyList()
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
            .joinToString("  ·  ")
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
                    poster = "$IMAGE/w500$poster",
                    backdrop = if (backdrop.isEmpty()) null else "$IMAGE/w780$backdrop",
                    overview = o.optString("overview"),
                    year = yearOf(o),
                    genres = genreNames(o.optJSONArray("genre_ids")),
                    isSeries = path.contains("/tv/"),
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun popular(media: String): List<Card> = cards("/$media/popular")

    suspend fun topRated(media: String): List<Card> = cards("/$media/top_rated")
}
