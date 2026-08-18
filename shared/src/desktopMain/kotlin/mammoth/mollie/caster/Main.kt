package mammoth.mollie.caster

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.database.buildDatabase
import mammoth.mollie.caster.data.database.databaseBuilder
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.DesktopPodcastPlayer

fun main() {
    val proxyUrl = System.getenv("MOLLIE_PODCAST_INDEX_PROXY_URL").orEmpty().trim()
    val store = MollieStore(
        database = buildDatabase(databaseBuilder()),
        discoveryConfig = DiscoveryConfig(
            appleStorefront = System.getenv("MOLLIE_APPLE_STOREFRONT").orEmpty().ifBlank { "us" },
            podcastIndexBaseUrl = proxyUrl.ifBlank { "https://api.podcastindex.org/api/1.0" },
            podcastIndexApiKey = System.getenv("MOLLIE_PODCAST_INDEX_API_KEY").orEmpty(),
            podcastIndexApiSecret = System.getenv("MOLLIE_PODCAST_INDEX_API_SECRET").orEmpty(),
            podcastIndexUsesTrustedProxy = proxyUrl.isNotBlank(),
        ),
    )
    val player = DesktopPodcastPlayer()
    application {
        Window(onCloseRequest = { player.close(); exitApplication() }, title = "Molliecaster") {
            MolliecasterApp(store = store, player = player)
        }
    }
}
