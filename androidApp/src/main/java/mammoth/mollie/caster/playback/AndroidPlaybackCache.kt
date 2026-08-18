package mammoth.mollie.caster.playback

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.common.util.UnstableApi
import java.io.File

/** Process-wide cache shared by playback and offline downloads. */
@OptIn(UnstableApi::class)
internal object AndroidPlaybackCache {
    @Volatile
    private var holder: Holder? = null

    fun get(context: Context): Holder = holder ?: synchronized(this) {
        holder ?: Holder(context.applicationContext).also { holder = it }
    }

    class Holder internal constructor(context: Context) {
        val databaseProvider = StandaloneDatabaseProvider(context)
        val cache = SimpleCache(
            File(context.filesDir, "episode_media"),
            NoOpCacheEvictor(),
            databaseProvider,
        )
        val upstreamFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent("Molliecaster/0.1")
            .setAllowCrossProtocolRedirects(true)
        val playbackDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
