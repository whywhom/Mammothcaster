package com.mammoth.podcast.core.data.util

enum class DownloadState(val value: Int) {
    NOT_DOWNLOAD(0),
    DOWNLOADING(1),
    DOWNLOADED(2)
}