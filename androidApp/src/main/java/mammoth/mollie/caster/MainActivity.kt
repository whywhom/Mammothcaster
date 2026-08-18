package mammoth.mollie.caster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import mammoth.mollie.caster.data.MollieStore
import mammoth.mollie.caster.data.database.buildDatabase
import mammoth.mollie.caster.data.database.databaseBuilder
import mammoth.mollie.caster.data.discovery.DiscoveryConfig
import mammoth.mollie.caster.playback.AndroidDownloadGateway
import mammoth.mollie.caster.playback.AndroidPodcastPlayerAdapter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        setContent {
            val player = remember { AndroidPodcastPlayerAdapter(applicationContext) }
            val store = remember {
                val proxyUrl = BuildConfig.PODCAST_INDEX_PROXY_URL.trim()
                MollieStore(
                    downloadGateway = AndroidDownloadGateway(applicationContext),
                    database = buildDatabase(databaseBuilder(applicationContext)),
                    discoveryConfig = DiscoveryConfig(
                        appleStorefront = BuildConfig.APPLE_STOREFRONT,
                        podcastIndexBaseUrl = proxyUrl.ifBlank { "https://api.podcastindex.org/api/1.0" },
                        podcastIndexApiKey = BuildConfig.PODCAST_INDEX_API_KEY,
                        podcastIndexApiSecret = BuildConfig.PODCAST_INDEX_API_SECRET,
                        podcastIndexUsesTrustedProxy = proxyUrl.isNotBlank(),
                    ),
                )
            }
            DisposableEffect(player) { onDispose(player::close) }
            MolliecasterApp(store = store, player = player)
        }
    }
}
