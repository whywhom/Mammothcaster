/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mammoth.caster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import com.mammoth.caster.theme.WearAppTheme
import com.mammoth.caster.ui.Episode
import com.mammoth.caster.ui.JetcasterNavController.navigateToEpisode
import com.mammoth.caster.ui.JetcasterNavController.navigateToLatestEpisode
import com.mammoth.caster.ui.JetcasterNavController.navigateToPodcastDetails
import com.mammoth.caster.ui.JetcasterNavController.navigateToUpNext
import com.mammoth.caster.ui.JetcasterNavController.navigateToYourPodcast
import com.mammoth.caster.ui.LatestEpisodes
import com.mammoth.caster.ui.PodcastDetails
import com.mammoth.caster.ui.UpNext
import com.mammoth.caster.ui.YourPodcasts
import com.mammoth.caster.ui.episode.EpisodeScreen
import com.mammoth.caster.ui.latest_episodes.LatestEpisodesScreen
import com.mammoth.caster.ui.library.LibraryScreen
import com.mammoth.caster.ui.player.PlayerScreen
import com.mammoth.caster.ui.podcast.PodcastDetailsScreen
import com.mammoth.caster.ui.podcasts.PodcastsScreen
import com.mammoth.caster.ui.queue.QueueScreen
import com.google.android.horologist.audio.ui.VolumeScreen
import com.google.android.horologist.audio.ui.VolumeViewModel
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.media.ui.navigation.MediaNavController.navigateToPlayer
import com.google.android.horologist.media.ui.navigation.MediaNavController.navigateToVolume
import com.google.android.horologist.media.ui.navigation.NavigationScreens
import com.google.android.horologist.media.ui.screens.playerlibrarypager.PlayerLibraryPagerScreen

@Composable
fun WearApp() {

    val navController = rememberSwipeDismissableNavController()
    val navHostState = rememberSwipeDismissableNavHostState()
    val volumeViewModel: VolumeViewModel = viewModel(factory = VolumeViewModel.Factory)

    WearAppTheme {
        AppScaffold(
            timeText = { ResponsiveTimeText() },
        ) {
            SwipeDismissableNavHost(
                startDestination = NavigationScreens.Player.navRoute,
                navController = navController,
                modifier = Modifier.background(Color.Transparent),
                state = navHostState,
            ) {
                composable(
                    route = NavigationScreens.Player.navRoute,
                    arguments = NavigationScreens.Player.arguments,
                    deepLinks = NavigationScreens.Player.deepLinks(""),
                ) {
                    val volumeState by volumeViewModel.volumeUiState.collectAsStateWithLifecycle()
                    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

                    PlayerLibraryPagerScreen(
                        pagerState = pagerState,
                        volumeUiState = { volumeState },
                        displayVolumeIndicatorEvents = volumeViewModel.displayIndicatorEvents,
                        playerScreen = {
                            PlayerScreen(
                                modifier = Modifier.fillMaxSize(),
                                volumeViewModel = volumeViewModel,
                                onVolumeClick = {
                                    navController.navigateToVolume()
                                }
                            )
                        },
                        libraryScreen = {
                            LibraryScreen(
                                onLatestEpisodeClick = { navController.navigateToLatestEpisode() },
                                onYourPodcastClick = { navController.navigateToYourPodcast() },
                                onUpNextClick = { navController.navigateToUpNext() },
                            )
                        },
                        backStack = it,
                    )
                }

                composable(
                    route = NavigationScreens.Volume.navRoute,
                    arguments = NavigationScreens.Volume.arguments,
                    deepLinks = NavigationScreens.Volume.deepLinks(""),
                ) {
                    ScreenScaffold(timeText = {}) {
                        VolumeScreen(volumeViewModel = volumeViewModel)
                    }
                }

                composable(
                    route = LatestEpisodes.navRoute,
                ) {
                    LatestEpisodesScreen(
                        onPlayButtonClick = {
                            navController.navigateToPlayer()
                        },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                composable(route = YourPodcasts.navRoute) {
                    PodcastsScreen(
                        onPodcastsItemClick = { navController.navigateToPodcastDetails(it.uri) },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                composable(route = PodcastDetails.navRoute) {
                    PodcastDetailsScreen(
                        onPlayButtonClick = {
                            navController.navigateToPlayer()
                        },
                        onEpisodeItemClick = { navController.navigateToEpisode(it.uri) },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                composable(route = UpNext.navRoute) {
                    QueueScreen(
                        onPlayButtonClick = {
                            navController.navigateToPlayer()
                        },
                        onEpisodeItemClick = { navController.navigateToPlayer() },
                        onDismiss = {
                            navController.popBackStack()
                            navController.navigateToYourPodcast()
                        }
                    )
                }
                composable(route = Episode.navRoute) {
                    EpisodeScreen(
                        onPlayButtonClick = {
                            navController.navigateToPlayer()
                        },
                        onDismiss = {
                            navController.popBackStack()
                            navController.navigateToYourPodcast()
                        }
                    )
                }
            }
        }
    }
}
