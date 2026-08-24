package mammoth.mollie.caster.data.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/** A TTL used by transient caches. Durable podcast and user records belong in Room instead. */
data class TTL(val durationMillis: Long) {
    init {
        require(durationMillis > 0) { "TTL must be positive" }
    }
}

interface CleanableStorage {
    suspend fun delete(key: String)
    suspend fun clear()
}

/**
 * Typed object storage with explicit fresh (`get`) and stale-tolerant (`peek`) read paths.
 * `get` emits null when an entry has expired; `peek` continues to expose the last known value.
 */
interface ObjectStorage : CleanableStorage {
    suspend fun <T : Any> get(key: String, kClass: KClass<T>): Flow<T?>
    suspend fun <T : Any> peek(key: String, kClass: KClass<T>): Flow<T?>
    suspend fun <T : Any> getCurrent(key: String, kClass: KClass<T>): T?
    suspend fun <T : Any> peekCurrent(key: String, kClass: KClass<T>): T?
    suspend fun peekTtl(key: String): TTL?
    suspend fun <T : Any> save(key: String, value: T, ttl: TTL)
}

/**
 * A bounded LRU cache suitable for discovery and RSS previews. It never persists user data.
 * Expired values can be inspected with `peek` for stale-while-revalidate experiences.
 */
class InMemoryObjectStorage(
    private val maxEntries: Int,
    private val nowMillis: () -> Long,
) : ObjectStorage {
    init {
        require(maxEntries > 0) { "Cache entry limit must be positive" }
    }

    private data class Entry(val value: Any, val ttl: TTL, val expiresAtMillis: Long)

    private val mutex = Mutex()
    private val entries = LinkedHashMap<String, Entry>()
    private val snapshots = MutableStateFlow<Map<String, Entry>>(emptyMap())
    private val expiryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun <T : Any> get(key: String, kClass: KClass<T>): Flow<T?> =
        snapshots.map { snapshot -> snapshot[key]?.takeUnless { it.expiresAtMillis <= nowMillis() }?.value.asType(kClass) }.distinctUntilChanged()

    override suspend fun <T : Any> peek(key: String, kClass: KClass<T>): Flow<T?> =
        snapshots.map { snapshot -> snapshot[key]?.value.asType(kClass) }.distinctUntilChanged()

    override suspend fun <T : Any> getCurrent(key: String, kClass: KClass<T>): T? = mutex.withLock {
        val entry = entries[key] ?: return@withLock null
        if (entry.expiresAtMillis <= nowMillis()) return@withLock null
        touch(key, entry)
        entry.value.asType(kClass)
    }

    override suspend fun <T : Any> peekCurrent(key: String, kClass: KClass<T>): T? = mutex.withLock {
        entries[key]?.let { entry ->
            touch(key, entry)
            entry.value.asType(kClass)
        }
    }

    override suspend fun peekTtl(key: String): TTL? = mutex.withLock { entries[key]?.ttl }

    override suspend fun <T : Any> save(key: String, value: T, ttl: TTL) {
        val expiresAt = nowMillis() + ttl.durationMillis
        mutex.withLock {
            entries.remove(key)
            entries[key] = Entry(value, ttl, expiresAt)
            while (entries.size > maxEntries) entries.remove(entries.entries.first().key)
            publish()
        }
        expiryScope.launch {
            delay(ttl.durationMillis)
            mutex.withLock {
                if (entries[key]?.expiresAtMillis == expiresAt && nowMillis() >= expiresAt) {
                    entries.remove(key)
                    publish()
                }
            }
        }
    }

    override suspend fun delete(key: String) = mutex.withLock {
        if (entries.remove(key) != null) publish()
    }

    override suspend fun clear() = mutex.withLock {
        if (entries.isNotEmpty()) {
            entries.clear()
            publish()
        }
    }

    private fun touch(key: String, entry: Entry) {
        entries.remove(key)
        entries[key] = entry
        publish()
    }

    private fun publish() {
        snapshots.value = entries.toMap()
    }
}

private fun <T : Any> Any?.asType(kClass: KClass<T>): T? =
    if (this != null && kClass.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else {
        null
    }
