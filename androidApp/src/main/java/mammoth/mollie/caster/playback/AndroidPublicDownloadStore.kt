package mammoth.mollie.caster.playback

import android.content.ContentValues
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.media3.common.C
import androidx.media3.datasource.DataSpec
import java.io.File
import java.io.FileOutputStream

/** Writes completed Media3 cache entries into the user-visible public Downloads collection. */
internal class AndroidPublicDownloadStore(private val context: Context) {
    fun export(sourceUrl: String, mimeType: String?, podcastDirectory: String, fileName: String): String =
        if (Build.VERSION.SDK_INT >= 29) exportScoped(sourceUrl, mimeType, podcastDirectory, fileName)
        else exportLegacy(sourceUrl, podcastDirectory, fileName)

    fun exists(reference: String?): Boolean = reference?.let { value -> runCatching {
        val uri = Uri.parse(value)
        if (uri.scheme == "file") File(requireNotNull(uri.path)).isFile
        else context.contentResolver.openFileDescriptor(uri, "r")?.use { true } == true
    }.getOrDefault(false) } == true

    fun delete(reference: String?) {
        reference ?: return
        runCatching {
            val uri = Uri.parse(reference)
            if (uri.scheme == "file") File(requireNotNull(uri.path)).delete()
            else context.contentResolver.delete(uri, null, null)
        }
    }

    private fun exportScoped(sourceUrl: String, mimeType: String?, podcastDirectory: String, fileName: String): String {
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/Molliecaster/$podcastDirectory/"
        findScoped(fileName, relativePath)?.let { return it.toString() }
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType ?: "audio/mpeg")
            put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = checkNotNull(context.contentResolver.insert(collection, values)) { "Could not create public download" }
        try {
            checkNotNull(context.contentResolver.openOutputStream(uri, "w")).use { output -> copyCached(sourceUrl, output::write) }
            context.contentResolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
            return uri.toString()
        } catch (error: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw error
        }
    }

    @Suppress("DEPRECATION")
    private fun exportLegacy(sourceUrl: String, podcastDirectory: String, fileName: String): String {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Molliecaster/$podcastDirectory")
        check(directory.mkdirs() || directory.isDirectory) { "Could not create public download folder" }
        val target = File(directory, fileName)
        if (target.isFile) return Uri.fromFile(target).toString()
        val partial = File(directory, "$fileName.part")
        FileOutputStream(partial).use { output -> copyCached(sourceUrl, output::write) }
        if (!partial.renameTo(target)) { partial.copyTo(target, overwrite = true); partial.delete() }
        return Uri.fromFile(target).toString()
    }

    private fun findScoped(fileName: String, relativePath: String): Uri? {
        if (Build.VERSION.SDK_INT < 29) return null
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME}=? AND ${MediaStore.Downloads.RELATIVE_PATH}=? AND ${MediaStore.Downloads.IS_PENDING}=0"
        return context.contentResolver.query(collection, projection, selection, arrayOf(fileName, relativePath), null)?.use { cursor ->
            if (cursor.moveToFirst()) ContentUris.withAppendedId(collection, cursor.getLong(0)) else null
        }
    }

    private fun copyCached(sourceUrl: String, write: (ByteArray, Int, Int) -> Unit) {
        val dataSource = AndroidPlaybackCache.get(context).downloadDataSourceFactory.createDataSource()
        try {
            dataSource.open(DataSpec.Builder().setUri(sourceUrl).setKey(sourceUrl).build())
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = dataSource.read(buffer, 0, buffer.size)
                if (count == C.RESULT_END_OF_INPUT) break
                write(buffer, 0, count)
            }
        } finally {
            dataSource.close()
        }
    }
}
