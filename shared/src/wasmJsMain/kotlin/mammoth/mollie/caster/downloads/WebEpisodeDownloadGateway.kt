package mammoth.mollie.caster.downloads

import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mammoth.mollie.caster.data.DownloadSnapshot
import mammoth.mollie.caster.data.EpisodeDownloadGateway
import mammoth.mollie.caster.model.Download
import mammoth.mollie.caster.model.DownloadState
import mammoth.mollie.caster.model.Episode
import mammoth.mollie.caster.model.EpisodeId

/** Cache Storage provides durable origin-local bytes; the browser controls the user's public Downloads folder. */
@OptIn(ExperimentalWasmJsInterop::class)
class WebEpisodeDownloadGateway : EpisodeDownloadGateway {
    private val generations = mutableMapOf<String, Int>()
    private val mutableDownloads = MutableStateFlow(DownloadSnapshot())
    override val downloads: StateFlow<DownloadSnapshot> = mutableDownloads.asStateFlow()
    override val supported: Boolean = true
    override val cellularDownloadControlSupported: Boolean = false
    override val cellularDownloadsAllowed: StateFlow<Boolean> = MutableStateFlow(false)

    override fun setCellularDownloadsAllowed(allowed: Boolean) = Unit

    init {
        reconcileBrowserDownloads(
            onReady = { json -> mutableDownloads.value = DownloadSnapshot(true, restoreIndex(json)) },
            onError = { /* Keep the snapshot uninitialized: an inspection failure is not proof that files are missing. */ },
        )
    }

    override fun download(episode: Episode, podcastTitle: String) {
        val enclosure = episode.enclosures.firstOrNull() ?: return
        val generation = nextGeneration(episode.id.value)
        val publicName = safeMediaPathComponent("$podcastTitle - ${episode.title}", "Podcast episode") + mediaFileExtension(enclosure)
        replace(Download(episode.id, enclosure.url, DownloadState.Downloading))
        cacheBrowserMedia(
            enclosure.url,
            episode.id.value,
            publicName,
            true,
            onReady = { objectUrl, size ->
                revokeBrowserObjectUrl(objectUrl)
                if (generations[episode.id.value] == generation) replace(Download(episode.id, enclosure.url, DownloadState.Completed, "cache://${episode.id.value}", size.toLong(), size.toLong()))
                else deleteBrowserMedia(enclosure.url, episode.id.value)
            },
            onError = { message -> if (generations[episode.id.value] == generation) replace(Download(episode.id, enclosure.url, DownloadState.Failed, failureMessage = message)) },
        )
    }

    override fun delete(episodeId: EpisodeId) {
        nextGeneration(episodeId.value)
        mutableDownloads.value.items.firstOrNull { it.episodeId == episodeId }?.let { deleteBrowserMedia(it.sourceUrl, episodeId.value) }
        mutableDownloads.update { it.copy(initialized = true, items = it.items.filterNot { item -> item.episodeId == episodeId }) }
    }

    /** Resolves Cache Storage first and fetches+stores the remote response on a miss. */
    fun resolveForPlayback(episode: Episode, onReady: (String) -> Unit, onError: (String) -> Unit) {
        val enclosure = episode.enclosures.firstOrNull() ?: return onError("This episode has no playable audio URL")
        cacheBrowserMedia(
            enclosure.url,
            episode.id.value,
            mediaFileName(episode.title, enclosure),
            false,
            onReady = { objectUrl, _ -> onReady(objectUrl) },
            onError = onError,
        )
    }

    fun isCached(episode: Episode): Boolean = browserMediaIsCached(episode.id.value)

    fun cacheForPlayback(episode: Episode) {
        val enclosure = episode.enclosures.firstOrNull() ?: return
        cacheBrowserMediaInBackground(enclosure.url, episode.id.value)
    }

    fun revoke(reference: String?) {
        if (reference?.startsWith("blob:") == true) revokeBrowserObjectUrl(reference)
    }

    private fun replace(download: Download) {
        mutableDownloads.update { snapshot ->
            snapshot.copy(initialized = true, items = snapshot.items.filterNot { it.episodeId == download.episodeId } + download)
        }
    }

    private fun restoreIndex(json: String): List<Download> = runCatching {
        val root = Json.parseToJsonElement(json).jsonObject
        root.mapNotNull { (episodeId, element) ->
            val item = element.jsonObject
            val source = item["source"]?.jsonPrimitive?.content ?: return@mapNotNull null
            Download(EpisodeId(episodeId), source, DownloadState.Completed, "cache://$episodeId")
        }
    }.getOrDefault(emptyList())

    private fun nextGeneration(episodeId: String): Int = ((generations[episodeId] ?: 0) + 1).also { generations[episodeId] = it }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(url, episodeId, fileName, savePublicCopy, onReady, onError) => {
  const cacheName = 'molliecaster-media-v1';
  const request = new Request(url, {mode: 'cors'});
  caches.open(cacheName).then(async cache => {
    let response = await cache.match(request);
    if (!response) {
      response = await fetch(request);
      if (!response.ok) throw new Error('Download returned HTTP ' + response.status);
      await cache.put(request, response.clone());
    }
    const playbackIndex = JSON.parse(localStorage.getItem('molliecaster-playback-cache-v1') || '{}');
    playbackIndex[episodeId] = true;
    localStorage.setItem('molliecaster-playback-cache-v1', JSON.stringify(playbackIndex));
    const blob = await response.blob();
    const objectUrl = URL.createObjectURL(blob);
    if (savePublicCopy) {
      const anchor = document.createElement('a');
      anchor.href = objectUrl;
      anchor.download = fileName;
      anchor.style.display = 'none';
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      const index = JSON.parse(localStorage.getItem('molliecaster-downloads-v1') || '{}');
      index[episodeId] = {source: url};
      localStorage.setItem('molliecaster-downloads-v1', JSON.stringify(index));
    }
    if (savePublicCopy) setTimeout(() => onReady(objectUrl, blob.size), 1000);
    else onReady(objectUrl, blob.size);
  }).catch(error => onError(String(error && error.message ? error.message : error)));
}""")
private external fun cacheBrowserMedia(
    url: String,
    episodeId: String,
    fileName: String,
    savePublicCopy: Boolean,
    onReady: (String, Double) -> Unit,
    onError: (String) -> Unit,
)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(url, episodeId) => {
  const request = new Request(url, {mode: 'cors'});
  caches.open('molliecaster-media-v1').then(async cache => {
    if (!(await cache.match(request))) {
      const response = await fetch(request);
      if (!response.ok) return;
      await cache.put(request, response);
    }
    const index = JSON.parse(localStorage.getItem('molliecaster-playback-cache-v1') || '{}');
    index[episodeId] = true;
    localStorage.setItem('molliecaster-playback-cache-v1', JSON.stringify(index));
  }).catch(() => {});
}""")
private external fun cacheBrowserMediaInBackground(url: String, episodeId: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(url, episodeId) => {
  caches.open('molliecaster-media-v1').then(cache => cache.delete(url));
  const index = JSON.parse(localStorage.getItem('molliecaster-downloads-v1') || '{}');
  delete index[episodeId];
  localStorage.setItem('molliecaster-downloads-v1', JSON.stringify(index));
  const playbackIndex = JSON.parse(localStorage.getItem('molliecaster-playback-cache-v1') || '{}');
  delete playbackIndex[episodeId];
  localStorage.setItem('molliecaster-playback-cache-v1', JSON.stringify(playbackIndex));
}""")
private external fun deleteBrowserMedia(url: String, episodeId: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("episodeId => !!JSON.parse(localStorage.getItem('molliecaster-playback-cache-v1') || '{}')[episodeId]")
private external fun browserMediaIsCached(episodeId: String): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""(onReady, onError) => {
  const downloadsKey = 'molliecaster-downloads-v1';
  const playbackKey = 'molliecaster-playback-cache-v1';
  const parseObject = value => {
    try {
      const parsed = JSON.parse(value || '{}');
      return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
    } catch (_) { return {}; }
  };
  (async () => {
    const index = parseObject(localStorage.getItem(downloadsKey));
    const valid = {};
    const discarded = new Set();
    const cache = await caches.open('molliecaster-media-v1');
    for (const [id, item] of Object.entries(index)) {
      const source = item && typeof item.source === 'string' ? item.source : '';
      const exists = source ? !!(await cache.match(source)) : false;
      if (exists) valid[id] = {source}; else discarded.add(id);
    }
    localStorage.setItem(downloadsKey, JSON.stringify(valid));
    const playback = parseObject(localStorage.getItem(playbackKey));
    for (const id of discarded) delete playback[id];
    localStorage.setItem(playbackKey, JSON.stringify(playback));
    onReady(JSON.stringify(valid));
  })().catch(error => onError(String(error && error.message ? error.message : error)));
}""")
private external fun reconcileBrowserDownloads(onReady: (String) -> Unit, onError: (String) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("url => URL.revokeObjectURL(url)")
private external fun revokeBrowserObjectUrl(url: String)
