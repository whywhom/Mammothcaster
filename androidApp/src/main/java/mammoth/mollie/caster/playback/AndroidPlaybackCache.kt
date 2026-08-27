package mammoth.mollie.caster.playback

import android.content.Context
import android.net.Uri
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.common.util.UnstableApi
import java.io.File
import mammoth.mollie.caster.data.cache.MediaCachePolicy

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
        val downloadCache = SimpleCache(
            File(context.filesDir, "episode_media"),
            NoOpCacheEvictor(),
            databaseProvider,
        )
        private val playbackCache = SimpleCache(
            File(context.cacheDir, "episode_stream_cache"),
            LeastRecentlyUsedCacheEvictor(MAX_STREAM_CACHE_BYTES),
            databaseProvider,
        )
        private val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Molliecaster/0.1")
            .setAllowCrossProtocolRedirects(true)
        val upstreamFactory: DataSource.Factory = DefaultDataSource.Factory(context, httpFactory)
        val downloadDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
        private val cachedHttpDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
            .setCache(playbackCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val playbackDataSourceFactory: DataSource.Factory = RemoteOnlyCacheDataSourceFactory(
            cachedHttpDataSourceFactory,
            upstreamFactory,
        )

        private companion object { const val MAX_STREAM_CACHE_BYTES = MediaCachePolicy.DEFAULT_MAX_BYTES }
    }
}

/** Keeps explicit file/content downloads out of the automatic HTTP stream cache. */
private class RemoteOnlyCacheDataSourceFactory(
    private val remoteFactory: DataSource.Factory,
    private val localFactory: DataSource.Factory,
) : DataSource.Factory {
    override fun createDataSource(): DataSource = object : DataSource {
        private val listeners = mutableListOf<TransferListener>()
        private var delegate: DataSource? = null

        override fun addTransferListener(transferListener: TransferListener) {
            listeners += transferListener
        }

        override fun open(dataSpec: DataSpec): Long {
            val factory = when (dataSpec.uri.scheme?.lowercase()) {
                "http", "https" -> remoteFactory
                else -> localFactory
            }
            return factory.createDataSource().also { source ->
                listeners.forEach(source::addTransferListener)
                delegate = source
            }.open(dataSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            checkNotNull(delegate) { "Data source is not open" }.read(buffer, offset, length)

        override fun getUri(): Uri? = delegate?.uri

        override fun getResponseHeaders(): Map<String, List<String>> = delegate?.responseHeaders.orEmpty()

        override fun close() {
            delegate?.close()
            delegate = null
        }
    }
}
