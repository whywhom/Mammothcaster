package mammoth.mollie.caster

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeViewport
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.WebPodcastPlayer
import web.dom.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val store = remember {
            // Web builds must use a trusted same-origin proxy; never ship Podcast Index secrets.
            MollieStore(discoveryConfig = DiscoveryConfig(podcastIndexBaseUrl = ""))
        }
        MolliecasterApp(store = store, player = remember { WebPodcastPlayer() })
    }
}
