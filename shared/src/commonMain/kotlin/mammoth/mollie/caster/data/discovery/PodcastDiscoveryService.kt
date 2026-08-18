package mammoth.mollie.caster.data.discovery

import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PodcastDiscoveryService(
    client: HttpClient,
    private val config: DiscoveryConfig,
) {
    private val podcastIndex = PodcastIndexTrendingClient(client, config)
    private val apple = AppleTopPodcastsClient(client, config.appleStorefront)

    suspend fun refresh(): DiscoverySnapshot = coroutineScope {
        val podcastIndexResult = if (config.podcastIndexEnabled) {
            async { podcastIndex.top(DISCOVERY_SOURCE_LIMIT) }
        } else {
            null
        }
        val appleResult = async { apple.top(DISCOVERY_SOURCE_LIMIT) }
        val index = podcastIndexResult?.await()
        val appleTop = appleResult.await()
        val indexPodcasts = (index as? DiscoverySourceResult.Success)?.podcasts.orEmpty()
        val applePodcasts = (appleTop as? DiscoverySourceResult.Success)?.podcasts.orEmpty()
        DiscoverySnapshot(
            podcastIndexTop = indexPodcasts,
            appleTop = applePodcasts,
            merged = mergeRankedDiscoverySources(indexPodcasts, applePodcasts),
            warnings = listOfNotNull(index?.warningOrNull(), appleTop.warningOrNull()),
        )
    }
}

private fun DiscoverySourceResult.warningOrNull(): String? = when (this) {
    is DiscoverySourceResult.Success -> null
    is DiscoverySourceResult.Failure -> message
    is DiscoverySourceResult.Unavailable -> message
}
