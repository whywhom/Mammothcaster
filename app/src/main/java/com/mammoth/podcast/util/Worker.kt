package com.mammoth.podcast.util

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mammoth.podcast.core.data.repository.DownloadWorker
import com.mammoth.podcast.core.model.EpisodeInfo

fun enqueueEpisodeDownloads(episodesList: List<EpisodeInfo>, context: Context) {
    val workManager = WorkManager.getInstance(context)

    for (episode in episodesList) {
        val extension = getFileExtension(episode.enclosureUrl)?:"mp3"
        val filePath = episode.uri.trim()+".$extension"
        val data = Data.Builder()
            .putString("EPISODE_URI", episode.uri)
            .putString("DOWNLOAD_URL", episode.enclosureUrl)
            .putString("FILE_PATH", filePath)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .build()

        workManager.enqueue(downloadWorkRequest)
    }
}

fun getFileExtension(url: String?): String? {
    return url?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
}

fun observeDownloadStatus(context: Context): LiveData<List<WorkInfo>> {
    val workManager = WorkManager.getInstance(context)
    return workManager.getWorkInfosByTagLiveData("EPISODE_DOWNLOAD")
}
