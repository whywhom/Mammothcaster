package mammoth.mollie.caster

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeViewport
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.WebPodcastPlayer
import mammoth.mollie.caster.downloads.WebEpisodeDownloadGateway
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val downloads = remember { WebEpisodeDownloadGateway() }
        val store = remember {
            // Web builds must use a trusted same-origin proxy; never ship Podcast Index secrets.
            MollieStore(downloadGateway = downloads, discoveryConfig = DiscoveryConfig(podcastIndexBaseUrl = ""))
        }
        MolliecasterApp(store = store, player = remember { WebPodcastPlayer(downloads) })
    }
}
