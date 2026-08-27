package mammoth.mollie.caster

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.database.buildDatabase
import mammoth.mollie.caster.data.database.databaseBuilder
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.DesktopPodcastPlayer
import mammoth.mollie.caster.downloads.DesktopEpisodeDownloadGateway
import kotlinx.coroutines.runBlocking
import mammoth.mollie.caster.playback.PlayerStatus
import java.awt.Toolkit
import kotlin.system.exitProcess

fun main() {
    val proxyUrl = System.getenv("MOLLIE_PODCAST_INDEX_PROXY_URL").orEmpty().trim()
    val downloads = DesktopEpisodeDownloadGateway()
    val store = MollieStore(
        downloadGateway = downloads,
        database = buildDatabase(databaseBuilder()),
        discoveryConfig = DiscoveryConfig(
            appleStorefront = System.getenv("MOLLIE_APPLE_STOREFRONT").orEmpty().ifBlank { "us" },
            podcastIndexBaseUrl = proxyUrl.ifBlank { "https://api.podcastindex.org/api/1.0" },
            podcastIndexApiKey = System.getenv("MOLLIE_PODCAST_INDEX_API_KEY").orEmpty(),
            podcastIndexApiSecret = System.getenv("MOLLIE_PODCAST_INDEX_API_SECRET").orEmpty(),
            podcastIndexUsesTrustedProxy = proxyUrl.isNotBlank(),
        ),
    )
    val player = DesktopPodcastPlayer(downloads)
    application {
        val windowState = rememberWindowState(size = defaultDesktopWindowSize())
        Window(
            state = windowState,
            onCloseRequest = {
                val finalState = player.state.value
                finalState.episode
                    ?.takeIf { finalState.positionMillis > 0 && finalState.status != PlayerStatus.Ended }
                    ?.let { episode ->
                        // The process exits immediately below, so wait for the final write.
                        runCatching {
                            runBlocking {
                                store.persistPlayback(episode, finalState.positionMillis, finalState.durationMillis)
                            }
                        }
                    }
                // Closing an engine after its event loop has already stopped can throw.
                // Cleanup must never prevent the user-requested application termination.
                runCatching { player.close() }
                runCatching { downloads.close() }
                runCatching { exitApplication() }
                // Compose and JavaFX each own desktop event loops. Terminate after their
                // cleanup so the macOS close button cannot leave a background process alive.
                exitProcess(0)
            },
            title = "Molliecaster",
        ) {
            MolliecasterApp(store = store, player = player)
        }
    }
}

private fun defaultDesktopWindowSize(): DpSize = runCatching {
    Toolkit.getDefaultToolkit().screenSize.let { screen ->
        DpSize((screen.width / 2).dp, (screen.height / 2).dp)
    }
}.getOrDefault(DpSize(960.dp, 640.dp))
