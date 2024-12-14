package com.mammoth.podcast.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.google.common.collect.ImmutableList
import com.mammoth.podcast.R

class MediaPlayerService : MediaSessionService() {
    private lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null
    private lateinit var nBuilder: NotificationCompat.Builder
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "media_playback_channel"
    }

    /**
     * This method is called when the service is being created.
     * It initializes the ExoPlayer and MediaSession instances.
     * onCreate() is invoked when the first controller is about to connect and the service is created and started.
     * This is the best place to build the Player and MediaSession.
     * The creation of the MediaSessionService allows it to run separately from the app's activity,
     * enabling external clients like Google Assistant, system media controls,
     * or companion devices to discover your service, connect to it, and control playback.
     */
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(MediaSessionService::class.java.simpleName, "onCreate")
        // Create an ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(this).build()
        // Create a MediaSession instance
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .build()

        // init notificationCompat.Builder before setting the MediaNotificationProvider
        this.setMediaNotificationProvider(object : MediaNotification.Provider{
            override fun createNotification(
                mediaSession: MediaSession,// this is the session we pass to style
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                createNotification(mediaSession)
                // notification should be created before you return here
                return MediaNotification(NOTIFICATION_ID,nBuilder.build())
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                Log.d(MediaPlayerService::class.java.simpleName, " = $action")
                return true
            }
        })
    }

    /**
     * This method is called when the system determines that the service is no longer used and is being removed.
     * It checks the player's state and if the player is not ready to play or there are no items in the media queue, it stops the service.
     *
     * @param rootIntent The original root Intent that was used to launch the task that is being removed.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(MediaSessionService::class.java.simpleName, "onTaskRemoved")
        val player = mediaSession?.player
        player?.let { exoPlayer ->
            if (!exoPlayer.playWhenReady
                || exoPlayer.mediaItemCount == 0
                || exoPlayer.playbackState == Player.STATE_ENDED) {
                // Stop the service if not playing,
                stopSelf()
            } else {
                // continue playing in the background otherwise.
                Log.d(this@MediaPlayerService::class.simpleName, "continue playing in the background")
            }
        }
    }

    /**
     * This method is called when a MediaSession.ControllerInfo requests the MediaSession.
     * It returns the current MediaSession instance.
     *
     * @param controllerInfo The MediaSession.ControllerInfo that is requesting the MediaSession.
     * @return The current MediaSession instance.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession.also {
            Log.d(MediaSessionService::class.java.simpleName, "onGetSession")
        }

    override fun onDestroy() {
        Log.d(MediaSessionService::class.java.simpleName, "onDestroy")
        mediaSession?.run {
            exoPlayer
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    fun  createNotification(session: MediaSession) {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID,"Channel", NotificationManager.IMPORTANCE_LOW))

        // NotificationCompat.Builder here.
        nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            // Text can be set here
            // but I believe setting MediaMetaData to MediaSession would be enough.
            // I havent tested it deeply yet but did display artist from session
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("your Content title")
            .setContentText("your content text")
            // set session here
            .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
        // we don build.
    }
}