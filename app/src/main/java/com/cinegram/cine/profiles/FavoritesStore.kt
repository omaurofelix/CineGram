package com.cinegram.cine.profiles

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Favorite(
    val owner: String? = null,
    val media: String,
    val id: Int,
    val title: String,
    val poster: String?,
    val backdrop: String?,
    val overview: String,
    val year: String?,
    val genres: String,
)

/**
 * Guarda os favoritos em um arquivo JSON no armazenamento privado.
 * Cada item carrega o perfil dono, para a troca de perfil não misturar as listas.
 */
object FavoritesStore {
    private const val FILE_NAME = "favorites.json"

    private lateinit var file: File
    private val mutex = Mutex()

    private val _items = MutableStateFlow<List<Favorite>>(emptyList())
    val items: StateFlow<List<Favorite>> = _items.asStateFlow()

    fun init(context: Context) {
        file = File(context.filesDir, FILE_NAME)
        _items.value = read()
    }

    fun has(owner: String?, media: String, id: Int): Boolean =
        _items.value.any { it.owner == owner && it.media == media && it.id == id }

    suspend fun toggle(owner: String?, item: Favorite) = mutex.withLock {
        val current = _items.value
        val exists = current.any { it.owner == owner && it.media == item.media && it.id == item.id }
        val next = if (exists) {
            current.filterNot { it.owner == owner && it.media == item.media && it.id == item.id }
        } else {
            current + item.copy(owner = owner)
        }
        write(next)
        _items.value = next
    }

    private suspend fun write(list: List<Favorite>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        list.forEach { f ->
            array.put(
                JSONObject()
                    .put("owner", f.owner ?: JSONObject.NULL)
                    .put("media", f.media)
                    .put("id", f.id)
                    .put("title", f.title)
                    .put("poster", f.poster ?: JSONObject.NULL)
                    .put("backdrop", f.backdrop ?: JSONObject.NULL)
                    .put("overview", f.overview)
                    .put("year", f.year ?: JSONObject.NULL)
                    .put("genres", f.genres),
            )
        }
        file.writeText(JSONObject().put("items", array).toString())
    }

    private fun read(): List<Favorite> = runCatching {
        if (!file.exists()) return emptyList()
        val array = JSONObject(file.readText()).optJSONArray("items") ?: return emptyList()
        (0 until array.length()).mapNotNull { i ->
            val o = array.optJSONObject(i) ?: return@mapNotNull null
            Favorite(
                owner = if (o.isNull("owner")) null else o.optString("owner"),
                media = o.optString("media"),
                id = o.optInt("id"),
                title = o.optString("title"),
                poster = if (o.isNull("poster")) null else o.optString("poster"),
                backdrop = if (o.isNull("backdrop")) null else o.optString("backdrop"),
                overview = o.optString("overview"),
                year = if (o.isNull("year")) null else o.optString("year"),
                genres = o.optString("genres"),
            )
        }
    }.getOrDefault(emptyList())
}
