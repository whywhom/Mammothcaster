package mammoth.mollie.caster.data.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CachePolicyTest {
    @Test
    fun metadataFreshnessHasAnExclusiveTtlBoundary() {
        val validatedAt = 1_000L
        val policy = MetadataCacheKind.EpisodeList

        assertTrue(policy.isFresh(validatedAt, validatedAt + policy.ttlMillis - 1))
        assertFalse(policy.isFresh(validatedAt, validatedAt + policy.ttlMillis))
        assertFalse(policy.isFresh(null, validatedAt))
        assertFalse(policy.isFresh(validatedAt, validatedAt - 1))
    }

    @Test
    fun retryScheduleOnlyAppliesToTransientFailures() {
        val policy = DownloadRetryPolicy()

        assertEquals(30_000L, policy.delayBeforeRetry(TransferFailure.Timeout, 0))
        assertEquals(120_000L, policy.delayBeforeRetry(TransferFailure.ConnectionLost, 1))
        assertEquals(600_000L, policy.delayBeforeRetry(TransferFailure.ServerError, 2))
        assertNull(policy.delayBeforeRetry(TransferFailure.ServerError, 3))
        assertNull(policy.delayBeforeRetry(TransferFailure.NotFound, 0))
        assertEquals(TransferFailure.Forbidden, DownloadRetryPolicy.classifyHttpStatus(403))
    }

    @Test
    fun remoteMediaRequiresStableIdentityAndHttpUrl() {
        assertNull(validateRemoteMedia("episode-1", "https://example.com/audio.mp3"))
        assertEquals("Episode ID is missing", validateRemoteMedia("", "https://example.com/audio.mp3"))
        assertEquals("Audio URL must use HTTP or HTTPS", validateRemoteMedia("episode-1", "file:///audio.mp3"))
    }
}
