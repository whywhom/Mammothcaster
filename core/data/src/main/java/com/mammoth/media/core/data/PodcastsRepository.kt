package com.mammoth.media.core.data

import com.mammoth.media.core.data.network.RemoteRepository
import javax.inject.Inject

class PodcastsRepository @Inject constructor(
    private val remoteRepository: RemoteRepository,
){
}