package mammoth.mollie.caster

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.database.buildDatabase
import mammoth.mollie.caster.data.database.databaseBuilder
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.IosPodcastPlayer
import mammoth.mollie.caster.downloads.IosEpisodeDownloadGateway
import platform.Foundation.NSProcessInfo

fun MainViewController() = ComposeUIViewController {
    val downloads = remember { IosEpisodeDownloadGateway() }
    val store = remember {
        val environment = NSProcessInfo.processInfo.environment
        val proxyUrl = (environment["MOLLIE_PODCAST_INDEX_PROXY_URL"] as? String).orEmpty().trim()
        MollieStore(
            downloadGateway = downloads,
            database = buildDatabase(databaseBuilder()),
            discoveryConfig = DiscoveryConfig(
                appleStorefront = (environment["MOLLIE_APPLE_STOREFRONT"] as? String).orEmpty().ifBlank { "us" },
                podcastIndexBaseUrl = proxyUrl.ifBlank { "https://api.podcastindex.org/api/1.0" },
                podcastIndexApiKey = (environment["MOLLIE_PODCAST_INDEX_API_KEY"] as? String).orEmpty(),
                podcastIndexApiSecret = (environment["MOLLIE_PODCAST_INDEX_API_SECRET"] as? String).orEmpty(),
                podcastIndexUsesTrustedProxy = proxyUrl.isNotBlank(),
            ),
        )
    }
    val player = remember { IosPodcastPlayer(downloads) }
    MolliecasterApp(store = store, player = player)
}
