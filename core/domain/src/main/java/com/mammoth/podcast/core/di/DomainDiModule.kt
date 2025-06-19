package com.mammoth.podcast.core.di

import com.mammoth.podcast.core.data.Dispatcher
import com.mammoth.podcast.core.data.MammothDispatchers
import com.mammoth.podcast.core.player.EpisodePlayer
import com.mammoth.podcast.core.player.MockEpisodePlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainDiModule {
    @Provides
    @Singleton
    fun provideEpisodePlayer(
        @Dispatcher(MammothDispatchers.Main) mainDispatcher: CoroutineDispatcher
    ): EpisodePlayer = MockEpisodePlayer(mainDispatcher)
}