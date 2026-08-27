package mammoth.mollie.caster.data.cache

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryObjectStorageTest {
    @Test
    fun freshReadsExpireWhilePeekRetainsLastKnownValue() = runTest {
        var now = 1_000L
        val storage = InMemoryObjectStorage(maxEntries = 2, nowMillis = { now })

        storage.save("preview", "cached", TTL(100))
        assertEquals("cached", storage.getCurrent("preview", String::class))
        now += 100
        assertNull(storage.getCurrent("preview", String::class))
        assertEquals("cached", storage.peekCurrent("preview", String::class))
        assertNull(storage.get("preview", String::class).first())
        assertEquals("cached", storage.peek("preview", String::class).first())
    }

    @Test
    fun accessesRefreshLruOrderBeforeCapacityEviction() = runTest {
        val storage = InMemoryObjectStorage(maxEntries = 2, nowMillis = { 1_000L })

        storage.save("first", 1, TTL(1_000))
        storage.save("second", 2, TTL(1_000))
        assertEquals(1, storage.getCurrent("first", Int::class))
        storage.save("third", 3, TTL(1_000))

        assertNull(storage.peekCurrent("second", Int::class))
        assertEquals(1, storage.peekCurrent("first", Int::class))
        assertEquals(3, storage.peekCurrent("third", Int::class))
    }
}
