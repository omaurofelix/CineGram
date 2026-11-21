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

/**
 * Guarda os perfis num arquivo JSON no armazenamento privado do app.
 * Nada sai do aparelho.
 */
object ProfileStore {

    private const val FILE_NAME = "profiles.json"
    const val MAX_PROFILES = 5

    private lateinit var file: File
    private val mutex = Mutex()

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _activeId = MutableStateFlow<String?>(null)
    val activeId: StateFlow<String?> = _activeId.asStateFlow()

    fun init(context: Context) {
        file = File(context.filesDir, FILE_NAME)
        _profiles.value = read()
        _activeId.value = _profiles.value.firstOrNull()?.id
    }

    suspend fun add(profile: Profile): Boolean = mutex.withLock {
        if (_profiles.value.size >= MAX_PROFILES) return@withLock false
        val next = _profiles.value + profile
        write(next)
        _profiles.value = next
        true
    }

    suspend fun update(profile: Profile) = mutex.withLock {
        val next = _profiles.value.map { if (it.id == profile.id) profile else it }
        write(next)
        _profiles.value = next
    }

    suspend fun remove(id: String) = mutex.withLock {
        val next = _profiles.value.filterNot { it.id == id }
        write(next)
        _profiles.value = next
        if (_activeId.value == id) _activeId.value = next.firstOrNull()?.id
    }

    fun setActive(id: String?) {
        _activeId.value = id
    }

    fun current(): Profile? = _profiles.value.firstOrNull { it.id == _activeId.value }

    private suspend fun write(list: List<Profile>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        list.forEach { p ->
            array.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("avatar", p.avatar)
                    .put("kid", p.kid)
                    .put("pin", p.pin ?: JSONObject.NULL),
            )
        }
        val root = JSONObject().put("profiles", array)
        file.writeText(root.toString())
    }

    private fun read(): List<Profile> = runCatching {
        if (!file.exists()) return emptyList()
        val array = JSONObject(file.readText()).optJSONArray("profiles") ?: return emptyList()
        (0 until array.length()).mapNotNull { i ->
            val o = array.optJSONObject(i) ?: return@mapNotNull null
            Profile(
                id = o.optString("id"),
                name = o.optString("name"),
                avatar = o.optInt("avatar"),
                kid = o.optBoolean("kid"),
                pin = if (o.isNull("pin")) null else o.optString("pin"),
            )
        }
    }.getOrDefault(emptyList())
}
