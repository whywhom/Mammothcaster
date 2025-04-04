package com.mammoth.podcast.service
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mammoth.podcast.MammothCastApp
import com.mammoth.podcast.util.DownloadState
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.io.IOException

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val episodeUri = inputData.getString("EPISODE_URI") ?: return Result.failure()
        val downloadUrl = inputData.getString("DOWNLOAD_URL") ?: return Result.failure()
        val filePath = inputData.getString("FILE_PATH") ?: return Result.failure()

        val client = OkHttpClient()
        val request = Request.Builder().url(downloadUrl).build()
        updateDatabase(episodeUri, null, DownloadState.DOWNLOADING)

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.retry()
                }

                val resolver = applicationContext.contentResolver
                val audioCollection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                val newEpisodeDetails = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filePath)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val localEpisodeUri = resolver
                    .insert(audioCollection, newEpisodeDetails)

                if (localEpisodeUri != null) {
                    resolver.openFileDescriptor(localEpisodeUri, "w", null).use { pfd ->
                        // Ensure the file descriptor is not null
                        pfd?.fileDescriptor?.let { fileDescriptor ->
                            // Open output stream to write to the pending audio file
                            FileOutputStream(fileDescriptor).use { outputStream ->
                                response.body?.byteStream()?.use { inputStream ->
                                    // Copy data from the input stream to the output stream
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                    }
                    newEpisodeDetails.clear()
                    newEpisodeDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(localEpisodeUri, newEpisodeDetails, null, null)
                    // Update the database with the download status
                    updateDatabase(episodeUri, localEpisodeUri.toString(), DownloadState.DOWNLOADED)
                    Result.success()
                } else {
                    Result.failure()
                }
            }
        } catch (e: IOException) {
            updateDatabase(episodeUri, null, DownloadState.NOT_DOWNLOAD)
            Result.retry()
        }
    }

    private suspend fun updateDatabase(episodeUri: String, filePath: String?, downloadState: DownloadState) {
        // Update the database with the download status and file path
        // (Requires access to the EpisodeDao or similar)
        MammothCastApp.database.episodesDao().updateDownloadStatus(episodeUri, filePath, isDownloaded = downloadState.value)
    }
}